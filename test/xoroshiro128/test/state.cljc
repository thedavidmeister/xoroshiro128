(ns xoroshiro128.test.state
 (:require
  [xoroshiro128.core :as x]
  xoroshiro128.long-int
  [clojure.test :refer [deftest is]]))

#?(:clj
   (deftest x-rand--parallel
    (let [iterations 10000]
     ; ensure there are no dupes caused by parallel execution
     (let [prands (pmap
                   (fn [_] (x/rand))
                   (range iterations))
           dupes (filter
                  (comp
                   (partial < 1)
                   second)
                  (frequencies prands))]
      (is (zero? (count dupes))))

     ; ensure parallel and serial execution returns the same set of numbers
     ; compare sets not seqs as pmap will screw up the order
     (let [seed (xoroshiro128.uuid/random-uuid)]
      (x/seed-rand! seed)
      (is
       (=
        (set
         (pmap
          (fn [_] (x/rand))
          (range iterations)))
        (set
         (take iterations
          (map x/value (iterate x/next (x/xoroshiro128+ seed)))))))))))

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
