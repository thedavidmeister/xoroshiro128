# xoroshiro128

Clojure implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/

The algorithm has been shown to be fast and produce superior statistical results to many PRNGs shipped with languages, [including Java](http://stackoverflow.com/questions/453479/how-good-is-java-util-random). The statistical results have been verified in both PractRand and TestU01 by the authors.

xoroshiro128+ is designed to be the successor to xorshift128+, currently used in the JavaScript engines of Chrome, Firefox and Safari.

Both xorshift128+ and xoroshiro128+ have a period of 2<sup>128</sup> but xoroshiro128+ is **benchmarked by the authors as 20% faster and with 20% fewer failures in BigCrush than its predecessor.**

[![Clojars Project](https://img.shields.io/clojars/v/xoroshiro128.svg)](https://clojars.org/xoroshiro128)

[![CircleCI](https://circleci.com/gh/thedavidmeister/xoroshiro128.svg?style=svg)](https://circleci.com/gh/thedavidmeister/xoroshiro128)

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

```clojure
(require '[xoroshiro128.core :as x])
(def seed 12345)
(def my-rand-seq (map x/value (iterate x/next (x/xoroshiro128+ seed))))
(take 5 my-rand-seq)
; (5983371452340661002 -139170902943549277 2600980751997770790 3131701164191746090 -3375623441569470803)
````

Additionally, we can inspect any item in the sequence to extract the seed, allowing us to resume the sequence from that point later.

```clojure
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

## Jump function

The Xoroshiro128+ algorithm supports a jump function to easily create new non-overlapping sequences from any starting point.

Calling the `jump` function is equivalent to calling `next` 2<sup>64</sup> times.

As the total period of Xoroshiro128+ is 2<sup>128</sup> it is possible to call jump 2<sup>64</sup> times before needing to reseed a totally new sequence.

If you are using simple calls to `rand` there is a `jump-rand!` function provided to jump the state of `rand`.

```clojure
(require '[xoroshiro128.core :as x])
(def seed 55555)
(def my-rand (x/xoroshiro128+ seed))
; jumped-rand is equivalent to 2^64 calls to (x/next my-rand)
(def jumped-rand (x/jump my-rand))
````

## Correctness

The outputs of Splitmix64 next value, Xoroshiro128+ next value, and the Xoroshiro128+ jump function have all been verified against samples from the reference C implementation.

Dozens of values were generated from https://ideone.com/PuauK5 and fed directly into the expected outputs for the test suite.

## Performance

I did some basic benchmarking on my laptop using [criterium](https://github.com/hugoduncan/criterium) and found ~60% speed improvement using xoroshiro128+ vs. the default Java PRNG.

As always with benchmarking, YMMV.

I compared `(.nextLong (java.util.Random.))` against `(xoroshiro128.core/rand)`.

Results from `java.util.Random`:

````
Evaluation count : 802245600 in 60 samples of 13370760 calls.
Execution time mean : 75.230975 ns
Execution time std-deviation : 1.344983 ns
Execution time lower quantile : 73.255333 ns ( 2.5%)
Execution time upper quantile : 78.972937 ns (97.5%)
Overhead used : 1.710409 ns

Found 4 outliers in 60 samples (6.6667 %)
low-severe	 3 (5.0000 %)
low-mild	 1 (1.6667 %)
Variance from outliers : 7.7675 % Variance is slightly inflated by outliers
````

Results from `xoroshiro128.core/rand`:

````
Evaluation count : 2080459080 in 60 samples of 34674318 calls.
Execution time mean : 27.986250 ns
Execution time std-deviation : 0.429291 ns
Execution time lower quantile : 27.290695 ns ( 2.5%)
Execution time upper quantile : 28.888978 ns (97.5%)
Overhead used : 1.710409 ns

Found 5 outliers in 60 samples (8.3333 %)
low-severe	 5 (8.3333 %)
Variance from outliers : 1.6389 % Variance is slightly inflated by outliers
````

## Cryptography

xoroshiro128+ and the family of related generators are not cryptographically secure or intended for use in cryptography.

These generators are designed to produce a statistically uniform distribution at high speeds, with a reasonable period.

## License

The xoroshiro128+ algorithm reference implementation in C was developed by David Blackman and Sebastiano Vigna in 2016 under a Creative Commons public domain license https://creativecommons.org/publicdomain/zero/1.0/.

This clojure implementation is copyright © 2016 David Meister.

Distributed under the [Eclipse Public License version 1.0](http://www.eclipse.org/legal/epl-v10.html).
