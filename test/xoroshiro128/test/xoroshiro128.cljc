(ns xoroshiro128.test.xoroshiro128
 (:require
  [clojure.test :refer [deftest is]]
  [xoroshiro128.core :as x]
  xoroshiro128.long-int))

(deftest ??seed-extraction
 ; We should be able to take a seed from any point in a sequence and seed a new
 ; identical sequence that starts from the first point.
 ; Xoroshiro128+
 (let [gen-one (x/xoroshiro128+ (xoroshiro128.long-int/native-rand))
       gen-one' (-> gen-one x/next x/next x/next)]))
   ;     a (first (x/seed gen-one'))
   ;     b (second (x/seed gen-one'))
   ;     gen-two (x/xoroshiro128+ a b)]
   ; (is (=  (-> gen-two x/next x/value)
   ;         (-> gen-one' x/next x/value)))
   ; (is (=  (-> gen-two x/next x/next x/value)
   ;         (-> gen-one' x/next x/next x/value)))))
