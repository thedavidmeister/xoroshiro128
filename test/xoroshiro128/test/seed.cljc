(ns xoroshiro128.test.seed
 (:refer-clojure :exclude [random-uuid])
 (:require
  xoroshiro128.core
  xoroshiro128.test.util
  [clojure.test :refer [deftest is]]))

(deftest ??uuid->seed128
 (let [uuids [#uuid "32be7cb8-cef3-4f1f-bfd7-67993641cb9e"
              #uuid "89e24685-47cb-4cc3-8c94-112873807db5"
              #uuid "9eac068c-bcfc-463f-b434-e95235cad3b3"
              #uuid "00bcd8e0-397a-44b3-9d81-1f0fd9cb27d5"
              #uuid "ac7cc8b6-6634-430e-b8a0-0361aa116ce8"
              #uuid "33ed27e6-7b6f-4e2a-b0b2-8cf2bca6188d"
              #uuid "229d2ec0-04de-41a3-a403-154454fa754f"
              #uuid "e7f9d247-4ea3-44b5-96f9-bf2b3138399a"
              #uuid "cb0af93c-299d-4bcb-bfdd-cd0d865d19a8"
              #uuid "60d8e578-e8c3-46e9-b4d3-69a2d3b848ee"]
       expected-seeds [["3656497080659300127" "-4623112584734585954"]
                       ["-8511162807527715645" "-8317003746412298827"]
                       ["-7013223318186867137" "-5461483908794494029"]
                       ["53155753170191539" "-7097357384820250667"]
                       ["-6017714316349521138" "-5143107056457126680"]
                       ["3741690736281603626" "-5714350003004761971"]
                       ["2494201170883396003" "-6628430843331578545"]
                       ["-1731121378043411275" "-7567807506589140582"]
                       ["-3815963697451545653" "-4621312184639743576"]
                       ["6978579930074531561" "-5416869778738886418"]]]
  (doall
   (map
    (fn [u es]
     (xoroshiro128.test.util/longs-equal?
      es
      (xoroshiro128.core/uuid->seed128 u)))
    uuids
    expected-seeds))))
