(ns xoroshiro128.test.xoroshiro128
 (:require
  [clojure.test :refer [deftest is]]
  [xoroshiro128.core :as x]
  xoroshiro128.test.util
  xoroshiro128.long-int))

(deftest ??seed-extraction
 ; We should be able to take a seed from any point in a sequence and seed a new
 ; identical sequence that starts from the first point.
 ; Xoroshiro128+
 (let [gen-one (x/xoroshiro128+ (xoroshiro128.long-int/native-rand))
       gen-one' (-> gen-one x/next x/next x/next)
       a (first (x/seed gen-one'))
       b (second (x/seed gen-one'))
       gen-two (x/xoroshiro128+ a b)]
  (is
   (xoroshiro128.long-int/=
    (-> gen-two x/next x/value)
    (-> gen-one' x/next x/value)))
  (is
   (xoroshiro128.long-int/=
    (-> gen-two x/next x/next x/value)
    (-> gen-one' x/next x/next x/value)))))

(deftest ??xoroshiro128+--args
 ; Check the signature of xoroshiro128+ all works as expected.
 ; 1x 64 bit.
 (let [l (xoroshiro128.long-int/native-rand)
       x (x/xoroshiro128+ l)
       seed128 (x/long->seed128 l)]
  (xoroshiro128.test.util/longs-equal?
   seed128
   (x/seed x)))

 ; 2x 64 bit
 (let [a (xoroshiro128.long-int/native-rand)
       b (xoroshiro128.long-int/native-rand)
       x (x/xoroshiro128+ a b)]
  (xoroshiro128.test.util/longs-equal?
   [a b]
   (x/seed x)))

 ; 1x 128 bit vector
 (let [seed128 [(xoroshiro128.long-int/native-rand)
                (xoroshiro128.long-int/native-rand)]
       x (x/xoroshiro128+ seed128)]
  (xoroshiro128.test.util/longs-equal?
   seed128
   (x/seed x)))

 ; 1x uuid
 (let [uuid (xoroshiro128.uuid/random-uuid)
       x (x/xoroshiro128+ uuid)]
  (is
   (xoroshiro128.test.util/longs-equal?
    (xoroshiro128.uuid/as-longs uuid)
    (x/seed x)))))
