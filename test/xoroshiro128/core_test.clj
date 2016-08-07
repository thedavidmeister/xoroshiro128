(ns xoroshiro128.core-test
  (:require [clojure.test :refer :all]
            [criterium.core]
            [xoroshiro128.core]))

(deftest splitmix64
  []
  (is (= -2152535657050944081 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 0))))
  (is (= -7995527694508729151 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 1))))
  (is (= -549842748227632346 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 4693323816697189744))))
  (is (= 1984452702661322627 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 5165464252035433577))))
  (is (= 8603550955848928026 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 -3762096910555017800))))
  (is (= 2259666501077083692 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 3265627685425294603)))))
