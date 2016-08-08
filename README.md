# xoroshiro128

Clojure implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/

The algorithm has been shown to be fast and produce superior statistical results to many PRNGs shipped with languages, including Java. The statistical results have been verified in both PractRand and TestU01 by the authors.

xoroshiro128+ is designed to be the successor to xorshift128+, currently used in the JavaScript engines of Chrome, Firefox and Safari.

Both xorshift128+ and xoroshiro128+ have a period of 2<sup>128</sup> but xoroshiro128+ is **benchmarked by the authors as 20% faster and with 20% fewer failures in BigCrush than its predecessor.**

[![Clojars Project](https://img.shields.io/clojars/v/xoroshiro128.svg)](https://clojars.org/xoroshiro128)

## Installation

### Leiningen

Add the following to your dependencies.

`[xoroshiro128 "0.1.0-SNAPSHOT"]`

### Maven

````
<dependency>
  <groupId>xoroshiro128</groupId>
  <artifactId>xoroshiro128</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
````
## Usage

Everything is in `xoroshiro128.core`.

`(require '[xoroshiro128.core])`

### Simple

The simplest usage is to simply call `rand` to generate a random long.

This uses an atom to store the state of the prng, which is updated to the next value in sequence on each call to `rand`.

`rand` is automatically seeded by `java.util.Random`, passed through splitmix64, and so should Just Work.

This atom can be manually reset/seeded (e.g. during testing) by calling `seed-rand!` and passing it a long. Internally, as per the suggestion in the reference implementation, this long will be passed through a splitmix64 generator to generate the two 64 bit seeds needed for the xoroshiro128+ algorithm. `seed-rand!` also accepts two arguments, which should both be longs to pass directly to xoroshiro128+ (no splitmix64 involved).

### Advanced

Both xoroshiro128+ and splitmix64 are implemented as immutable types with a shared protocol. The `IPRNG` protocol exposes `next` and `value` to generate the next item in the sequence or the long representing the current item, respectively.

This protocol allows us to construct a lazy sequence from a seed quite easily with `iterate` and `map`.

````
(require '[xoroshiro128.core :as x])
(def seed 12345)
(def my-rand-seq (map x/value (iterate x/next (x/xoroshiro128+ seed))))
(take 5 my-rand-seq)
; (5983371452340661002 -139170902943549277 2600980751997770790 3131701164191746090 -3375623441569470803)
````

Additionally, we can inspect any item in the sequence to extract the seed, allowing us to resume the sequence from that point later.

````
(require '[xoroshiro128.core :as x])
(def seed 9876)
(def my-rand-item (-> (x/xoroshiro128+ seed) x/next x/next x/next))

; Peek ahead at my-rand-item + 1
(x/value (x/next my-rand-item))
; 3486300715335445982

; Extract the seed from my-rand-item for later.
(.-a my-rand-item)
; -5785456751514194665
(.-b my-rand-item)
; 7961309068892779353

; Create a new item from our extracted seed.
(def new-rand-item (x/xoroshiro128+ -5785456751514194665 7961309068892779353))
; If we seeded correctly, new-rand-item + 1 should be the same as my-rand-item + 1.
(x/value (x/next new-rand-item))
; 3486300715335445982
````

The seeds for a xoroshiro128+ is `a` and `b`. The seed for a splitmix64 is `a`.

## License

The xoroshiro128+ algorithm reference implementation in C was developed by David Blackman and Sebastiano Vigna in 2016 under a Creative Commons public domain license https://creativecommons.org/publicdomain/zero/1.0/.

This clojure implementation is copyright © 2016 David Meister

Distributed under the [Eclipse Public License version 1.0](http://www.eclipse.org/legal/epl-v10.html).
