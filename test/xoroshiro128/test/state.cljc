(ns xoroshiro128.test.state
 (:require
  [xoroshiro128.core :as x]
  xoroshiro128.long-int
  [clojure.test :refer [deftest is]]))

(deftest x-rand
 (let [seed (xoroshiro128.long-int/native-rand)
       x (x/xoroshiro128+ seed)
       j (x/jump x)
       j' (x/jump j)]
   ; Check that we can seed rand properly.
   (x/seed-rand! seed)
   (is
    (xoroshiro128.long-int/=
     (x/rand)
     (x/value x)))
   (is
    (xoroshiro128.long-int/=
     (x/rand)
     (x/value (x/next x))))
   (is
    (xoroshiro128.long-int/=
     (x/rand)
     (x/value (x/next (x/next x)))))
   (x/seed-rand! seed)
   (is
    (xoroshiro128.long-int/=
     (x/rand)
     (x/value x)))
   (is
    (xoroshiro128.long-int/=
     (x/rand)
     (x/value (x/next x))))
   (is
    (xoroshiro128.long-int/=
     (x/rand)
     (x/value (x/next (x/next x)))))
   ; Check that we can jump rand properly.
   (x/seed-rand! seed)
   (x/jump-rand!)
   (is
    (xoroshiro128.long-int/=
     (x/rand)
     (x/value j)))
   (x/seed-rand! seed)
   (x/jump-rand!)
   (x/jump-rand!)
   (is
    (xoroshiro128.long-int/= 
     (x/rand)
     (x/value j')))))
