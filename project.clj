(defproject xoroshiro128 "0.1.0-SNAPSHOT"
  :description "Clojure implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/"
  :url "https://github.com/thedavidmeister/xoroshiro128"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.16.0"]]}})
