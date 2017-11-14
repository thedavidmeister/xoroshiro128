(ns xoroshiro128.test.splitmix64
 (:require
  xoroshiro128.test.util
  [xoroshiro128.core :as x]
  [clojure.test :refer [deftest is]]))

(deftest ??seed-extraction
 ; Splitmix64
 (let [gen-one (x/splitmix64 (xoroshiro128.test.util/rand-long))
       gen-one' (-> gen-one x/next x/next x/next)
       a (first (x/seed gen-one'))
       gen-two (x/splitmix64 a)]
  (is
   (.equals
    (-> gen-two x/next x/value)
    (-> gen-one' x/next x/value)))
  (is
   (.equals
    (-> gen-two x/next x/next x/value)
    (-> gen-one' x/next x/next x/value)))))
