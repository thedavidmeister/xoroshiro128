(ns xoroshiro128.core
  (:refer-clojure :exclude [next rand]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defprotocol IPRNG
  "A single, seedable state in a PRNG sequence"
  (value [_] "The value of this state, as a long")
  (next [_] "The next state in the sequence")
  (seed [_] "A vector of the seed.")
  (jump [_] "The jump function for this algorithm."))

; Splitmix64
; Reference C implementation at http://xoroshiro.di.unimi.it/splitmix64.c

(deftype Splitmix64 [^long a]
  IPRNG
  (value
    [_]
    (as-> a a
      ; 0x9E3779B97F4A7C15 = -7046029254386353131
      (+ a -7046029254386353131)

      ; 0xBF58476D1CE4E5B9 = -4658895280553007687
      (*  (bit-xor a (unsigned-bit-shift-right a 30))
          -4658895280553007687)

      ; 0x94D049BB133111EB = -7723592293110705685
      (*  (bit-xor a (unsigned-bit-shift-right a 27))
          -7723592293110705685)

      (*  (bit-xor a (unsigned-bit-shift-right a 31)))))

  (next
    [_]
    ; 0x9E3779B97F4A7C15 = -7046029254386353131
    (Splitmix64. (+ a -7046029254386353131)))

  (seed [_] [a]))

(defn splitmix64 [a] (Splitmix64. a))

; Xoroshiro128+
; Reference C implementation http://xoroshiro.di.unimi.it/xoroshiro128plus.c

(declare xoroshiro128+)

(deftype Xoroshiro128+ [^long a ^long b]
  IPRNG
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
 (let [splitmix (Splitmix64. x)
       a (-> splitmix next value)
       b (-> splitmix next next value)]
  [a b]))

(defn xoroshiro128+
  ([x]
   (cond (number? x)
         (xoroshiro128+ (seed64->seed128 x))

         (sequential? x)
         (apply xoroshiro128+ x)))
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
