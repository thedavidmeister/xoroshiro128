(ns xoroshiro128.long-int
 (:refer-clojure :exclude [long])
 #?(:cljs (:require goog.math.Long)))

(defn long? [a]
 #?(:cljs (instance? goog.math.Long a)
    :clj (instance? java.lang.Long a)))

(defn long
 [a]
 {:post [(long? %)]}
 #?(:cljs
    (cond
     (long? a)
     a

     (string? a)
     (goog.math.Long.fromString a 10)

     (number? a)
     (goog.math.Long.fromNumber a))
    :clj (clojure.core/long a)))

; @see int-rotate-left
; https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/core.cljs#L879
(defn rotate-left
 [x n]
 (.or
  (.shiftLeft x n)
  (.shiftRightUnsigned x (- n))))
