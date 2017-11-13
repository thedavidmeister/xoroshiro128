(def project 'thedavidmeister/xoroshiro128)
(def version "1.1.0-SNAPSHOT")

(set-env!
 :source-paths #{"src"}
 :dependencies
 '[[org.clojure/clojure "1.9.0-RC1"]
   [criterium "0.4.4" :scope "test"]
   [adzerk/boot-test "RELEASE" :scope "test"]
   [adzerk/bootlaces "RELEASE" :scope "test"]])

(task-options!
 pom {:project project
      :version version
      :description "Clojure implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/"
      :url "https://github.com/thedavidmeister/xoroshiro128"
      :scm {:url "https://github.com/thedavidmeister/wheel"}
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}})

(require
 '[adzerk.boot-test :refer [test]])

(replace-task!
 [t test]
 (fn [& xs]
  (set-env! :source-paths #{"src" "test"})
  (apply t xs)))
