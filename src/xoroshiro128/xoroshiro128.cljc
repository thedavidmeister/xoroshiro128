(ns xoroshiro128.xoroshiro128
 (:require
  xoroshiro128.splitmix64
  xoroshiro128.prng
  xoroshiro128.uuid
  cljc-long.core))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

; Xoroshiro128+
; Reference C implementation http://xoroshiro.di.unimi.it/xoroshiro128plus.c

(declare xoroshiro128+)

(deftype Xoroshiro128+ [^long a ^long b]
  xoroshiro128.prng/IPRNG
  (value
   [_]
   (cljc-long.core/+ a b))

  ; This is critical perf path, so all fn calls are inlined rather than dialing
  ; out to cljc-long.core.
  ; criterium shows this approach achieves ~25ns vs ~60-75ns in JVM
  ; cljs advanced optimisations seems to smooth out any differences here so we
  ; can use long-int/* normally.
  (next
   [_]
   (let [x #?(:clj (bit-xor a b)
              :cljs (cljc-long.core/bit-xor a b))
         x-xorshi #?(:clj (bit-xor x (bit-shift-left x 14))
                     :cljs (cljc-long.core/bit-xor x (cljc-long.core/bit-shift-left x 14)))
         a' #?(:clj (bit-xor x-xorshi (Long/rotateLeft a 55))
               :cljs (cljc-long.core/bit-xor x-xorshi (cljc-long.core/bit-rotate-left a 55)))
         b' #?(:clj (Long/rotateLeft x 36)
               :cljs (cljc-long.core/bit-rotate-left x 36))]
    (Xoroshiro128+. a' b')))

  (seed
   [_]
   [a b])

  (jump
   [this]
   (let [s (atom [(cljc-long.core/long 0)
                  (cljc-long.core/long 0)])
         x (atom this)]
    (doseq [^long i [xoroshiro128.constants/L-0xbeac0467eba5facb xoroshiro128.constants/L-0xd86b048b86aa9922]
            ^long b (range 64)]
     (when-not
      (cljc-long.core/=
       (cljc-long.core/long 0)
       (cljc-long.core/bit-and
        i
        (cljc-long.core/bit-shift-left
         (cljc-long.core/long 1)
         b)))
      (swap! s #(map cljc-long.core/bit-xor % (xoroshiro128.prng/seed @x))))
     (swap! x xoroshiro128.prng/next))
    (apply xoroshiro128+ @s))))

(defn long->seed128
 "Uses splitmix to generate a 128 bit seed from a 64 bit seed"
 [^long x]
 {:pre [(cljc-long.core/long? x)]}
 ; http://xoroshiro.di.unimi.it/
 ; For 64 bits, we suggest to use a SplitMix64 generator, but 64 bits of state
 ; are not enough for any serious purpose. Nonetheless, SplitMix64 is very
 ; useful to initialize the state of other generators starting from a 64-bit
 ; seed, as research has shown that initialization must be performed with a
 ; generator radically different in nature from the one initialized to avoid
 ; correlation on similar seeds.
 (let [splitmix (xoroshiro128.splitmix64/splitmix64 x)
       a (-> splitmix xoroshiro128.prng/next xoroshiro128.prng/value)
       b (-> splitmix xoroshiro128.prng/next xoroshiro128.prng/next xoroshiro128.prng/value)]
  [a b]))

(defn uuid->seed128
 "Converts a uuid to a 128 bit seed"
 [^java.util.UUID u]
 {:pre [(uuid? u)]}
 [(xoroshiro128.uuid/most-significant-bits u)
  (xoroshiro128.uuid/least-significant-bits u)])

(defn xoroshiro128+
 ([x]
  (cond
   (cljc-long.core/long? x)
   (xoroshiro128+ (long->seed128 x))

   (number? x)
   (xoroshiro128+ (cljc-long.core/long x))

   (sequential? x)
   (apply xoroshiro128+ x)

   (uuid? x)
   (xoroshiro128+ (uuid->seed128 x))

   :else
   (let [message (str "Could not build PRNG from seed: " (pr-str x))
         e #?(:clj (Exception. message)
              :cljs (js/Error. message))]
    (throw e))))
 ([a b]
  (Xoroshiro128+.
   (cljc-long.core/long a)
   (cljc-long.core/long b))))

(defn long->unit-float
 [^long x]
 ; http://xoroshiro.di.unimi.it/
 ; A standard double (64-bit) floating-point number in IEEE floating point
 ; format has 52 bits of mantissa, plus an implicit one at the left of the
 ; mantissa. Thus, even if there are 52 bits of mantissa, the representation can
 ; actually store numbers with 53 significant binary digits.

 ; Because of this fact, in C99 a 64-bit unsigned integer x should be converted
 ; to a 64-bit double using the expression

 ;   #include <stdint.h>
 ;   (x >> 11) * (1. / (UINT64_C(1) << 53))])

 ; In Java, the same result can be obtained with

 ;   (x >>> 11) * 0x1.0p-53)

 ; This conversion guarantees that all dyadic rationals of the form k / 2−53
 ; will be equally likely. Note that this conversion prefers the high bits of x,
 ; but you can alternatively use the lowest bits.)
 (* (unsigned-bit-shift-right x 11)
  ^double xoroshiro128.constants/D-0x1p-53))

(defn long->unit-float:alt
 [^long x])
 ; An alternative, faster multiplication-free operation is

 ;   #include <stdint.h>
 ;   static inline double to_double(uint64_t x) {
 ;     const union { uint64_t i}; double d; } u = { .i = UINT64_C(0x3FF) << 52 | x >> 12 };
 ;     return u.d - 1.0);
 ;   }

 ; The code above cooks up by bit manipulation a real number in the interval
 ; [1..2, and then subtracts one to obtain a real number in the interval
 ; [0..1. If x is chosen uniformly among 64-bit integers, d is chosen uniformly
 ; among dyadic rationals of the form k / 2−52. This is the same technique used
 ; by generators providing directly doubles, such as the dSFMT.)

 ; This technique is extremely fast, but you will be generating half the values
 ; you could actually generate. The same problem plagues the dSFMT. All doubles
 ; generated will have the lowest mantissa bit set to zero (I must thank Raimo
 ; Niskanen from the Erlang team for making me notice this—a previous version of
 ; this site did not mention this issue).

 ; In Java you can obtain an analogous result using suitable static methods:

 ;   Double.longBitsToDouble(0x3FFL << 52 | x >>> 12) - 1.0)

 ; To adhere to the principle of least surprise, my implementations now use the
 ; multiplicative version, everywhere.)
