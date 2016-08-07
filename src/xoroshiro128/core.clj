(ns xoroshiro128.core
  (:refer-clojure :exclude [next rand]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defprotocol IPRNG
  "A single, seedable state in a PRNG sequence"
  (value [_] "The value of this state, as a long")
  (next [_] "The next state in the sequence"))

; Splitmix64
; Reference C implementation at http://xoroshiro.di.unimi.it/splitmix64.c

(deftype Splitmix64 [^long x]
  IPRNG
  (value
    [_]
    (as-> x x
      (+ x (unchecked-long 0x9E3779B97F4A7C15))

      (*  (bit-xor x (unsigned-bit-shift-right x 30))
          (unchecked-long 0xBF58476D1CE4E5B9))

      (*  (bit-xor x (unsigned-bit-shift-right x 27))
          (unchecked-long 0x94D049BB133111EB))

      (*  (bit-xor x (unsigned-bit-shift-right x 31)))))

  (next
    [_]
    (Splitmix64. (+ x (unchecked-long 0x9E3779B97F4A7C15)))))

(defn splitmix64 [x] (Splitmix64. x))

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
      (Xoroshiro128+. a' b'))))

(defn xoroshiro128+ [a b] (Xoroshiro128+. a b))

(defn generator
  [a b]
  (map value (iterate next (Xoroshiro128+. a b))))

(def simple-state (atom (Xoroshiro128+. 1 2)))

(defn rand
  []
  (let [result (value @simple-state)]
    (swap! simple-state next)
    result))
