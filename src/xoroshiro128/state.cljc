(ns xoroshiro128.state
 (:refer-clojure :exclude [rand])
 (:require
  xoroshiro128.prng
  cljc-long.core
  xoroshiro128.xoroshiro128))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

; wrapper around native PRNG functionality to generate longs

(defn native-rand
 []
 {:post [(cljc-long.core/long? %)]}
 #?(:cljs
    ; lifted from https://cljs.github.io/api/cljs.core/random-uuid
    (let [hex #(.toString (rand-int 16) 16)]
     (goog.math.Long.fromString
      (apply str (take 16 (repeatedly hex)))
      16))
    :clj
    (.nextLong (java.util.Random.))))

; Simple PRNG
; Can be re-seeded ad-hoc using seed-rand!
; Relies on mutable state.

(def ^:private rand-state (atom nil))

(defn seed-rand!
 "Takes a long, passes it to splitmix64 to create two new longs, then seeds rand with those new longs."
 ([x]
  (reset! rand-state (xoroshiro128.xoroshiro128/xoroshiro128+ x)))
 ([^long a ^long b]
  (reset! rand-state (xoroshiro128.xoroshiro128/xoroshiro128+ a b))))

(seed-rand! (native-rand))

(defn jump-rand!
 "Jumps rand. Equivalent to calling rand 2^64 times."
 []
 (swap! rand-state xoroshiro128.prng/jump))

(defn rand
 "Generate a random long using Xoroshiro128+ seeded with splitmix64."
 []
 (loop [old-val @rand-state]
  (if (compare-and-set! rand-state old-val (xoroshiro128.prng/next old-val))
   (xoroshiro128.prng/value old-val)
   (recur @rand-state))))
