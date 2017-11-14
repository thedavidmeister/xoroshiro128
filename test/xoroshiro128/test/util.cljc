(ns xoroshiro128.test.util
 (:require
  xoroshiro128.long-int
  [clojure.test :refer [deftest is]]))

(defn longs-equal?
 [s-1 s-2]
 (doall
  (map
   #(is (xoroshiro128.long-int/= %1 %2))
   (map xoroshiro128.long-int/long s-1)
   (map xoroshiro128.long-int/long s-2))))
