(ns xoroshiro128.core-test
  (:require [clojure.test :refer :all]
            [criterium.core]
            [xoroshiro128.core]))

(deftest a
  []
  (time (prn "native" (.nextLong (java.util.Random.))))
  (time (prn "xoro" (xoroshiro128.core/rand))))
