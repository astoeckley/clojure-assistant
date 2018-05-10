(ns assistant.df
  (:require [assistant.structures :refer [is-pack]]))

(defn parse-arglist
  "Walks an arg list and replaces any list with the last s-exp in the list, which must be a symbol (and the name of an argument binding). A special case for a list that begins with the keyword 'hint' which will add a type hint when replacing the list. The return is a map with this new arglist, and a vector of original removed lists.

i.e. If the input arglist is:

  [(int? a) [[b (string? c) (hint bool f)] :as (foo? d)]]: 

the output would be: 

  {:arglist [a [[b c ^bool f] :as d]] :predicates [(int? a) (string? c) (foo? d)]}."

  [arglist]
  
  {:pre  [(vector? arglist)]
   :post [(is-pack {:arglist vector? :predicates vector? #_ (fn [c] (and (vector? c) (every? list? c)))} %)]}

  (let [predicates (atom '[])
        ret        (clojure.walk/prewalk #(if (list? %)
                                            (do (assert (symbol? (last %)))
                                                (if (= 'hint (first %))
                                                  (do
                                                    (assert (= 3 (count %)))
                                                    (assert (symbol? (second %)))
                                                    (vary-meta (last %) assoc :tag (second %)))
                                                  (do
                                                    (swap! predicates conj %)
                                                    (last %))))
                                            %)
                                         arglist)]
    {:arglist ret :predicates @predicates}))


