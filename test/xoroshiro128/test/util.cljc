(ns xoroshiro128.test.util
 (:require
  xoroshiro128.long-int))

(defn rand-long
 []
 #?(:cljs
    (xoroshiro128.long-int/long
     (* 9223372036854775807 (Math/random)))
    :clj
    (.nextLong (java.util.Random.))))
