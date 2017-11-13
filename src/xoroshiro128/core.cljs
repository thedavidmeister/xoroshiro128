(ns xoroshiro128.core
 (:refer-clojure :exclude [next rand uuid? long]))

(defprotocol IPRNG
  "A single, seedable state in a PRNG sequence"
  (value [_] "The value of this state, as a long")
  (next [_] "The next state in the sequence")
  (seed [_] "A vector of the seed.")
  (jump [_] "The jump function for this algorithm."))

; Splitmix64
; Reference C implementation at http://xoroshiro.di.unimi.it/splitmix64.c)

; 0x9E3779B97F4A7C15 = -7046029254386353131
(def L-0x9E3779B97F4A7C15
 (goog.math.Long.fromString "-7046029254386353131" 10))

; 0xBF58476D1CE4E5B9 = -4658895280553007687
(def L-0xBF58476D1CE4E5B9
 (goog.math.Long.fromString "-4658895280553007687" 10))

; 0x94D049BB133111EB = -7723592293110705685
(def L-0x94D049BB133111EB
 (goog.math.Long.fromString "-7723592293110705685" 10))

(deftype Splitmix64 [a]
  IPRNG
  (value
    [_]
    (as-> a a

      (.add a L-0x9E3779B97F4A7C15)

      (.multiply
       (.xor a (.shiftRightUnsigned a 30))
       L-0xBF58476D1CE4E5B9)

      (.multiply
       (.xor a (.shiftRightUnsigned a 27))
       L-0x94D049BB133111EB)

      (.xor a (.shiftRightUnsigned a 31))))

  (next
    [_]
    (Splitmix64. (.add a L-0x9E3779B97F4A7C15)))

  (seed [_] [a]))

(defn long? [a] (instance? goog.math.Long a))

(defn long
 [a]
 {:post [(long? %)]}
 (cond
  (long? a)
  a

  (string? a)
  (goog.math.Long.fromString a 10)

  (number? a)
  (goog.math.Long.fromNumber a)))

(defn splitmix64
 [a]
 (Splitmix64. (long a)))
