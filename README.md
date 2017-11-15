# xoroshiro128+

This is a Clojure(Script) implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/

The algorithm has been shown to be fast and produce superior statistical results to many PRNGs shipped with languages, [including Java](http://stackoverflow.com/questions/453479/how-good-is-java-util-random). The statistical results have been verified in both PractRand and TestU01 by the authors.

xoroshiro128+ is designed to be the successor to xorshift128+, currently used in the JavaScript engines of Chrome, Firefox and Safari.

Both xorshift128+ and xoroshiro128+ have a period of 2<sup>128</sup> but xoroshiro128+ is **benchmarked by the authors as 20% faster and with 20% fewer failures in BigCrush than its predecessor.**

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/thedavidmeister/xoroshiro128.svg)](https://clojars.org/thedavidmeister/xoroshiro128)

[![CircleCI](https://circleci.com/gh/thedavidmeister/xoroshiro128.svg?style=svg)](https://circleci.com/gh/thedavidmeister/xoroshiro128)

## Usage

Everything is in `xoroshiro128.core`.

`(require '[xoroshiro128.core])`

### Simple

The simplest usage is to simply call `rand` to generate a random long.

This uses an atom to store the state of the prng, which is updated to the next value in sequence on each call to `rand`.

`rand` is automatically seeded by `java.util.Random` or `Math.random`, passed through splitmix64, and so should Just Work.

This atom can be manually reset/seeded (e.g. during testing) by calling `seed-rand!` and passing it a long. Internally, as per the suggestion in the reference implementation, this long will be passed through a splitmix64 generator to generate the two 64 bit seeds needed for the xoroshiro128+ algorithm. `seed-rand!` also accepts two arguments, which should both be longs to pass directly to xoroshiro128+ (no splitmix64 involved).

#### Parallel calls to `rand`

As of version `1.1.1`, calls to `rand` in parallel will use `compare-and-set!` to ensure that each value is returned only once across all threads. No guarantees about the ordering of `rand` are made for parallel execution.

For example, using `pmap` and `map` to produce two sequences of random numbers with `rand`, starting with the same seed, would:

- create the same _set_ of longs twice
- _not order_ the sequences in the same way, `pmap` will have incorrect ordering
- _not_ duplicate any values in `pmap` that aren't duplicated in `map`

See `xoroshiro128.test.state` for examples.

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
(def new-seed (x/seed my-rand-item));
; [-5785456751514194665 7961309068892779353]

; Create a new item from our extracted seed.
(def new-rand-item (apply x/xoroshiro128+ new-seed))
; If we seeded correctly, new-rand-item + 1 should be the same as my-rand-item + 1.
(x/value (x/next new-rand-item))
; 3486300715335445982
````

### Seeds

Seeds for the xoroshiro128+ algorithm must be 128 bit (as the name implies).

As clojure on the JVM supports 64 bit integers (longs) but not 128 bit integers, the seed is represented internally as two longs in a vector.

The situation is slightly more complex in clojurescript as JavaScript does _not_ provide native support for 64 bit integers, only 64 bit _floats_. The `google.math.Long` class from Google Closure is used to provide 64 bit integer support, and then 128 bit seed support using two longs as per the clojure implementation.

As mentioned above, if only a single long is available to seed the PRNG, the splitmix algorithm can be used to extrapolate further longs to use as a seed. For convenience `long->seed128` is provided convert a 64 bit seed into a 128 bit seed. This function is used internally when only a single long is provided.

It is worth noting that converting a 64 bit seed to a 128 bit seed using `long->seed128` is a deterministic process, i.e. any given long always provides the same seed. This means the pool of available seeds is 64 bits, and is not magically increased to 128 bits. Whether this matters or not is entirely contextual, but providing 128 bit seeds will drastically increase the size of the pool of available pseudo random sequences to draw upon.

As UUIDs represent 128 bit integers (hexadecimal), they are also supported for convenience as seed values both via. the `xoroshiro128+` function and `uuid->seed128`. Note that UUID generation functions leave a few bits of the UUID static to indicate the UUID version and variant. This means the seed pool for a given UUID generation function is both orders of magnitude larger than a single 64 bit seed, and smaller than two independant 64 bit seeds.

The current seed value can be extracted as a 128 bit seed vector from both `Xoroshiro128+` and `Splitmix64` type data with the `seed` function.

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

Dozens of values were generated from https://ideone.com/PuauK5 and fed directly into the expected outputs for the [test suite](https://github.com/thedavidmeister/xoroshiro128/blob/master/test/xoroshiro128/core_test.clj).

## Performance

All benchmarking code is available under `xoroshiro128.test.performance`.

### Clojure

I did some basic benchmarking on my laptop using [criterium](https://github.com/hugoduncan/criterium) and found ~66% speed improvement using xoroshiro128+ vs. the default Java PRNG.

As always with benchmarking, YMMV.

I compared `(.nextLong (java.util.Random.))` against `(xoroshiro128.core/rand)`.

Results from `java.util.Random`:

````
Evaluation count : 831204360 in 60 samples of 13853406 calls.
             Execution time mean : 70.719127 ns
    Execution time std-deviation : 0.615334 ns
   Execution time lower quantile : 69.942200 ns ( 2.5%)
   Execution time upper quantile : 72.194872 ns (97.5%)
                   Overhead used : 1.675261 ns

Found 2 outliers in 60 samples (3.3333 %)
	low-severe	 2 (3.3333 %)
 Variance from outliers : 1.6389 % Variance is slightly inflated by outliers
````

Results from `xoroshiro128.core/rand`:

````
Evaluation count : 2301527460 in 60 samples of 38358791 calls.
             Execution time mean : 24.654548 ns
    Execution time std-deviation : 0.398656 ns
   Execution time lower quantile : 24.329306 ns ( 2.5%)
   Execution time upper quantile : 25.960526 ns (97.5%)
                   Overhead used : 1.687464 ns

Found 5 outliers in 60 samples (8.3333 %)
	low-severe	 2 (3.3333 %)
	low-mild	 3 (5.0000 %)
 Variance from outliers : 2.0241 % Variance is slightly inflated by outliers
````

Given these results I think it's safe to recommend xoroshiro128+ as a mostly "drop in" replacement for `(.nextLong (java.util.Random.))`.

### ClojureScript

Unsurprisingly the performance of `goog.math.Long` is significantly worse across the board than native JavaScript floats.

I'm not aware of any benchmarking tool for CLJS that is as sophisticated as criterium, so I simply ran each function 1 million times and recorded the timestamps with `performance.now`.

Results:

```
LOG: '"benchmarking xoroshiro128.state/rand"'
LOG: '290.325'
LOG: '"benchmarking xoroshiro128.long-int/native-rand"'
LOG: '9787.36'
LOG: '"benchmarking Math.random"'
LOG: '18.715000000000146'
```

These numbers seem to wobble by around +/- 20% on subsequent runs.

We can see that xoroshiro128+ is ~16x slower than `Math.random` but it's a bit "apples vs. oranges" as `Math.random` produces floats between [0, 1] and xoroshiro128+ produces `goog.math.Long` objects across the full 64 bit integer range.

We see ~290ns per call (0.29s for 1 000 000 calls) in CLJS vs. ~25ns per call in CLJ. This puts the JVM at around 10x faster than JS for this particular use-case. This is also another "apples vs. oranges" comparison as the JVM and JS runtime environments are obviously very different.

To get "apples to apples" timings within JS (and to seed `rand`) I created a "native random long" function that works exactly like the internals of `random-uuid`, but stops halfway to return a single `goog.math.Long` instead of a full UUID:

```
; lifted from https://cljs.github.io/api/cljs.core/random-uuid
(let [hex #(.toString (rand-int 16) 16)]
 (goog.math.Long.fromString
  (apply str (take 16 (repeatedly hex)))
  16))
```

This approach is ~33x slower than xoroshiro128+ and ~540x slower than `Math.random` (due to the string manipulation, I assume).

Overall the use-cases for xoroshiro128+ in CLJS are not as clear cut as CLJ due to lack of native long support.

I recommend xoroshiro128+ when:

- Seeding the PRNG is important
- Working with the full range of 64 bit integers (`goog.math.Long`) is acceptible or even important
- Wanting to work against the `xoroshiro128.prng/IPRNG` protocol
- Compatibility with another system implementing xoroshiro128+ is important

I recommend `Math.random` when working with an _unseeded_ PRNG with an _undefined algorithm_ outputting only _a subset of all possible floats_, [specifically those between [0, 1]](https://lemire.me/blog/2017/02/28/how-many-floating-point-numbers-are-in-the-interval-01/) is acceptible.

I recommend `xoroshiro128.long-int/native-rand` when generating new seeds for xoroshiro128+ if UUID seeds are not suitable.

### CLJS optimizations & environment

CLJS benchmarks were conducted on Chrome with the `:advanced` compiler optimization flag as this should best represent usage in production deployments. Interestingly, advanced compilation made `Math.random` calls about 6x _slower_, and `goog.math.Long` based logic ~30-60% faster.

Fair warning that changing the browser and CLJS compilation optimisations level _drastically_ changes the absolute and relative benchmark timings - in some cases by 100% or more.

## Cryptography

xoroshiro128+ and the family of related generators are not cryptographically secure or intended for use in cryptography.

These generators are designed to produce a seedable, statistically uniform distribution at high speeds, with a reasonable period.

## License

The xoroshiro128+ algorithm reference implementation in C was developed by David Blackman and Sebastiano Vigna in 2016 under a Creative Commons public domain license https://creativecommons.org/publicdomain/zero/1.0/.

This clojure implementation is copyright © 2016 David Meister.

Distributed under the [Eclipse Public License version 1.0](http://www.eclipse.org/legal/epl-v10.html).

## Thanks

Thanks to [mscharley](https://github.com/mscharley) for helping to verify the outputs of the test suite.
