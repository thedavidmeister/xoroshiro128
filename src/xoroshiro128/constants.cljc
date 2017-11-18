(ns xoroshiro128.constants
 (:require
  cljc-long.core))

#?(:clj (set! *warn-on-reflection* true))
#?(:clj (set! *unchecked-math* :warn-on-boxed))

; 0x9E3779B97F4A7C15 = -7046029254386353131
(def L-0x9E3779B97F4A7C15
 #?(:cljs (cljc-long.core/long "-7046029254386353131")
    :clj -7046029254386353131))

; 0xBF58476D1CE4E5B9 = -4658895280553007687
(def L-0xBF58476D1CE4E5B9
 #?(:cljs (cljc-long.core/long "-4658895280553007687")
    :clj -4658895280553007687))

; 0x94D049BB133111EB = -7723592293110705685
(def L-0x94D049BB133111EB
 #?(:cljs (cljc-long.core/long "-7723592293110705685")
    :clj -7723592293110705685))

; 0xbeac0467eba5facb = -4707382666127344949
(def L-0xbeac0467eba5facb
 #?(:cljs (cljc-long.core/long "-4707382666127344949")
    :clj -4707382666127344949))

; 0xd86b048b86aa9922 = -2852180941702784734
(def L-0xd86b048b86aa9922
 #?(:cljs (cljc-long.core/long "-2852180941702784734")
    :clj -2852180941702784734))

; http://www.exploringbinary.com/hexadecimal-floating-point-constants/
(def D-0x1p-53
 #?(:cljs (Math/pow 2 -53)
    :clj (Double/parseDouble "0x1.0p-53")))

(def D-0x3FF (cljc-long.core/long 0x3FF))
