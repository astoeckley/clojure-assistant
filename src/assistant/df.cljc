;;  Copyright (c) Andrew Stoeckley / Balcony Studio 2017 - 2018. All rights reserved.

;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the license file at the root directory of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns assistant.df
  (:require [assistant.structures :refer [is-pack]]
            [assistant.predicates :refer [nil-or?]]))

;;; This file defines macros for removing repitition when making pre conditions for every argument, and allowing the predicate to appear in the argument vector of the defn or fn directly, including type hints

;; A convenience for tersely requiring an argument to not be nil
(def nn #(not (nil? %)))

(defn parse-arglist
  "Walks an arg list and replaces any list with the last s-exp in the list, which must be a symbol that will become the name of the argument. A special case for a list that begins with the keyword 'hint' which will add a type hint when replacing the list. The return is a map with this new arglist, and a vector of original removed lists.
i.e. If the input arglist is:
  [(int? a) [[b (string? c) (hint bool f)] (foo? d)]]: 
the output would be: 
  {:arglist [a [[b c ^bool f] d]] :predicates [(int? a) (string? c) (foo? d)]}."
  [arglist]
  {:pre  [(vector? arglist)]
   :post [(is-pack {:arglist vector? :predicates vector? #_ (fn [c] (and (vector? c) (every? list? c)))} %)]}
  (let [predicates (atom '[])
        ret        (clojure.walk/prewalk #(if (list? %)
                                            (do (assert (symbol? (last %)))
                                                (if (= 'hint (first %))
                                                  (do (assert (= 3 (count %)))
                                                      (assert (symbol? (second %)))
                                                      (vary-meta (last %) assoc :tag (second %)))
                                                  (do (swap! predicates conj %)
                                                      (last %))))
                                            %)
                                         arglist)]
    {:arglist ret :predicates @predicates}))

(defn- is-pre-or-post?
  "Determines if the provided form is a :pre/:post map for a function body."
  [m]
  (and (map? m)
       (clojure.set/subset? (set (keys m)) #{:pre :post})))

(defn write-function
  "Returns the quoted syntax for a defn or fn, by parsing the arglist as described in parse-arglist and inserting the assertions. The body is the entire function body, which may optionally include pre/post conditions. The assertions will be inserted after the pre/post if present. fn-name can be nil in the case of a fn, but not a defn. Docstring may be nil."
  [defn-or-fn fn-name docstring args body]
  {:pre [(#{:defn :fn} defn-or-fn)
         (if (= :defn defn-or-fn) (symbol? fn-name) (nil-or? symbol? fn-name))
         (nil-or? string? docstring)
         (vector? args)
         (seq? body)]}
  (let [leader                       (cond (= :defn defn-or-fn) ['defn fn-name]
                                           (nil? fn-name)       ['fn]
                                           :else                ['fn fn-name])
        {:keys [arglist predicates]} (parse-arglist args)
        has-pre-post?                (is-pre-or-post? (first body))]
    `(~@leader 
      ~@(if docstring [docstring] [])
      ~arglist
      ~(if has-pre-post? (first body) '(comment "No pre/post conditions."))
      ~@(for [p predicates] `(assert ~p (str "Failed argument for " ~defn-or-fn " " '~fn-name " " '~p)))
      ~@(if has-pre-post? (rest body) body))))

(defmacro dfn
  "Makes a defn definition, parsing the arglist as described in parse-arglist."
  [fn-name & fn-contents]
  {:pre [(symbol? fn-name)]}
  (assert (seq fn-contents) (str "Incomplete dfn definition for " fn-name))
  (let [docstring (if (string? (first fn-contents)) (first fn-contents) nil)
        args      (if docstring
                    (do (assert (vector? (second fn-contents)) (str "No arglist provided after docstring for dfn " fn-name))
                        (second fn-contents))
                    (do (assert (vector? (first fn-contents)) (str "No arglist provided for dfn " fn-name))
                        (first fn-contents)))
        body      (if docstring (drop 2 fn-contents) (drop 1 fn-contents))]
    (write-function :defn fn-name docstring args body)))

(defmacro df
  "Makes a fn definition, parsing the arglist as described in parse-arglist. fn name is optional. Like clojure.core/fn, will not accept a docstring."
  [& fn-contents]
  (assert (seq fn-contents) (str "Incomplete fn definition"))
  (let [fn-name (if (symbol? (first fn-contents)) (first fn-contents) nil)
        args    (if fn-name
                  (do (assert (vector? (second fn-contents)) (str "No arglist provided for named fn " fn-name))
                      (second fn-contents))
                  (do (assert (vector? (first fn-contents)) "No arglist provided for anonymous fn")
                      (first fn-contents)))
        body    (if fn-name (drop 2 fn-contents) (drop 1 fn-contents))]
    (write-function :fn fn-name nil args body)))
