;;  Copyright (c) Andrew Stoeckley / Balcony Studio 2017. All rights reserved.

;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the license file at the root directory of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns assistant.asserts
  (:require [clojure.core]))

;; This macro allows ClojureScript code to access the compile-time value of *assert*
(defmacro asserts? [] *assert*)

(defmacro when-asserts [& forms] (when *assert* `(do ~@forms nil)))

(defmacro as
  
  "A better assert. Two arities are possible:
  [a & [b]]
  1. (as some-expression-or-value)
  or
  2. (as some-predicate-fn some-expression-or-value)

   If *assert* is off, this is a no-op -- it just passes through the expression-or-value ('a' if there is no 'b', or 'b'). In ClojureScript, just set :elide-asserts to true as a compiler flag in your project.clj to turn off these assertions.

   If *assert* is on and there is no predicate-fn, it will just assert the expression and then return it (unlike normal assert which always returns nil). Useful for making sure your gets and get-ins and keyword functions are actually returning something non-nil, but without having to separately bind then assert them. 

   If there is a predicate it will first assert the (a b) expression, then return just 'b'. If the predicate fails, then the expression, it's evaluated value, and the predicate are all shown in the assertion message.

   Examples:

   Ensure map entry exists or is not nil (will either return it or throw an assertion failure):
   (as (get some-map :some-key))

   Ensure a value is an int and is odd:
   (as (every-pred int? odd?) (get some-map :some-number))

   Ensure a value is 35:
   (as #(= 35 %) some-value-or-expression)"
  
  [a & [b :as rests]]
  (let [num-b (count rests)]
    (when (> num-b 1) (assert false "Your 'as' has too many arguments."))
    (if *assert*
      (if (= 1 num-b)
        `(let [ret#   ~b
               valid# (~a ret#)]
           (assert valid# (str "Expression " '~b
                               " evals to " (if (nil? ret#) "nil" ret#)
                               " which fails " '~a))
           ret#)
        `(let [ret# ~a]
           (assert ret# (str "Expression " '~a
                             " is " (if (nil? ret#) "nil" ret#)))
           ret#))
      (if (= 1 num-b) b a))))

