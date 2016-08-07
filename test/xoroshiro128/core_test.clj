(ns xoroshiro128.core-test
  (:require [clojure.test :refer :all]
            [xoroshiro128.core :refer :all]))

; Test that we can read environment variables.
(deftest ^:wip b
  (let [r 10000
        -j (java.util.Random.)
        j (repeatedly #(.nextLong -j))
        c (repeatedly rand)
        ci (repeatedly #(rand-int Integer/MAX_VALUE))
        gen (repeatedly xoroshiro128.core/next!)
        g (xoroshiro128.core/generator 1 3)]
    (prn "java long:")
    (time (doall (take r j)))
    (prn "rand:")
    (time (doall (take r c)))
    (prn "rand-int:")
    (time (doall (take r ci)))
    (prn "atomic:")
    ; (criterium.core/bench (doall (take r gen)))
    ; (criterium.core/bench (doall (take r gen))
    ;                       :verbose)
    (prn "type:")))
    ; (criterium.core/bench (into [] (take r g)))))
    ; (criterium.core/bench (doall (take r g))
    ;                       :verbose)))
