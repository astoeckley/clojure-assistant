;;  Copyright (c) Andrew Stoeckley / Balcony Studio 2017. All rights reserved.

;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the license file at the root directory of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns assistant.structures)

;; A 'pack' is a map of keys of any type to predicate functions or other packs.
;; A predicate accepts one argument and returns a truthy value. 
;; The predicates or packs specify the allowed data to be stored in the pack with each key.

;; Example pack:

;; (def cool-toy {:minimum-age (every-pred number? pos?) 
;;                :color keyword?})

;; (def two-toys {:toy1 cool-toy :toy2 cool-toy}

;; When packs are defined, the following functions and macros can test or assert that other maps pass these definitions.
;; Choose among true/false predicates or assertions that simply pass on the tested map after asserting it is correct.

;; The tools are:

;; (as-pack? cool-toy {:minimum-age 5 :color :red :other :entry}) => true

;; The as-pack? predicate returns true if the map at least meets the requirements, even if the map has additional keys other
;; than those specified in the pack. It can be used *as* that pack object.

;; (is-pack? cool-toy {:minimum-age 5 :color :red :other :entry}) => false

;; is-pack? is like as-pack? but with the additional restriction that there can be no extra keys. It *is* that object.

;; (as-pack cool-toy some-map)

;; This is not a predicate test, as-pack is an *assertion* that the map can be used as a cool-toy. If it passes, the map
;; simply flows through. If *assert* is false, this expression does nothing and just compiles to some-map. In ClojureScript,
;; pass ":elide-asserts true" as a compiler flag to turn asserts off. The assertions will show detailed errors that reveal the
;; full path to nested pack keys and their values that failed.

;; (is-pack cool-toy some-map)

;; Also an assertion, but is-pack tests in the same way as is-pack?

;; (defpack person {:name string? :age (every-pred number? pos?)})

;; Used like this, the macro simply creates a map as if by def instead of defpack. If a last argument of true is passed,
;; it will generate additional convenience functions which wrap as-pack? and is-pack? See more explanation below.

(defn keys-match?
  "Questions if the pack and the value have the exact same keys, with no extra keys in one that are not in the other."
  [pack v]
  {:pre  [(map? pack)]
   :post [(or (false? %) (true? %))]}
  (try
    (= (set (keys pack)) (set (keys v)))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

(defn pack?
  "Primarily used by as-pack? and is-pack?, this accepts a pack, a value to test, and a boolean compare-keys.
   If compare-keys is true, the test will also use keys-match?"
  [pack v compare-keys]
  {:pre  [(map? pack) (or (false? compare-keys) (true? compare-keys))]
   :post [(or (false? %) (true? %))]}
  (and
   (if compare-keys
     (keys-match? pack v)
     true)
   (try
     (every? (fn [[k func]]
               (if (map? func)
                 (pack? func (get v k) compare-keys)
                 (func (get v k)))) pack)
     (catch #?(:clj Exception
               :cljs :default)
         _ false))))

(defn as-pack?
  "Accepts a pack map and any other value and returns true if the value meets the specifications of the provided pack. 
   Note that a map might have additional keys not specified in the pack, and they are ignored. Use is-pack? to additionally 
   require that *only* the pack's keys are included in the provided value."
  [pack v]
  {:pre  [(map? pack)]
   :post [(or (false? %) (true? %))]}
  (pack? pack v false))

(defn is-pack?
  "Returns true if as-pack? is true and there are no additional keys in provided value."
  [pack v]
  {:pre  [(map? pack)]
   :post [(or (false? %) (true? %))]}
  (pack? pack v true))

(defn explanation?
  "A pack explanation is outlined in explain-pack, and this function checks that the explanation is in the correct format."
  [{:keys [invalid extra] :as explanation}]
  (and (map? explanation) (vector? invalid) (vector? extra) (= 2 (count explanation))))

(defn explain-pack
  "This is used by as-pack and is-pack macros to provide helpful assertion errors. Probably does not need to be called 
   directly, though it could be. Returns a map of :invalid and :extra vectors, each which contains k/v vector pairs from 
   the provided value that do not meet the specification of the pack map. The :invalid and :extra vectors might be empty, 
   but never nil. :invalid means the values do not pass the pack's predicates. :extra shows the additional entries not 
   covered by the pack. Nested packs are tested and :invalid and :extra may contain nested vectors which act as paths to
   the nested keys and values that failed the pack specification."
  [pack v]
  {:pre  [(map? pack)]
   :post [(explanation? %)]}
  (if (map? v)
    (let [{:keys [invalid extra]} (reduce
                                   (fn [{:keys [invalid extra] :as ret} [k func]]
                                     (let [found (get v k)]
                                       (if (map? func)
                                         (let [{invalids :invalid extras :extra} (explain-pack func found)]
                                           {:invalid
                                            (if (empty? invalids)
                                              invalid
                                              (conj invalid [k invalids]))
                                            :extra
                                            (if (empty? extras)
                                              extra
                                              (conj extra [k extras]))})
                                         (try
                                           (if (func found)
                                             ret
                                             {:invalid (conj invalid [k found]) :extra extra})
                                           (catch #?(:clj Exception
                                                     :cljs :default)
                                               _ {:invalid (conj invalid [k found]) :extra extra})))))
                                   {:invalid [] :extra []} pack)
          root-extra              (vec (remove (fn [[k v]] (get pack k)) v))]
      {:invalid invalid :extra (if (empty? extra) root-extra (into root-extra extra))})
    {:invalid (mapv #(do [% nil]) (keys pack)) :extra []}))

(defn explained-as-pack?
  "Receives an explanation from explain-pack and returns true if there are no invalid keys."
  [explanation]
  {:pre [(explanation? explanation)]}
  (empty? (:invalid explanation)))

(defn explained-is-pack?
  "Receives an explanation from explain-pack and returns true if there are no invalid keys and no extra keys."
  [explanation]
  {:pre [(explanation? explanation)]}
  (and (empty? (:invalid explanation))
       (empty? (:extra explanation))))

(defmacro assert-pack
  "Used by the as-pack and is-pack macros; should not be called directly.
   Similar to the two-arity version of assistant.asserts/as macro, but instead of a predicate function, a pack map is supplied, 
   which is just an ordinary map (as describe at top). When *assert* is on, it will assert that the value passes as if by 
   as-pack? or is-pack? depending on explain-fn, and then return it. If it does not pass, the assertion failure shows the 
   offending paths to keys and their values. If *assert* is off, as-pack does nothing and just passes through the value."
  [pack v explain-fn]
  (if *assert*
    `(let [ret#       ~v
           pack#      ~pack
           explained# (explain-pack pack# ret#)]
       (assert (~explain-fn explained#) (str "Paths not matching pack " '~pack
                                             " are: INVALID: " (:invalid explained#)
                                             " EXTRA: " (:extra explained#)))
       ret#)
    v))

(defmacro as-pack
  "assert-pack based on a test like as-pack?"
  [pack v]
  `(assert-pack ~pack ~v explained-as-pack?))

(defmacro is-pack
  "assert-pack based on a test like is-pack?"
  [pack v]
  `(assert-pack ~pack ~v explained-is-pack?))

(defmacro defpack
  "This is a convenience macro that generates 1 to 3 defs at once. Even if you don't need the defined functions, it can make 
   code more clear to explicitly show that the map you are creating will be used as a pack. The map is always generated; the 
   other functions are only created if 'extras' is true.

   For example:

   (defpack toy {:minimum-age pos? :color keyword?} true)

   Will create the following:

   (def toy {:minimum-age pos? :color keyword?})
   (defn as-toy? [v] (as-pack? toy v))
   (defn is-toy? [v] (is-pack? toy v))

   It will not create new macros. Because macros which emit other defmacros are tasks reserved for others. 
   (hint: ClojureScript files are supported; you can write a defpack in cljs source.)"
  [packname packmap & [extras]]
  {:pre [(symbol? packname) (or (= 'true extras) (= 'false extras) (= nil extras))]}
  (if extras
    (let [as-name?  (symbol (str "as-" packname "?"))
          is-name?  (symbol (str "is-" packname "?"))
          args-name (gensym "args")]
      `(let [pack# ~packmap]
         (assert (map? pack#) (str "defpack did not receive a map for " '~packname ". Instead: " (if (nil? pack#) "nil" pack#)))
         (def ~packname pack#)
         (defn ~as-name?
           [~args-name]
           (as-pack? ~packname ~args-name))
         (defn ~is-name?
           [~args-name]
           (is-pack? ~packname ~args-name))))
    `(let [pack# ~packmap]
       (assert (map? pack#) (str "defpack did not receive a map for " '~packname ". Instead: " (if (nil? pack#) "nil" pack#)))
       (def ~packname pack#))))
