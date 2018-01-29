(ns assistant.structures-test
  (:require [clojure.test :refer :all]
            [assistant.structures :refer :all]))

(def pack1 {:a number?
            :b float?
            :c string?
            :d (every-pred pos? #(< % 10))})

(def pack2 {:pack pack1})

(defpack pack3 {:pack2 pack2 :pack1 pack1})

(def no-explain {:invalid [] :extra []})
(def pack1-is1 {:a 1 :b 1.0 :d 1 :c "hello"})
(def pack1-is2 {:d 9.9 :b -0.001 :a 1000.1 :c ""})
(def pack1-as1 {:a 1 :b 1.0 :d 1 :c "hello" :e :whoa})
(def explain-pack1-as1 {:invalid [] :extra [[:e :whoa]]})
(def pack1-as2 {:d 9.9 :b -0.001 :a 1000.1 :c "" :f ""})
(def explain-pack1-as2 {:invalid [] :extra [[:f ""]]})
(def pack1-not1 {:a 1 :b 1 :d 0 :c ""})
(def pack1-not2 {})
(def pack1-not3 {:a 1 :b 1 :d 1 :c ""})
(def explain-pack1-is1 {:invalid [[:b 1]] :extra []})
(def pack2-not {:pack pack1-not3})
(def explain-pack1-is2 {:invalid [[:pack [[:b 1]]]] :extra []})
(def pack2-not2 {:pack (assoc pack1-not3 :foo :bar)})
(def explain-pack1-is3 {:invalid [[:pack [[:b 1]]]] :extra [[:pack [[:foo :bar]]]]})

(defpack toy {:minimum-age pos? :color keyword?
              :size        #(and (<= % 10) (>= % 5))
              :name        string?}
  true)

(defpack toys {:toy1 toy :toy2 toy}
  true)

(defn map-size-3?
  "Is true if the value is a map with three elements"
  [m]
  (and (map? m)
       (= 3 (count m))))

(defpack testing-size {:map map-size-3?})

(set! *assert* true)

(deftest asserts-on
  (testing "as-pack?"
    (is (as-pack? {#{1 2} #{:a :b}} {#{2 1} :b :c 99}))
    (is (as-pack? pack1 pack1-is1))
    (is (as-pack? pack1 pack1-is2))
    (is (as-pack? pack1 (dissoc pack1-is1 :e)))
    (is (as-pack? pack1 (dissoc pack1-as1 :e)))
    (is (as-pack? pack1 pack1-as1))
    (is (as-pack? pack1 pack1-as2))
    (is (as-pack? pack1 (merge pack1-is1 pack1-is2)))
    (is (as-pack? pack1 (merge pack1-as1 pack1-is2)))
    (is (false? (as-pack? pack1 nil)))
    (is (false? (as-pack? pack1 {:a 1})))
    (is (false? (as-pack? pack1 (dissoc pack1-is1 :d))))
    (is (false? (as-pack? pack1 pack1-not1)))
    (is (false? (as-pack? pack1 pack1-not2)))
    (is (thrown? AssertionError (as-pack? nil pack1-is1)))
    (is (as-pack? pack2 {:pack pack1-is1}))
    (is (as-pack? pack2 {:pack pack1-is2}))
    (is (as-pack? pack2 {:pack pack1-as1}))
    (is (as-pack? pack2 {:pack pack1-as2}))
    (is (false? (as-pack? pack2 {:whatever pack1-is2})))
    (is (false? (as-pack? pack2 {:pack {}})))
    (is (false? (as-pack? pack2 {:pack pack2})))
    (is (as-pack? pack3 {:pack1 pack1-is1 :pack2 {:pack pack1-is1}}))
    (is (as-pack? pack3 {:pack2 {:pack pack1-as2} :pack1 pack1-is2}))
    (is (false? (as-pack? pack3 {:pack {:pack pack1-as2} :pack1 pack1-is2})))
    (is (false? (as-pack? pack3 {:pack2 {:pack (dissoc pack1-is1 :d)} :pack1 pack1-is2})))
    (is (false? (as-toys? {:a 1})))
    (is (false? (as-toys? {:toy1 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}
                           :toy2 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}})))
    (is (true? (as-toys? {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
                          :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue :foo :bar}})))
    (is (false? (as-pack? toys {:toy1 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}
                                :toy2 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}})))
    (is (true? (as-pack? toys {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
                               :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue :foo :bar}}))))
  (testing "testing size"
    (is (is-pack testing-size {:map {:a 1 :b 2 :c 3}}))
    (is (false? (is-pack? testing-size {:map {:a 1 :b 2 :c 3 :d 4}}))))
  (testing "keys-match?"
    (is (keys-match? pack1 pack1-is1))
    (is (keys-match? pack1 pack1))
    (is (keys-match? pack1 pack1-is2))
    (is (false? (keys-match? pack1 pack1-as1)))
    (is (false? (keys-match? pack1 pack1-as2)))
    (is (thrown? AssertionError (keys-match? [] pack1)))
    (is (thrown? AssertionError (keys-match? nil nil)))
    (is (thrown? AssertionError (keys-match? nil pack1)))
    (is (false? (keys-match? pack1 nil)))
    (is (false? (keys-match? pack1 9)))
    (is (keys-match? pack1 pack1-not1))
    (is (false? (keys-match? pack1 pack1-not2))))
  (testing "is-pack?"
    (is (is-pack? {#{1 2} #{:a :b}} {#{2 1} :b}))
    (is (is-pack? pack1 pack1-is1))
    (is (is-pack? pack1 pack1-is2))
    (is (is-pack? pack1 (dissoc pack1-is1 :e)))
    (is (is-pack? pack1 (dissoc pack1-as1 :e)))
    (is (is-pack? pack1 (dissoc pack1-as2 :f)))
    (is (false? (is-pack? pack1 pack1-as1)))
    (is (false? (is-pack? pack1 pack1-as2)))
    (is (is-pack? pack1 (merge pack1-is1 pack1-is2)))
    (is (false? (is-pack? pack1 (merge pack1-as1 pack1-is2))))
    (is (is-pack? pack1 (dissoc (merge pack1-as1 pack1-is2) :e)))
    (is (false? (is-pack? pack1 nil)))
    (is (false? (is-pack? pack1 {:a 1})))
    (is (false? (is-pack? pack1 (dissoc pack1-is1 :d))))
    (is (false? (is-pack? pack1 (dissoc pack1-is1 :a))))
    (is (false? (is-pack? pack1 (dissoc pack1-is1 :d :b))))
    (is (false? (is-pack? pack1 pack1-not1)))
    (is (false? (is-pack? pack1 pack1-not2)))
    (is (thrown? AssertionError (is-pack? nil pack1-is1)))
    (is (is-pack? pack3 {:pack1 pack1-is1 :pack2 {:pack pack1-is1}}))
    (is (false? (is-pack? pack3 {:a {} :pack2 {:pack pack1-is1} :pack1 pack1-is2})))
    (is (false? (is-pack? pack3 {:pack1 pack1-is1 :pack pack1-is1 :pack2 {:pack pack1-is1}})))
    (is (is-pack? pack3 {:pack2 {:pack pack1-is1} :pack1 pack1-is2}))
    (is (false? (is-pack? pack3 {:a nil :pack2 {:pack pack1-is1} :pack1 pack1-is2})))
    (is (false? (is-pack? pack3 {:pack {:pack pack1-as2} :pack1 pack1-is2})))
    (is (false? (is-pack? pack3 {:pack2 {:pack (dissoc pack1-is1 :d)} :pack1 pack1-is2})))
    (is (false? (is-toys? {:toy1 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}
                           :toy2 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}})))
    (is (false? (is-toys? {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
                           :toy2 {:minimum-age 5 :name "bobby" :size 8 :color :red :foo :bar}})))
    (is (true? (is-toys? {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
                          :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}})))
    (is (false? (is-pack? toys {:toy1 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}
                                :toy2 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}})))
    (is (false? (is-pack? toys {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
                                :toy2 {:minimum-age 5 :name "bobby" :size 8 :color :red :foo :bar}})))
    (is (true? (is-pack? toys {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
                               :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}}))))
  (testing "explain-pack"
    (is (= no-explain (explain-pack pack1 pack1-is1)))
    (is (= no-explain (explain-pack pack1 pack1-is2)))
    (is (= explain-pack1-as1 (explain-pack pack1 pack1-as1)))
    (is (= explain-pack1-as2 (explain-pack pack1 pack1-as2)))
    (is (= explain-pack1-is1 (explain-pack pack1 pack1-not3)))
    (is (= explain-pack1-is2 (explain-pack pack2 pack2-not)))
    (is (= explain-pack1-is3 (explain-pack pack2 pack2-not2))))
  (testing "as-pack"
    (is (as-pack pack1 pack1-is1))
    (is (= pack1-is1 (as-pack pack1 pack1-is1)))
    (is (= pack1-is2 (as-pack pack1 pack1-is2)))
    (is (= pack1-as1 (as-pack pack1 pack1-as1)))
    (is (= pack1-as2 (as-pack pack1 pack1-as2)))
    (is (thrown? AssertionError (as-pack pack1 {})))
    (is (thrown? AssertionError (as-pack pack1 nil)))
    (is (thrown? AssertionError (as-pack pack1 99)))
    (is (= {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
            :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue :b :b}}
           (as-pack toys {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
                          :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue :b :b}})))
    (is (= {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
            :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}}
           (as-pack toys {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
                          :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}})))
    (is (thrown? AssertionError (as-pack toys {:toy1 {:minimum-age 5 :name "andrew" :size :foo :color :red :foo :bar}
                                               :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}}))))
  (testing "is-pack"
    (is (is-pack pack1 pack1-is1))
    (is (= pack1-is1 (is-pack pack1 pack1-is1)))
    (is (= pack1-is2 (is-pack pack1 pack1-is2)))
    (is (thrown? AssertionError (is-pack pack1 pack1-as1)))
    (is (thrown? AssertionError (is-pack pack1 pack1-as2)))
    (is (thrown? AssertionError (is-pack pack1 {})))
    (is (thrown? AssertionError (is-pack pack1 nil)))
    (is (thrown? AssertionError (is-pack pack1 99)))
    (is (not= 5 (clojure.walk/macroexpand-all '(is-pack pack1 5))))
    (is (thrown? AssertionError (is-pack toys {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
                                               :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}})))
    (is (= {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
            :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}}
           (is-pack toys {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
                          :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}}))))
  (testing "defpack"
    (is (map? toy))
    (is (fn? as-toy?))
    (is (fn? is-toy?))
    (is (false? (as-toy? {:a 1})))
    (is (false? (as-toy? {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar})))
    (is (true? (as-toy? {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar})))
    (is (true? (is-toy? {:minimum-age 5 :name "andrew" :size 8 :color :red})))
    (is (false? (is-toy? {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar})))))

(defpack address {:street string? :city string? :state (every-pred string? #(= 2 (count %)))
                  :zip    (every-pred string? #(= 5 (count %)))}
  true)
(def person {:address address
             :name    string?
             :age     (every-pred pos? integer?)
             :height  (every-pred pos? number?)})
(defpack two-people {:one person :two person})

(deftest asserts-on-nested
  (testing "nested people"
    (is (is-address? {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}))
    (is (is-pack? address {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}))
    (is (false? (is-address? {:street "Foo St." :city "Foo City" :state "ME" :zip "123456"})))
    (is (false? (is-pack? address {:street "Foo St." :city "Foo City" :state "ME" :zip "123456"})))
    (is (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}
                          :name    "Andrew"
                          :age     1
                          :height  1}))
    (is (false? (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}
                                  :name    "Andrew"
                                  :age     1
                                  :height  0})))
    (is (false? (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "ME" :zip "123456"}
                                  :name    "Andrew"
                                  :age     1
                                  :height  1})))
    (is (false? (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "MEi" :zip "12345"}
                                  :name    "Andrew"
                                  :age     1
                                  :height  1})))
    (is (= {:invalid [[:address [[:street 5]]]] :extra [[:address [[:extra 99]]]]}
           (explain-pack person {:address {:street 5 :city "hi" :state "hi" :zip "abcde" :extra 99}
                                 :name    "hi" :age 1 :height 1000000000000})))
    (is (thrown? AssertionError (is-pack address {:street 5 :city "hi" :state "hi" :zip "abcde" :extra 99})))
    (is (thrown? AssertionError (is-pack person {:address {:street "Foo St." :city "Foo City" :state "MEi" :zip "12345"}
                                                 :name    "Andrew"
                                                 :age     1
                                                 :height  1})))
    (is (= {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
            :name    "hi" :age 1 :height 1000000000000}
           (as-pack person {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                            :name    "hi" :age 1 :height 1000000000000})))
    (is (thrown? AssertionError (is-pack person {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                                 :name    "hi" :age 1 :height 1000000000000})))
    (is (true? (as-pack? two-people {:one {:name    "" :age 1 :height 1
                                           :address {:a      1  :b     1    :c   :hi
                                                     :city   "lllll1123235"
                                                     :street "" :state "aa" :zip "12345"}}
                                     :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                           :name    "hi" :age 1 :height 1000000000000}})))
    (is (false? (is-pack? two-people {:one {:name    "" :age 1 :height 1
                                            :address {:a      1  :b     1    :c   :hi
                                                      :city   "lllll1123235"
                                                      :street "" :state "aa" :zip "12345"}}
                                      :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                            :name    "hi" :age 1 :height 1000000000000}})))
    (is (= {:one {:name    "" :age 1 :height 1
                  :address {:a      1  :b     1    :c   :hi
                            :city   "lllll1123235"
                            :street "" :state "aa" :zip "12345"}}
            :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                  :name    "hi" :age 1 :height 1000000000000}}
           (as-pack two-people {:one {:name    "" :age 1 :height 1
                                      :address {:a      1  :b     1    :c   :hi
                                                :city   "lllll1123235"
                                                :street "" :state "aa" :zip "12345"}}
                                :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                      :name    "hi" :age 1 :height 1000000000000}})))
    (is (= {:one {:name    "" :age 1 :height 1
                  :address {:city   "lllll1123235"
                            :street "" :state "aa" :zip "12345"}}
            :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde"}
                  :name    "hi" :age 1 :height 1000000000000}}
           (is-pack two-people {:one {:name    "" :age 1 :height 1
                                      :address {:city   "lllll1123235"
                                                :street "" :state "aa" :zip "12345"}}
                                :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde"}
                                      :name    "hi" :age 1 :height 1000000000000}})))
    (is (thrown? AssertionError (is-pack two-people {:one {:name    "" :age 1 :height 0
                                                           :address {:a      1  :b     1    :c   :hi
                                                                     :city   "lllll1123235"
                                                                     :street "" :state "aa" :zip "12345"}}
                                                     :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                                           :name    "hi" :age 1 :height 1000000000000}})))
    (is (thrown? AssertionError (as-pack two-people {:one {:name    "" :age 1 :height 1
                                                           :address {:a      1  :b     1    :c   :hi
                                                                     :city   "lllll1123235"
                                                                     :street "" :state "aa" :zip 9}}
                                                     :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde"}
                                                           :name    "hi" :age 1 :height 1000000000000}})))))

(set! *assert* false)

(deftest asserts-off
  (testing "as-pack?"
    (is (as-pack? {#{1 2} #{:a :b}} {#{2 1} :b :c 99}))
    (is (as-pack? pack1 pack1-is1))
    (is (as-pack? pack1 pack1-is2))
    (is (as-pack? pack1 (dissoc pack1-is1 :e)))
    (is (as-pack? pack1 (dissoc pack1-as1 :e)))
    (is (as-pack? pack1 pack1-as1))
    (is (as-pack? pack1 pack1-as2))
    (is (as-pack? pack1 (merge pack1-is1 pack1-is2)))
    (is (as-pack? pack1 (merge pack1-as1 pack1-is2)))
    (is (false? (as-pack? pack1 nil)))
    (is (false? (as-pack? pack1 {:a 1})))
    (is (false? (as-pack? pack1 (dissoc pack1-is1 :d))))
    (is (false? (as-pack? pack1 pack1-not1)))
    (is (false? (as-pack? pack1 pack1-not2)))
    (is (as-pack? pack2 {:pack pack1-is1}))
    (is (as-pack? pack2 {:pack pack1-is2}))
    (is (as-pack? pack2 {:pack pack1-as1}))
    (is (as-pack? pack2 {:pack pack1-as2}))
    (is (false? (as-pack? pack2 {:whatever pack1-is2})))
    (is (false? (as-pack? pack2 {:pack {}})))
    (is (false? (as-pack? pack2 {:pack pack2})))
    (is (as-pack? pack3 {:pack1 pack1-is1 :pack2 {:pack pack1-is1}}))
    (is (as-pack? pack3 {:pack2 {:pack pack1-as2} :pack1 pack1-is2}))
    (is (false? (as-pack? pack3 {:pack {:pack pack1-as2} :pack1 pack1-is2})))
    (is (false? (as-pack? pack3 {:pack2 {:pack (dissoc pack1-is1 :d)} :pack1 pack1-is2})))
    (is (false? (as-toys? {:toy1 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}
                           :toy2 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}})))
    (is (true? (as-toys? {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar}
                          :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue :foo :bar}}))))
  (testing "keys-match?"
    (is (keys-match? pack1 pack1-is1))
    (is (keys-match? pack1 pack1))
    (is (keys-match? pack1 pack1-is2))
    (is (false? (keys-match? pack1 pack1-as1)))
    (is (false? (keys-match? pack1 pack1-as2)))
    (is (false? (keys-match? pack1 nil)))
    (is (false? (keys-match? pack1 9)))
    (is (keys-match? pack1 pack1-not1))
    (is (false? (keys-match? pack1 pack1-not2))))
  (testing "is-pack?"
    (is (is-pack? {#{1 2} #{:a :b}} {#{2 1} :b}))
    (is (is-pack? pack1 pack1-is1))
    (is (is-pack? pack1 pack1-is2))
    (is (is-pack? pack1 (dissoc pack1-is1 :e)))
    (is (is-pack? pack1 (dissoc pack1-as1 :e)))
    (is (is-pack? pack1 (dissoc pack1-as2 :f)))
    (is (false? (is-pack? pack1 pack1-as1)))
    (is (false? (is-pack? pack1 pack1-as2)))
    (is (is-pack? pack1 (merge pack1-is1 pack1-is2)))
    (is (false? (is-pack? pack1 (merge pack1-as1 pack1-is2))))
    (is (is-pack? pack1 (dissoc (merge pack1-as1 pack1-is2) :e)))
    (is (false? (is-pack? pack1 nil)))
    (is (false? (is-pack? pack1 {:a 1})))
    (is (false? (is-pack? pack1 (dissoc pack1-is1 :d))))
    (is (false? (is-pack? pack1 (dissoc pack1-is1 :a))))
    (is (false? (is-pack? pack1 (dissoc pack1-is1 :d :b))))
    (is (false? (is-pack? pack1 pack1-not1)))
    (is (false? (is-pack? pack1 pack1-not2)))
    (is (is-pack? pack3 {:pack1 pack1-is1 :pack2 {:pack pack1-is1}}))
    (is (is-pack? pack3 {:pack2 {:pack pack1-is1} :pack1 pack1-is2}))
    (is (false? (is-pack? pack3 {:a {} :pack2 {:pack pack1-is1} :pack1 pack1-is2})))
    (is (false? (is-pack? pack3 {:pack1 pack1-is1 :pack pack1-is1 :pack2 {:pack pack1-is1}})))
    (is (false? (is-pack? pack3 {:a nil :pack2 {:pack pack1-is1} :pack1 pack1-is2})))
    (is (false? (is-pack? pack3 {:pack {:pack pack1-as2} :pack1 pack1-is2})))
    (is (false? (is-pack? pack3 {:pack2 {:pack (dissoc pack1-is1 :d)} :pack1 pack1-is2})))
    (is (false? (is-toys? {:toy1 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}
                           :toy2 {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar}})))
    (is (false? (is-toys? {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
                           :toy2 {:minimum-age 5 :name "bobby" :size :foo :color :red}})))
    (is (true? (is-toys? {:toy1 {:minimum-age 5 :name "andrew" :size 8 :color :red}
                          :toy2 {:minimum-age 5 :name "bobby" :size 9 :color :blue}}))))
  (testing "explain-pack"
    (is (= no-explain (explain-pack pack1 pack1-is1)))
    (is (= no-explain (explain-pack pack1 pack1-is2)))
    (is (= explain-pack1-as1 (explain-pack pack1 pack1-as1)))
    (is (= explain-pack1-as2 (explain-pack pack1 pack1-as2)))
    (is (= explain-pack1-is1 (explain-pack pack1 pack1-not3)))
    (is (= explain-pack1-is2 (explain-pack pack2 pack2-not)))
    (is (= explain-pack1-is3 (explain-pack pack2 pack2-not2))))
  (testing "as-pack"
    (is (as-pack pack1 pack1-is1))
    (is (= pack1-is1 (as-pack pack1 pack1-is1)))
    (is (= pack1-is2 (as-pack pack1 pack1-is2)))
    (is (= pack1-as1 (as-pack pack1 pack1-as1)))
    (is (= pack1-as2 (as-pack pack1 pack1-as2)))
    (is (= {} (as-pack pack1 {})))
    (is (nil? (as-pack pack1 nil)))
    (is (= 99 (as-pack pack1 99))))
  (testing "is-pack"
    (is (is-pack pack1 pack1-is1))
    (is (= pack1-is1 (is-pack pack1 pack1-is1)))
    (is (= pack1-is2 (is-pack pack1 pack1-is2)))
    (is (= pack1-as1 (is-pack pack1 pack1-as1)))
    (is (= pack1-as2 (is-pack pack1 pack1-as2)))
    (is (= {} (is-pack pack1 {})))
    (is (nil? (is-pack pack1 nil)))
    (is (= 99 (is-pack pack1 99)))
    (is (= 5 (binding [*assert* false]
               (clojure.walk/macroexpand-all '(is-pack pack1 5)))))
    (is (= '(get {} :a)
           (binding [*assert* false]
             (clojure.walk/macroexpand-all 
              '(is-pack pack1 (get {} :a)))))))
  (testing "defpack"
    (is (map? toy))
    (is (fn? as-toy?))
    (is (fn? is-toy?))
    (is (false? (as-toy? {:a 1})))
    (is (false? (as-toy? {:minimum-age 5 :name :andrew :size 8 :color :red :foo :bar})))
    (is (true? (as-toy? {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar})))
    (is (true? (is-toy? {:minimum-age 5 :name "andrew" :size 8 :color :red})))
    (is (false? (is-toy? {:minimum-age 5 :name "andrew" :size 8 :color :red :foo :bar})))))

(deftest asserts-off-nested
  (testing "nested people"
    (is (is-address? {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}))
    (is (is-pack? address {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}))
    (is (false? (is-address? {:street "Foo St." :city "Foo City" :state "ME" :zip "123456"})))
    (is (false? (is-pack? address {:street "Foo St." :city "Foo City" :state "ME" :zip "123456"})))
    (is (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}
                          :name    "Andrew"
                          :age     1
                          :height  1}))
    (is (false? (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "ME" :zip "12345"}
                                  :name    "Andrew"
                                  :age     1
                                  :height  0})))
    (is (false? (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "ME" :zip "123456"}
                                  :name    "Andrew"
                                  :age     1
                                  :height  1})))
    (is (false? (is-pack? person {:address {:street "Foo St." :city "Foo City" :state "MEi" :zip "12345"}
                                  :name    "Andrew"
                                  :age     1
                                  :height  1})))
    (is (= {:invalid [[:address [[:street 5]]]] :extra [[:address [[:extra 99]]]]}
           (explain-pack person {:address {:street 5 :city "hi" :state "hi" :zip "abcde" :extra 99}
                                 :name    "hi" :age 1 :height 1000000000000})))
    (is (= {:street 5 :city "hi" :state "hi" :zip "abcde" :extra 99}
           (is-pack address {:street 5 :city "hi" :state "hi" :zip "abcde" :extra 99})))
    (is (= {:address {:street "Foo St." :city "Foo City" :state "MEi" :zip "12345"}
            :name    "Andrew"
            :age     1
            :height  1}
           (is-pack person {:address {:street "Foo St." :city "Foo City" :state "MEi" :zip "12345"}
                            :name    "Andrew"
                            :age     1
                            :height  1})))
    (is (= {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
            :name    "hi" :age 1 :height 1000000000000}
           (as-pack person {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                            :name    "hi" :age 1 :height 1000000000000})))
    (is (= {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
            :name    "hi" :age 1 :height 1000000000000}
           (is-pack person {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                            :name    "hi" :age 1 :height 1000000000000})))
    (is (true? (as-pack? two-people {:one {:name    "" :age 1 :height 1
                                           :address {:a      1  :b     1    :c   :hi
                                                     :city   "lllll1123235"
                                                     :street "" :state "aa" :zip "12345"}}
                                     :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                           :name    "hi" :age 1 :height 1000000000000}})))
    (is (false? (is-pack? two-people {:one {:name    "" :age 1 :height 1
                                            :address {:a      1  :b     1    :c   :hi
                                                      :city   "lllll1123235"
                                                      :street "" :state "aa" :zip "12345"}}
                                      :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                            :name    "hi" :age 1 :height 1000000000000}})))
    (is (= {:one {:name    "" :age 1 :height 1
                  :address {:a      1  :b     1    :c   :hi
                            :city   "lllll1123235"
                            :street "" :state "aa" :zip "12345"}}
            :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                  :name    "hi" :age 1 :height 1000000000000}}
           (as-pack two-people {:one {:name    "" :age 1 :height 1
                                      :address {:a      1  :b     1    :c   :hi
                                                :city   "lllll1123235"
                                                :street "" :state "aa" :zip "12345"}}
                                :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                      :name    "hi" :age 1 :height 1000000000000}})))
    (is (= {:one {:name    "" :age 1 :height 1
                  :address {:city   "lllll1123235"
                            :street "" :state "aa" :zip "12345"}}
            :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde"}
                  :name    "hi" :age 1 :height 1000000000000}}
           (is-pack two-people {:one {:name    "" :age 1 :height 1
                                      :address {:city   "lllll1123235"
                                                :street "" :state "aa" :zip "12345"}}
                                :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde"}
                                      :name    "hi" :age 1 :height 1000000000000}})))
    (is (= {:one {:name    "" :age 1 :height 0
                  :address {:a      1  :b     1    :c   :hi
                            :city   "lllll1123235"
                            :street "" :state "aa" :zip "12345"}}
            :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                  :name    "hi" :age 1 :height 1000000000000}}
           (is-pack two-people {:one {:name    "" :age 1 :height 0
                                      :address {:a      1  :b     1    :c   :hi
                                                :city   "lllll1123235"
                                                :street "" :state "aa" :zip "12345"}}
                                :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde" :extra 99}
                                      :name    "hi" :age 1 :height 1000000000000}})))
    (is (= {:one {:name    "" :age 1 :height 1
                  :address {:a      1  :b     1    :c   :hi
                            :city   "lllll1123235"
                            :street "" :state "aa" :zip 9}}
            :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde"}
                  :name    "hi" :age 1 :height 1000000000000}}
           (as-pack two-people {:one {:name    "" :age 1 :height 1
                                      :address {:a      1  :b     1    :c   :hi
                                                :city   "lllll1123235"
                                                :street "" :state "aa" :zip 9}}
                                :two {:address {:street "5" :city "hi" :state "hi" :zip "abcde"}
                                      :name    "hi" :age 1 :height 1000000000000}})))))

(set! *assert* true)
