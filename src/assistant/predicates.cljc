;;  Copyright (c) Andrew Stoeckley / Balcony Studio 2017 - 2018. All rights reserved.

;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the license file at the root directory of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns assistant.predicates)

;; Simple predicates to aid in creating elegant assertions and validations.
;; Returns false if targets are not eligible for these inspections (instead of throwing an exception).

;; The target value being tested is always the last argument

(defn bool?
  "Is x true or false"
  [x]
  {:post [(or (= true %) (= false %))]}
  (or (= true x) (= false x)))

(defn count?
  "Determines if sequence has expected size"
  [size target]
  {:pre  [(#?(:clj integer? :cljs int?) size)
          (or (pos? size) (zero? size))]
   :post [(bool? %)]}
  (try
    (= size (count target))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

(defn all?
  "Determines if a sequence is of expected size and all entries pass the same predicate"
  [size pred target]
  {:pre  [(#?(:clj integer? :cljs int?) size)
          (or (pos? size) (zero? size))]
   :post [(bool? %)]}
  (try
    (and (every? pred target) (= size (count target)))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

(defn same-size?
  "Are the provided collections the same count?"
  [& xs]
  {:post [(bool? %)]}
  (try
    (apply = (map count xs))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

(defn map-structure?
  "Verifies that a map has all keys of the same type and values of a same type."
  [keys-pred vals-pred target]
  {:post [(bool? %)]}
  (try
    (and
     (map? target)
     (every? keys-pred (keys target))
     (every? vals-pred (vals target)))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))


;; --------- Nilable data --------- 

(defn nil-or?
  "Returns true if target is nil or passes predicate."
  [pred target]
  {:pre [pred]}
  (try
    (or (nil? target) (pred target))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

(defn nilable
  "Wraps a predicate in a function that allows either the predicate to pass, or nil be allowed."
  [pred]
  {:pre [pred]}
  #(nil-or? pred %))

