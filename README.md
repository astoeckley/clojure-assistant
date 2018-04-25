### Clojure Assistant

Simple but effective validation on any kind of data and types, without any fuss or learning curve.

The small amount of code in this library is short and decently documented, and the tests also make it clear. For example, [here is all you need to know about shaping maps](https://github.com/astoeckley/clojure-assistant/blob/master/src/assistant/structures.cljc#L12).

### Motivation

This library provides tools for validating data in a simple, lightweight manner using ordinary maps and functions, without a DSL or heavy API or extra syntax. It is designed to make it very easy to assert that the data flowing through your Clojure or ClojureScript app is exactly what you expect, using every-day Clojure constructs and without a lot of added code to do this. And it all compiles away at production, with zero run-time overhead.

It has similar goals, but with easier use (and fewer features), to Clojure.spec and many other similar libraries. I built this tiny library because I just wanted to easily verify that types are what I expect, and do so without significantly adding to the dynamic and elegant workflow of writing succinct Clojure. I also wanted every "spec" to be confirmed as existing at compile time, something which Clojure.spec does not do out of the box (and has bitten many a developer who mistyped keywords).

Clojure Assistant is just normal maps and functions.

### Installation

Clojars:  

```[nl.balconystudio/clojure-assistant "0.28"]```

Require in CLJ: 

```[assistant.structures ..(etc)..]```

```[assistant.asserts ...]```

```[assistant.predicates ...]```


Require macros in CLJS:

```[assistant.structures :as ass :include-macros true ..(etc)..]```


### PRs, Issues

Sure.

### License

Copyright © 2017 - 2018 Andrew Stoeckley and Balcony Studio

Distributed under the Eclipse Public License, the same as the Clojure language.
