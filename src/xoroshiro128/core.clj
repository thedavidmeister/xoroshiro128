(ns xoroshiro128.core)

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def s0! (atom 1))
(def s1! (atom 3))

(defn next!
  []
  (let [s0 (long @s0!)
        s1 (long @s1!)
        result (+ s0
                  s1)
        x (bit-xor s1 s0)
        b (bit-xor  (Long/rotateLeft s0 55)
                    x
                    (bit-shift-left x 14))
        c (Long/rotateLeft x 36)]
    (reset! s0! b)
    (reset! s1! c)
    result))

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
