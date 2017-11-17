(ns xoroshiro128.test.performance
 (:require
  cljc-long.core
  [xoroshiro128.core :as x]
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
 (bench x/rand))

#?(:cljs
   (deftest ??benchmark
    (when false
     (prn "benchmarking xoroshiro128.state/rand")
     (bench-rand)

     (prn "benchmarking cljc-long.core/native-rand")
     (bench-native)

     (prn "benchmarking Math.random")
     (bench Math.random))))
