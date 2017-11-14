(ns xoroshiro128.core
 (:refer-clojure :exclude [next rand uuid?])
 (:require
  xoroshiro128.constants
  xoroshiro128.prng
  xoroshiro128.splitmix64
  xoroshiro128.xoroshiro128
  [xoroshiro128.long-int :as l]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def value xoroshiro128.prng/value)
(def next xoroshiro128.prng/next)
(def seed xoroshiro128.prng/seed)
(def jump xoroshiro128.prng/jump)

(def seed64->seed128 xoroshiro128.xoroshiro128/seed64->seed128)
(def uuid->seed128 xoroshiro128.xoroshiro128/uuid->seed128)

(def splitmix64 xoroshiro128.splitmix64/splitmix64)
(def xoroshiro128+ xoroshiro128.xoroshiro128/xoroshiro128+)

(defn uuid?
 "BC for uuid? function (clojure 1.9)"
 [u]
 ; https://github.com/weavejester/medley/blob/master/src/medley/core.cljc#L312
 (instance? java.util.UUID u))

; Simple PRNG
; Can be re-seeded ad-hoc using seed-rand!
; Relies on mutable state.

(def ^:private rand-state (atom nil))

(defn seed-rand!
 "Takes a long, passes it to splitmix64 to create two new longs, then seeds rand with those new longs."
 ([^long x]
  (reset! rand-state (xoroshiro128+ x)))
 ([^long a b]
  (reset! rand-state (xoroshiro128+ a b))))

(seed-rand! (.nextLong (java.util.Random.)))

(defn jump-rand!
 "Jumps rand. Equivalent to calling rand 2^64 times."
 []
 (swap! rand-state jump))

(defn rand
 "Generate a random long using Xoroshiro128+ seeded with splitmix64."
 []
 (let [result (value @rand-state)]
  (swap! rand-state next)
  result))
