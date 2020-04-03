(ns restful-clojure.models.products
  (:require [restful-clojure.entities :as e]
            [korma.core :as k]))

(defn create [product]
  (k/insert e/products
    (k/values product)))

(defn find-all []
  (k/select e/products))

(defn find-by [field value]
  (first
    (k/select e/products
      (k/where {field value})
      (k/limit 1))))

(defn find-all-by [field value]
  (k/select e/products
    (k/where {field value})))

(defn find-by-id [id]
  (find-by :id id))

(defn count-products []
  (let [agg (k/select e/products
              (k/aggregate (count :*) :cnt))]
    (get-in agg [0 :cnt] 0)))

(defn update-product [product]
  (k/update e/products
    (k/set-fields (dissoc product :id))
    (k/where {:id (product :id)})))

(defn delete-product [product]
  (k/delete e/products
    (k/where {:id (product :id)})))
