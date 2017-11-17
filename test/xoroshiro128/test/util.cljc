(ns xoroshiro128.test.util
 (:require
  cljc-long.core
  [clojure.test :refer [deftest is]]))

(defn longs-equal?
 [s-1 s-2]
 (doall
  (map
   #(is (cljc-long.core/= %1 %2))
   (map cljc-long.core/long s-1)
   (map cljc-long.core/long s-2))))

(defn is-long?
 [expected test-output]
 (is
  (cljc-long.core/=
   (cljc-long.core/long expected)
   test-output)))
