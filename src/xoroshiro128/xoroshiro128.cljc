(ns xoroshiro128.xoroshiro128
 (:require
  xoroshiro128.splitmix64
  xoroshiro128.prng
  xoroshiro128.uuid
  xoroshiro128.long-int))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

; Xoroshiro128+
; Reference C implementation http://xoroshiro.di.unimi.it/xoroshiro128plus.c

(declare xoroshiro128+)

(deftype Xoroshiro128+ [^long a ^long b]
  xoroshiro128.prng/IPRNG
  (value
    [_]
    (xoroshiro128.long-int/+ a b))

  (next
    [_]
    (let [x (xoroshiro128.long-int/bit-xor a b)
          a' (xoroshiro128.long-int/bit-xor
              (xoroshiro128.long-int/bit-rotate-left a 55)
              (xoroshiro128.long-int/bit-xor
               x
               (xoroshiro128.long-int/bit-shift-left x 14)))
          b' (xoroshiro128.long-int/bit-rotate-left x 36)]
      (Xoroshiro128+. a' b')))

  (seed
    [_]
    [a b])

  (jump
    [this]
    (let [s (atom '(0 0))
          x (atom this)]
      (doseq [^long i [xoroshiro128.constants/L-0xbeac0467eba5facb xoroshiro128.constants/L-0xd86b048b86aa9922]
              ^long b (range 64)]
        (when-not (= 0 (xoroshiro128.long-int/bit-and i (xoroshiro128.long-int/bit-shift-left 1 b)))
                  (swap! s #(map xoroshiro128.long-int/bit-xor % (xoroshiro128.prng/seed @x))))
        (swap! x xoroshiro128.prng/next))
      (apply xoroshiro128+ @s))))

(defn seed64->seed128
 "Uses splitmix to generate a 128 bit seed from a 64 bit seed"
 [^long x]
 (let [splitmix (xoroshiro128.splitmix64/splitmix64 x)
       a (-> splitmix xoroshiro128.prng/next xoroshiro128.prng/value)
       b (-> splitmix xoroshiro128.prng/next xoroshiro128.prng/next xoroshiro128.prng/value)]
  [a b]))

(defn uuid->seed128
 "Converts a uuid to a 128 bit seed"
 [^java.util.UUID u]
 [(xoroshiro128.uuid/most-significant-bits u)
  (xoroshiro128.uuid/least-significant-bits u)])

(defn xoroshiro128+
 ([x]
  (cond
   (number? x)
   (xoroshiro128+ (seed64->seed128 x))

   (sequential? x)
   (apply xoroshiro128+ x)

   (uuid? x)
   (xoroshiro128+ (uuid->seed128 x))))
 ([^long a ^long b] (Xoroshiro128+. a b)))
