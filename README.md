### Clojure Assistant

Simple but effective validation on any kind of data and types, without any fuss or learning curve. I use this in every project to streamline the inclusion of pre conditions on all functions (named or anonymous), and to provide strict enforcement of values and map structures throughout a codebase. Also offers easy logging of expressions and their evaluations to console or repl. All tools herein work in Clojure and ClojureScript with zero overhead in a production build, and with a simple function-based, data-based syntax that does not rely on any new DSL, fancy magic, or verbose spec composition.

The small amount of code in this library is short and decently documented, and the tests also make it clear. For example, [here is all you need to know about shaping maps](https://github.com/stoeckley/clojure-assistant/blob/master/src/assistant/structures.cljc#L13).

### Motivation
#### (and comparison to clojure.spec)

I built this tiny library to add much safety without upsetting the terse, dynamic workflow of writing succinct Clojure.

In the absence of static types, I want to easily and quickly validate all data flows in a simple, lightweight manner using ordinary maps and functions, without a DSL or heavy API or extra syntax and verbosity (i.e. clojure.spec). 

I also want **compile-time enforcement that my "specs" actually exist and that my typos are actually caught** -- a fundamental expectation of a validation tool which clojure.spec doesn't offer (and has bitten many a developer who mistyped spec keywords and thus experienced passing validations on invalid data!). 

I also want short, targeted error messages if data doesn't conform.

This library addresses these concerns, and makes it easy to assert that all values flowing through a Clojure or ClojureScript app are exactly what you expect, using every-day Clojure constructs and with as little extra code as possible. It all compiles away at production, with zero run-time overhead.

### Installation

Clojars:  

```[nl.balconystudio/clojure-assistant "0.31"]```

Require in CLJ: 

```[assistant.structures ..(etc)..]```

The other namespaces are:

```[assistant.asserts ...]```
```[assistant.predicates ...]```
```[assistant.functions ...]```
```[assistant.log ...]```

Require macros in CLJS:

```[assistant.structures :as ass :include-macros true ..(etc)..]```

(for any namespace which has macros)

### PRs, Issues

Sure.

### License

Copyright © 2017 - 2018 Andrew Stoeckley and Balcony Studio

Distributed under the Eclipse Public License, the same as the Clojure language.
