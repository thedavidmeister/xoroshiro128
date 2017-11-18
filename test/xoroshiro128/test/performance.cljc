(ns xoroshiro128.test.performance
 (:require
  cljc-long.core
  xoroshiro128.xoroshiro128
  xoroshiro128.state
  #?(:clj criterium.core)
  [clojure.test :refer [deftest is]]))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

(defn bench
 [f]
 #?(:clj (criterium.core/bench (f))
    :cljs
    (let [profile-name (gensym)
          start (.now js/performance)]
     (dotimes [n 1000000]
      (f))
     (prn
      (-
       (.now js/performance)
       start)))))

(defn bench-native
 []
 (bench cljc-long.core/native-rand))

(defn bench-rand
 []
 (bench xoroshiro128.state/rand))

(defn bench-rand-float
 []
 (bench
  (comp xoroshiro128.xoroshiro128/long->unit-float xoroshiro128.state/rand)))

#?(:cljs
   (deftest ??benchmark
    (when true
     (prn "benchmarking xoroshiro128.state/rand")
     (bench-rand)

     (prn "benchmarking xoroshiro128.state/rand as xoroshiro128.xoroshiro128/long->unit-float")
     (bench-rand-float)

     (prn "benchmarking cljc-long.core/native-rand")
     (bench-native)

     (prn "benchmarking Math.random")
     (bench Math.random))))
