(ns xoroshiro128.core)

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defprotocol IPRNG
  (presult [_])
  (pnext [_]))

(deftype PRNG [^long a ^long b]
  IPRNG
  (presult
    [_]
    ; We need to auto-promote integers as the original C implementation relied
    ; on unsigned integers. This doesn't affect bitwise operations, but will
    ; trip up standard clojure addition with +.
    (+ a b))
  (pnext
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
  (map presult (iterate pnext (PRNG. a b))))

(def g (atom (PRNG. 1 3)))

(defn next'!
  []
  (let [result (presult @g)]
    (swap! g pnext)
    result))
