(ns xoroshiro128.test.performance
 (:require
  xoroshiro128.long-int
  [xoroshiro128.core :as x]
  #?(:clj criterium.core)))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

(defn bench
 [f]
 #?(:clj (criterium.core/bench (f))
    :cljs (dotimes [n 10000]
           (f))))

(defn bench-native
 []
 (bench xoroshiro128.long-int/native-rand))

(defn bench-rand
 []
 (bench x/rand))
