(ns xoroshiro128.prng
 (:refer-clojure :exclude [next]))

; PRNG protocol

(defprotocol IPRNG
 "A single, seedable state in a PRNG sequence"
 (value [_] "The value of this state, as a long")
 (next [_] "The next state in the sequence")
 (seed [_] "A vector of the seed.")
 (jump [_] "The jump function for this algorithm."))
