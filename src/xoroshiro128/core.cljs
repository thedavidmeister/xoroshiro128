(ns xoroshiro128.core
 (:require
  [xoroshiro128.long-int :as l])
 (:refer-clojure :exclude [next rand]))

; PRNG protocol

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
 (l/long "-7046029254386353131"))

; 0xBF58476D1CE4E5B9 = -4658895280553007687
(def L-0xBF58476D1CE4E5B9
 (l/long "-4658895280553007687"))

; 0x94D049BB133111EB = -7723592293110705685
(def L-0x94D049BB133111EB
 (l/long "-7723592293110705685"))

(deftype Splitmix64 [a]
  IPRNG
  (value
    [_]
    (as-> a a

      (l/add a L-0x9E3779B97F4A7C15)

      (l/multiply
       (l/xor a (l/unsigned-bit-shift-right a 30))
       L-0xBF58476D1CE4E5B9)

      (l/multiply
       (l/xor a (l/unsigned-bit-shift-right a 27))
       L-0x94D049BB133111EB)

      (l/xor a (l/unsigned-bit-shift-right a 31))))

  (next
    [_]
    (Splitmix64. (.add a L-0x9E3779B97F4A7C15)))

  (seed [_] [a]))

(defn splitmix64
 [a]
 (Splitmix64. (l/long a)))

; Xoroshiro128+
; Reference C implementation http://xoroshiro.di.unimi.it/xoroshiro128plus.c

(declare xoroshiro128+)

(deftype Xoroshiro128+ [^long a ^long b]
  IPRNG
  (value
   [_]
   (.add a b))

  (next
   [_]
   (let [x (l/xor a b)
         a' (l/xor
             (l/rotate-left a 55)
             (l/xor x
              (.shiftLeft x 14)))
         b' (l/rotate-left x 36)]
     (Xoroshiro128+. a' b')))

  (seed
    [_]
    [a b])

  (jump
    [this]
    ; 0xbeac0467eba5facb = -4707382666127344949
    ; 0xd86b048b86aa9922 = -2852180941702784734
    (let [s   (atom '(0 0))
          x   (atom this)]
      (doseq [^long i [-4707382666127344949 -2852180941702784734]
              ^long b (range 64)]
        (when-not (= 0 (.and i (.shiftLeft 1 b)))
                  (swap! s #(map bit-xor % (seed @x))))
        (swap! x next))
      (apply xoroshiro128+ @s))))

(defn seed64->seed128
 "Uses splitmix to generate a 128 bit seed from a 64 bit seed"
 [x]
 (let [splitmix (Splitmix64. (long x))
       a (-> splitmix next value)
       b (-> splitmix next next value)]
  [a b]))

(defn uuid->seed128
 "Converts a uuid to a 128 bit seed"
 [u]
 [u
  u])

(defn xoroshiro128+
  ([x]
   (cond (number? x)
         (xoroshiro128+ (seed64->seed128 x))

         (sequential? x)
         (apply xoroshiro128+ x)

         (uuid? x)
         (xoroshiro128+ (uuid->seed128 x))))
  ([^long a ^long b] (Xoroshiro128+. a b)))
