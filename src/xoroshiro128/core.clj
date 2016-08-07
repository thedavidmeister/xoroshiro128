(ns xoroshiro128.core
  (:refer-clojure :exclude [next rand]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defprotocol IPRNG
  (result [_])
  (next [_]))

(deftype PRNG [^long a ^long b]
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
      (PRNG. a' b'))))

(defn prng [a b] (PRNG. a b))

(defn generator
  [a b]
  (map result (iterate next (PRNG. a b))))

(def simple-state (atom (PRNG. 1 3)))

(defn rand
  []
  (let [result (result @simple-state)]
    (swap! simple-state next)
    result))
