(def project 'thedavidmeister/xoroshiro128)
(def version "1.1.0-SNAPSHOT")

(set-env!
 :source-paths #{"src"}
 :dependencies
 '[[org.clojure/clojure "1.9.0-RC1"]
   [org.clojure/clojurescript "1.9.946"]
   [org.clojure/tools.namespace "0.2.11"]

   [adzerk/boot-cljs "2.1.2" :scope "test"]
   [doo "0.1.7" :scope "test"]
   [criterium "0.4.4" :scope "test"]
   [adzerk/boot-test "RELEASE" :scope "test"]
   [adzerk/bootlaces "RELEASE" :scope "test"]
   [crisptrutski/boot-cljs-test "0.3.4" :scope "test"]])

(task-options!
 pom {:project project
      :version version
      :description "Clojure implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/"
      :url "https://github.com/thedavidmeister/xoroshiro128"
      :scm {:url "https://github.com/thedavidmeister/wheel"}})

(require
 '[adzerk.bootlaces :refer :all]
 '[adzerk.boot-test :refer [test]]
 '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(bootlaces! version)

(replace-task!
 [t test]
 (fn [& xs]
  (set-env! :source-paths #{"src" "test"})
  (apply t xs)))

(deftask tests-cljs
 "Run all the CLJS tests"
 []
 (set-env! :source-paths #{"src" "test"})
 (comp
  (test-cljs
   :cljs-opts {:process-shim false}
   :namespaces [#".*test.*"])))

(deftask deploy
 []
 (comp
  (build-jar)
  (push-snapshot)))
