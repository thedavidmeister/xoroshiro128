(ns xoroshiro128.splitmix64
 (:require
  xoroshiro128.prng
  xoroshiro128.long-int
  xoroshiro128.constants))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

; Splitmix64
; Reference C implementation at http://xoroshiro.di.unimi.it/splitmix64.c)

(deftype Splitmix64 [a]
 xoroshiro128.prng/IPRNG
 (value
   [_]
   (as-> a a

     (xoroshiro128.long-int/add a xoroshiro128.constants/L-0x9E3779B97F4A7C15)

     (xoroshiro128.long-int/multiply
      (xoroshiro128.long-int/xor
       a
       (xoroshiro128.long-int/unsigned-bit-shift-right a 30))
      xoroshiro128.constants/L-0xBF58476D1CE4E5B9)

     (xoroshiro128.long-int/multiply
      (xoroshiro128.long-int/xor
       a
       (xoroshiro128.long-int/unsigned-bit-shift-right a 27))
      xoroshiro128.constants/L-0x94D049BB133111EB)

     (xoroshiro128.long-int/xor
      a
      (xoroshiro128.long-int/unsigned-bit-shift-right a 31))))

 (next
   [_]
   (Splitmix64.
    (xoroshiro128.long-int/add
     a
     xoroshiro128.constants/L-0x9E3779B97F4A7C15)))

 (seed [_] [a]))

(defn splitmix64
 [a]
 (Splitmix64. (xoroshiro128.long-int/long a)))
