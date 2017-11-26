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
          (pos? size)]
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
          (pos? size)]
   :post [(bool? %)]}
  (try
    (and (every? pred target) (= size (count target)))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

