(ns restful-clojure.products-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [restful-clojure.models.products :as products]
            [restful-clojure.test-core :refer [with-rollback]]))

(use-fixtures :each with-rollback)

(deftest create-product
  (testing "Create a product increments product count"
    (let [count-orig (products/count-products)]
      (products/create {:title "Cherry Tomatos"
                        :description "Tasty red tomatos"})
      (is (= (inc count-orig) (products/count-products))))))
