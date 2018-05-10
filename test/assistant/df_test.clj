(ns assistant.df-test
  (:require [clojure.test :refer :all]
            [assistant.df :refer :all]))

(set! *print-meta* true)

(deftest df-test
  (testing "parse-arglist"
    (is (= {:arglist    '[a b c ^int f ^String g i {:keys [a b d], :as ^bool g}],
            :predicates '[(h i) (c d)]}
           (parse-arglist '[a b c (hint int f) (hint String g) (h i) {:keys [a b (c d)] :as (hint bool g)}])))
    (is (= {:arglist [] :predicates []} (parse-arglist '[])))
    (is (= {:arglist '[a] :predicates []} (parse-arglist '[a])))
    (is (= {:arglist '[a b] :predicates []} (parse-arglist '[a b])))
    (is (= {:arglist '[a] :predicates '[(int a)]} (parse-arglist '[(int a)])))
    (is (= {:arglist '[a b] :predicates '[(int a)]} (parse-arglist '[(int a) b])))
    (is (= {:arglist '[a b] :predicates '[(int a) (int b)]} (parse-arglist '[(int a) (int b)])))
    (is (= {:arglist '[a b] :predicates '[(int b)]} (parse-arglist '[a (int b)])))
    (is (= {:arglist '[[[[[[[[[^Something foo-bar]]]]]]]] bar-foo]
            :predicates '[(some-predicate bar-foo)]}
           (parse-arglist '[[[[[[[[[(hint Something foo-bar)]]]]]]]] (some-predicate bar-foo)])))
    (is (= {:arglist '[a [[b c ^bool f] :as d]] :predicates '[(int? a) (string? c) (foo? d)]}
           (parse-arglist '[(int? a) [[b (string? c) (hint bool f)] :as (foo? d)]])))))
