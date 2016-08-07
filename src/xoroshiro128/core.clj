(ns xoroshiro128.core
  (:refer-clojure :exclude [next rand]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defprotocol IPRNG
  (result [_])
  (next [_]))

(deftype Splitmix64 [^long x]
  IPRNG
  (result
    [_]
    (let [z     (+ x (unchecked-long 0x9E3779B97F4A7C15))
          z'    (*  (bit-xor z (unsigned-bit-shift-right z 30))
                    (unchecked-long 0xBF58476D1CE4E5B9))
          z''   (*  (bit-xor z (unsigned-bit-shift-right z 27))
                    (unchecked-long 0x94D049BB133111EB))
          z'''  (bit-xor z'' (unsigned-bit-shift-right z'' 31))]
      z'''))

  (next
    [_]
    (Splitmix64. (+ x (unchecked-long 0x9E3779B97F4A7C15)))))

(defn splitmix64 [x] (Splitmix64. x))

(deftype Xoroshiro128 [^long a ^long b]
  IPRNG
  (result
    [_]
    (+ a b))

  (next
    [_]
    (let [x   (bit-xor a b)
          a'  (bit-xor  (Long/rotateLeft a 55)
                        x
                        (bit-shift-left x 14))
          b'  (Long/rotateLeft x 36)]
      (Xoroshiro128. a' b'))))

(defn xoroshiro128 [a b] (Xoroshiro128. a b))

(defn generator
  [a b]
  (map result (iterate next (Xoroshiro128. a b))))

(def simple-state (atom (Xoroshiro128. 1 2)))

(defn rand
  []
  (let [result (result @simple-state)]
    (swap! simple-state next)
    result))
