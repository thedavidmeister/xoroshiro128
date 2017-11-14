(ns xoroshiro128.long-int
 (:refer-clojure :exclude [long unsigned-bit-shift-right])
 #?(:cljs (:require goog.math.Long)))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

(defn long? [a]
 #?(:cljs (instance? goog.math.Long a)
    :clj (instance? java.lang.Long a)))

(defn long
 [a]
 {:post [(long? %)]}
 (cond
  (long? a)
  a

  (string? a)
  #?(:cljs (goog.math.Long.fromString a 10)
     :clj (Long/parseLong a))

  (number? a)
  #?(:cljs (goog.math.Long.fromNumber a)
     :clj (cast Long a))))

(defn add
 [^long a ^long b]
 #?(:cljs (.add a b)
    :clj (+ a b)))

(defn multiply
 [^long a ^long b]
 #?(:cljs (.multiply a b)
    :clj (* a b)))

(defn xor
 [^long a ^long b]
 #?(:cljs (.xor a b)
    :clj (bit-xor a b)))

(defn unsigned-bit-shift-right
 [^long a ^long n]
 #?(:cljs (.shiftRightUnsigned a n)
    :clj (clojure.core/unsigned-bit-shift-right a n)))

; @see int-rotate-left
; https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/core.cljs#L879
(defn rotate-left
 [^long x ^long n]
 #?(:cljs
    (.or
     (.shiftLeft x n)
     (.shiftRightUnsigned x (- n)))
    :clj
    (Long/rotateLeft x n)))
