(ns xoroshiro128.core
 (:refer-clojure :exclude [next rand uuid?])
 (:require
  xoroshiro128.constants
  xoroshiro128.prng
  xoroshiro128.splitmix64
  [xoroshiro128.long-int :as l]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def value xoroshiro128.prng/value)
(def next xoroshiro128.prng/next)
(def seed xoroshiro128.prng/seed)
(def jump xoroshiro128.prng/jump)

(def splitmix64 xoroshiro128.splitmix64/splitmix64)

; Xoroshiro128+
; Reference C implementation http://xoroshiro.di.unimi.it/xoroshiro128plus.c

(declare xoroshiro128+)

(deftype Xoroshiro128+ [^long a ^long b]
  xoroshiro128.prng/IPRNG
  (value
    [_]
    (+ a b))

  (next
    [_]
    (let [x   (bit-xor a b)
          a'  (bit-xor  (Long/rotateLeft a 55)
                        x
                        (bit-shift-left x 14))
          b'  (Long/rotateLeft x 36)]
      (Xoroshiro128+. a' b')))

  (seed
    [_]
    [a b])

  (jump
    [this]
    ; 0xbeac0467eba5facb = -4707382666127344949
    ; 0xd86b048b86aa9922 = -2852180941702784734
    (let [s   (atom '(0 0))
          x   (atom this)]
      (doseq [^long i [-4707382666127344949 -2852180941702784734]
              ^long b (range 64)]
        (when-not (= 0 (bit-and i (bit-shift-left 1 b)))
                  (swap! s #(map bit-xor % (seed @x))))
        (swap! x next))
      (apply xoroshiro128+ @s))))

(defn seed64->seed128
 "Uses splitmix to generate a 128 bit seed from a 64 bit seed"
 [^long x]
 (let [splitmix (splitmix64 x)
       a (-> splitmix next value)
       b (-> splitmix next next value)]
  [a b]))

(defn uuid?
 "BC for uuid? function (clojure 1.9)"
 [u]
 ; https://github.com/weavejester/medley/blob/master/src/medley/core.cljc#L312
 (instance? java.util.UUID u))

(defn uuid->seed128
 "Converts a uuid to a 128 bit seed"
 [^java.util.UUID u]
 [(.getMostSignificantBits u)
  (.getLeastSignificantBits u)])

(defn xoroshiro128+
  ([x]
   (cond (number? x)
         (xoroshiro128+ (seed64->seed128 x))

         (sequential? x)
         (apply xoroshiro128+ x)

         (uuid? x)
         (xoroshiro128+ (uuid->seed128 x))))
  ([^long a ^long b] (Xoroshiro128+. a b)))

; Simple PRNG
; Can be re-seeded ad-hoc using seed-rand!
; Relies on mutable state.

(def ^:private rand-state (atom nil))

(defn seed-rand!
  "Takes a long, passes it to splitmix64 to create two new longs, then seeds rand with those new longs."
  ([^long x]
   (reset! rand-state (xoroshiro128+ x)))
  ([^long a b]
   (reset! rand-state (xoroshiro128+ a b))))

(seed-rand! (.nextLong (java.util.Random.)))

(defn jump-rand!
  "Jumps rand. Equivalent to calling rand 2^64 times."
  []
  (swap! rand-state jump))

(defn rand
  "Generate a random long using Xoroshiro128+ seeded with splitmix64."
  []
  (let [result (value @rand-state)]
    (swap! rand-state next)
    result))
