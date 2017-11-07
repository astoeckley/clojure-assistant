# Clojure Assistant

Simple but effective validation on any kind of data and types, without any fuss or learning curve.

I don't have the time to write up a nice Readme, but the small amount of code in this library is quite well documented, and the tests also make it clear.

## Motivation

This library provides tools for validating data in a simple, lightweight manner using ordinary maps and functions, without a DSL or heavy API or extra syntax. It is designed to make it very easy to assert that data flowing through your Clojure or ClojureScript app is exactly what you expect, but without a lot of added code to do this. And it all compiles away at production, with zero run-time overhead.

It has similar goals, but with much easier use (and fewer features), to Clojure.spec and many other libraries out there. I built this tiny library because I just wanted to easily verify that types are what I expect, and do so without signifanctly adding to the dynamic and elegant workflow of writing succinct Clojure. And I didn't want a heavy macro-based library where the actions are a bit mysterious.

Clojure Assistant is just maps and functions for the most part.

## Installation

Download from http://example.com/FIXME.

## License

Copyright © 2017 Andrew Stoeckley

Distributed under the Eclipse Public License, the same as the Clojure language.