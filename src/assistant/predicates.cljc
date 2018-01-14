(ns assistant.predicates)

;; Simple predicates to aid in creating elegant assertions. Returns false if targets are not eligible for
;; these inspections (instead of throwing an exception).

(defn bool?
  "Is x true or false"
  [x]
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
