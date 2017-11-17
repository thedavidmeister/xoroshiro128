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
