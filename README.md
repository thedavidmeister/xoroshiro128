# xoroshiro128

Clojure implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/

The xoroshiro128+ algorithm was developed by David Blackman and Sebastiano Vigna in 2016 under a Creative Commons license.

The algorithm has been shown to be fast and produce superior statistical results to many PRNGs shipped with languages, including Java. The statistical results have been verified in both PractRand and TestU01 by the authors.

xoroshiro128+ is designed to be the successor to xorshift128+, currently used in the JavaScript engines of Chrome, Firefox and Safari.

Both xorshift128+ and xoroshiro128+ have a period of 2<sup>128</sup> but xoroshiro128+ is benchmarked as 20% faster and with 20% fewer failures in BigCrush than its predecessor.

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

This atom can be reset/seeded by calling `seed-rand!` and passing it a long. Internally, as per the suggestion in the reference implementation, this long will be passed through a splitmix64 generator to generate the two 64 bit seeds needed for the xoroshiro128+ algorithm.

In the simple usage

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
