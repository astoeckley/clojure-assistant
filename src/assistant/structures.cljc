;;  Copyright (c) Andrew Stoeckley / Balcony Studio 2017. All rights reserved.

;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the license file at the root directory of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns assistant.structures
  (:require [clojure.core]))

(defn as-pack?
  "A 'pack' is a map of keys of any type to predicate functions. A predicate accepts one argument and returns a truthy value. The predicates specify the allowed data to be stored in the pack with each key.

   Example pack:

   (def cool-toy {:minimum-age (every-pred number? pos?) 
                  :color keyword?})

   as-pack? accepts the pack map and any other value and returns true if the value meets the specifications of the provided pack. Note that a map might have additional keys not specified in the pack, and they are ignored. Use is-pack? to additionally require that *only* the pack's keys are included in the provided value."
  [pack v]
  {:pre  [(map? pack)]
   :post [(or (false? %) (true? %))]}
  (try
    (every? (fn [[k func]] (func (get v k))) pack)
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

(defn keys-match?
  [pack v]
  {:pre  [(map? pack)]
   :post [(or (false? %) (true? %))]}
  (try
    (= (set (keys pack)) (set (keys v)))
    (catch #?(:clj Exception
              :cljs :default)
        _ false)))

(defn is-pack?
  "Returns true if as-pack? is true and there are no additional keys in provided value."
  [pack v]
  {:pre  [(map? pack)]
   :post [(or (false? %) (true? %))]}
  (and (keys-match? pack v)
       (as-pack? pack v)))

(defn explanation?
  "A pack explanation is outlined in explain-pack, and this function checks that the explanation is in the correct format."
  [{:keys [invalid extra] :as explanation}]
  (and (map? explanation) (vector? invalid) (vector? extra) (= 2 (count explanation))))

(defn explain-pack
  "Returns a map of :invalid and :extra vectors, each which contains k/v vector pairs from the provided value that do not meet the specification of the pack map. The :invalid and :extra vectors might be empty, but never nil. :invalid means the values do not pass the pack's predicates. :extra shows the additional entries not covered by the pack."
  [pack v]
  {:pre  [(map? pack)]
   :post [(explanation? %)]}
  (if (map? v)
    (let [invalid (reduce
                   (fn [ret [k func]]
                     (let [found (get v k)]
                       (try
                         (if (func found)
                           ret
                           (conj ret [k found]))
                         (catch #?(:clj Exception
                                   :cljs :default)
                             _ (conj ret [k found])))))
                   [] pack)
          extra   (vec (remove (fn [[k v]] (get pack k)) v))]
      {:invalid invalid :extra extra})
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

(defmacro as-pack
  "Similar to the two-arity version of assistant.asserts/as macro, but instead of a predicate function, a pack map is supplied, which is just an ordinary map (see as-pack? doc string). When *assert* is on, it will assert that the value passes as-pack? and then return it. If it does not pass, the assertion failure shows the offending keys and values. If *assert* is off, as-pack does nothing."
  [pack v]
  (if *assert*
    `(let [ret#       ~v
           pack#      ~pack
           explained# (explain-pack pack# ret#)]
       (assert (explained-as-pack? explained#) (str "Entries not matching pack " '~pack " are: " (:invalid explained#)))
       ret#)
    v))

(defmacro is-pack
  "Like as-pack, but tests with is-pack? instead of as-pack?"
  [pack v]
  (if *assert*
    `(let [ret#       ~v
           pack#      ~pack
           explained# (explain-pack pack# ret#)]
       (assert (explained-is-pack? explained#) (str "Entries not matching pack " '~pack
                                                    " are: INVALID: " (:invalid explained#)
                                                    " EXTRA: " (:extra explained#)))
       ret#)
    v))

(defmacro defpack
  "This is a convenience macro that generates several defs at once.

   For example:

   (defpack toy {:minimum-age pos? :color keyword?})

   Will create the following:

   (def toy {:minimum-age pos? :color keyword?})
   (defn as-toy? [v] (as-pack? toy v))
   (defn is-toy? [v] (is-pack? toy v))

   It will also create the macros as-toy and is-toy, similarly."

  [packname packmap]
  {:pre [symbol? packname]}
  (let [as-name?  (symbol (str "as-" packname "?"))
        is-name?  (symbol (str "is-" packname "?"))
        as-name   (symbol (str "as-" packname))
        is-name   (symbol (str "is-" packname))
        args-name (gensym "args")]
    `(let [pack# ~packmap]
       (assert (map? pack#) (str "defpack did not receive a map for " '~packname ". Instead: " (if (nil? pack#) "nil" pack#)))
       (def ~packname pack#)
       (defn ~as-name?
         [~args-name]
         (as-pack? ~packname ~args-name))
       (defn ~is-name?
         [~args-name]
         (is-pack? ~packname ~args-name))
       (defmacro ~is-name
         [~args-name]
         `(is-pack ~'~packname ~~args-name))
       (defmacro ~as-name
         [~args-name]
         `(as-pack ~'~packname ~~args-name)))))