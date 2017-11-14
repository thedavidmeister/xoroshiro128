(ns xoroshiro128.state
 (:refer-clojure :exclude [rand])
 (:require
  xoroshiro128.prng
  xoroshiro128.long-int
  xoroshiro128.xoroshiro128))

; Simple PRNG
; Can be re-seeded ad-hoc using seed-rand!
; Relies on mutable state.

(def ^:private rand-state (atom nil))

(defn seed-rand!
 "Takes a long, passes it to splitmix64 to create two new longs, then seeds rand with those new longs."
 ([^long x]
  (reset! rand-state (xoroshiro128.xoroshiro128/xoroshiro128+ x)))
 ([^long a b]
  (reset! rand-state (xoroshiro128.xoroshiro128/xoroshiro128+ a b))))

(seed-rand! (xoroshiro128.long-int/native-rand))

(defn jump-rand!
 "Jumps rand. Equivalent to calling rand 2^64 times."
 []
 (swap! rand-state xoroshiro128.prng/jump))

(defn rand
 "Generate a random long using Xoroshiro128+ seeded with splitmix64."
 []
 (let [result (xoroshiro128.prng/value @rand-state)]
  (swap! rand-state xoroshiro128.prng/next)
  result))
