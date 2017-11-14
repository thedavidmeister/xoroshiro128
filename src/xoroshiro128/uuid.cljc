(ns xoroshiro128.uuid
 #?(:cljs (:refer-clojure :exclude [random-uuid]))
 #?(:cljs (:require goog.math.Long)))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

; from https://github.com/weavejester/medley
(defn random-uuid
 "Generates a new random UUID. Same as `cljs.core/random-uuid` except it works
 for Clojure as well as ClojureScript."
 []
 #?(:clj (java.util.UUID/randomUUID)
    :cljs (cljs.core/random-uuid)))

(defn as-longs
 [u]
 #?(:clj [(.getMostSignificantBits u) (.getLeastSignificantBits u)]
    :cljs
    (as-> u u
     (str u)
     (clojure.string/replace u "-" "")
     (partition 16 u)
     (map (partial apply str) u)
     (map
      #(goog.math.Long.fromString % 16)
      u))))

(defn most-significant-bits
 [^java.util.UUID u]
 (first (as-longs u)))

(defn least-significant-bits
 [^java.util.UUID u]
 (last (as-longs u)))
