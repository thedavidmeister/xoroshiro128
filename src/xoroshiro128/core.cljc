(ns xoroshiro128.core
 (:refer-clojure :exclude [next rand])
 (:require
  xoroshiro128.constants
  xoroshiro128.prng
  xoroshiro128.splitmix64
  xoroshiro128.xoroshiro128
  xoroshiro128.state
  [xoroshiro128.long-int :as l]))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

(def value xoroshiro128.prng/value)
(def next xoroshiro128.prng/next)
(def seed xoroshiro128.prng/seed)
(def jump xoroshiro128.prng/jump)

(def seed64->seed128 xoroshiro128.xoroshiro128/seed64->seed128)
(def uuid->seed128 xoroshiro128.xoroshiro128/uuid->seed128)

(def splitmix64 xoroshiro128.splitmix64/splitmix64)
(def xoroshiro128+ xoroshiro128.xoroshiro128/xoroshiro128+)

(def seed-rand! xoroshiro128.state/seed-rand!)
(def jump-rand! xoroshiro128.state/jump-rand!)
(def rand xoroshiro128.state/rand)
