# xoroshiro128

Clojure implementation of the xoroshiro128+ PRNG described at http://xoroshiro.di.unimi.it/

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

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
