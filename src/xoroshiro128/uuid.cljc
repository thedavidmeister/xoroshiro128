(ns xoroshiro128.uuid
 #?(:cljs (:require goog.math.Long)))

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
