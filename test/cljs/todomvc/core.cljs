(ns todomvc.test.core
  (:require-macros [cemerick.cljs.test
                  :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]))

(deftest somewhat-less-wat
  (is (= "{}[]" (+ {} []))))

(deftest foo
  (testing "foo"
    (is (= 1 1))))


