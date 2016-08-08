(ns xoroshiro128.core-test
  (:require [clojure.test :refer :all]
            [criterium.core]
            [xoroshiro128.core :as x]))

(deftest splitmix64
  []
  (let [next-seed #(+ % -7046029254386353131)
        iterator #(map x/value (iterate x/next (x/splitmix64 %)))]
    ; Just outlining a list of known-good values.
    (is (= -2152535657050944081 (x/value (x/splitmix64 0))))
    (is (=  7960286522194355700
            (x/value (x/splitmix64 (next-seed 0)))
            (x/value (x/next (x/splitmix64 0)))))
    (is (= '(-2152535657050944081 7960286522194355700 487617019471545679 -537132696929009172 1961750202426094747 6038094601263162090 3207296026000306913 -4214222208109204676 4532161160992623299 -884877559730491226)
            (take 10 (iterator 0))))

    (is (= -7995527694508729151 (x/value (x/splitmix64 1))))
    (is (=  -4689498862643123097
            (x/value (x/splitmix64 (next-seed 1)))
            (x/value (x/next (x/splitmix64 1)))))
    (is (=  '(-7995527694508729151 -4689498862643123097 -534904783426661026 8196980753821780235 8195237237126968761 -4373826470845021568 -2262517385565684571 -8797857673641491083 5266705631892356520 -3800091893662914666)
            (take 10 (iterator 1))))

    (is (= -549842748227632346 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 4693323816697189744))))
    (is (= 1984452702661322627 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 5165464252035433577))))
    (is (= 8603550955848928026 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 -3762096910555017800))))
    (is (= 2259666501077083692 (xoroshiro128.core/value (xoroshiro128.core/splitmix64 3265627685425294603))))))

(deftest xoroshiro128+
  []
  (let [x (x/xoroshiro128+ 0 1)]
    (is (= 1 (x/value x)))
    (is (= 68719493121 (x/value (x/next x))))
    (is (= 38280734540038433 (x/value (x/next (x/next x))))))

  (let [x (x/xoroshiro128+ 1 0)]
    (is (= 1 (x/value x)))
    (is (= 36028865738457089 (x/value (x/next x))))
    (is (= 2322306399469857 (x/value (x/next (x/next x))))))

  (let [x (x/xoroshiro128+ 1 1)]
    (is (= 2 (x/value x)))
    (is (= 36028797018963968 (x/value (x/next x))))
    (is (= 36099165897359360 (x/value (x/next (x/next x))))))

  (let [x (x/xoroshiro128+ -2288729261622650145 -6926512846790308433)]
    (is (= -9215242108412958578 (x/value x)))
    (is (= -7532115046694008527 (x/value (x/next x))))
    (is (= 7536573313527036548 (x/value (x/next (x/next x))))))

  (let [x (x/xoroshiro128+ 6229099873966726092 6043473223518792799)]
    (is (= -6174170976224032725 (x/value x)))
    (is (= -709792299180922954 (x/value (x/next x))))
    (is (= -6877720052118061367 (x/value (x/next (x/next x)))))))
