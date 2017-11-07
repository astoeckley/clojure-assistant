(ns assistant.asserts-test
  (:require [clojure.test :refer :all]
            [assistant.asserts :refer :all]))

(set! *assert* true)

(deftest asserts-on
  (testing "No predicates"
    (is (= 4 (as 4)))
    (is (= :hello (as :hello)))
    (is (= "string" (as "string")))
    (is (= 9 (as (+ 4 5))))
    (is (true? (as true)))
    (is (= true (as true)))
    (is (= {:a 1 :b 1} (as {:a 1 :b 1})))
    (is (= :a (as (#{:a :b} :a))))
    (is (thrown? AssertionError (as nil)))
    (is (thrown? AssertionError (as false)))
    (is (thrown? AssertionError (as (#{:a :b} :c)))))
  (testing "Valid false and nil values, using explicit predicate tests for false and nil"
    (is (= false (as #(false? %) false)))
    (is (= false (as false? false)))
    (is (nil? (as nil? (get {} :a))))
    (is (nil? (as nil? nil)))
    (is (= nil (as nil? (next ()))))
    (is (nil? (as #(or (nil? %) (false? %)) nil)))
    (is (false? (as #(or (nil? %) (false? %)) false))))
  (testing "Number predicates"
    (is (= 5 (as number? 5)))
    (is (= 3 (as number? (+ 1 2))))
    (is (= 0.132 (as float? 0.132)))
    (is (thrown? AssertionError (as float? 1))))
  (testing "String predicates"
    (is (= "abcdef" (as string? (str "abc" "def"))))
    (is (= "abcdef" (as (every-pred string? #(= "abcdef" %)) (str "abc" "def"))))
    (is (thrown? AssertionError (as string? (+ 1 4))))
    (is (thrown? AssertionError (as keyword? (str "abc" "def")))))
  (testing "Gets and Get-ins"
    (is (= 5 (as integer? (get {:a 5} :a))))
    (is (= "hello" (as (every-pred string? #(< (count %) 6))
                       (get-in {:a {:a {:b "hello"}}} [:a :a :b]))))
    (is (thrown? AssertionError (as (every-pred string? #(< (count %) 6))
                                    (get-in {:a {:a {:b "hello"}}} [:a :a :c]))))
    (is (thrown? AssertionError (as (every-pred string? #(< (count %) 6))
                                    (get-in {:a {:a {:b "hello there"}}} [:a :a :b]))))))


(set! *assert* false)

(deftest asserts-off
  "Run above tests, but replace assertion expectations with pass-through of values, even if fail predicates."
  (testing "No predicates"
    (is (= 4 (as 4)))
    (is (= :hello (as :hello)))
    (is (= "string" (as "string")))
    (is (= 9 (as (+ 4 5))))
    (is (true? (as true)))
    (is (= true (as true)))
    (is (= {:a 1 :b 1} (as {:a 1 :b 1})))
    (is (= :a (as (#{:a :b} :a))))
    (is (nil? (as nil)))
    (is (false? (as false)))
    (is (nil? (as (#{:a :b} :c)))))
  (testing "Valid false and nil values, using explicit predicate tests for false and nil"
    (is (= false (as #(false? %) false)))
    (is (= false (as false? false)))
    (is (nil? (as nil? (get {} :a))))
    (is (nil? (as nil? nil)))
    (is (= nil (as nil? (next ()))))
    (is (nil? (as #(or (nil? %) (false? %)) nil)))
    (is (false? (as #(or (nil? %) (false? %)) false))))
  (testing "Number predicates"
    (is (= 5 (as number? 5)))
    (is (= 3 (as number? (+ 1 2))))
    (is (= 0.132 (as float? 0.132)))
    (is (= 1 (as float? 1))))
  (testing "String predicates"
    (is (= "abcdef" (as string? (str "abc" "def"))))
    (is (= "abcdef" (as (every-pred string? #(= "abcdef" %)) (str "abc" "def"))))
    (is (= 5 (as string? (+ 1 4))))
    (is (= "abcdef" (as keyword? (str "abc" "def")))))
  (testing "Gets and Get-ins"
    (is (= 5 (as integer? (get {:a 5} :a))))
    (is (= "hello" (as (every-pred string? #(< (count %) 6))
                       (get-in {:a {:a {:b "hello"}}} [:a :a :b]))))
    (is (= nil (as (every-pred string? #(< (count %) 6))
                   (get-in {:a {:a {:b "hello"}}} [:a :a :c]))))
    (is (= "hello there" (as (every-pred string? #(< (count %) 6))
                             (get-in {:a {:a {:b "hello there"}}} [:a :a :b]))))))

;; Turn them back on
(set! *assert* true)
