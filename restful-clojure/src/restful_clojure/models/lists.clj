(ns restful-clojure.models.lists
  (:require [korma.core :as k]
            [restful-clojure.entities :as e]
            [clojure.set :refer [difference]]))

(declare add-product)

(defn find-all []
  (k/select e/lists
    (k/with e/products)))

(defn find-by [field value]
  (first
    (k/select e/lists
      (k/with e/products)
      (k/where {field value})
      (k/limit 1))))

(defn find-all-by [field value]
  (k/select e/lists
    (k/with e/products)
    (k/where {field value})))

(defn find-by-id [id]
  (find-by :id id))

(defn for-user [userdata]
  (find-all-by :user_id (:id userdata)))

(defn count-lists []
  (let [agg (k/select e/lists
              (k/aggregate (count :*) :cnt))]
    (get-in agg [0 :cnt] 0)))

(defn create [listdata]
  (let [newlist (k/insert e/lists
                  (k/values (dissoc listdata :products)))]
    (doseq [product (:products listdata)]
      (add-product newlist (:id product) "incomplete"))
    (assoc newlist :products (into [] (:products listdata)))))

(defn add-product
  "Add a product to a list with an optional status arg"
  ([listdata product-id]
    (add-product listdata product-id "incomplete"))
  ([listdata product-id status]
    (let [sql (str "INSERT INTO lists_products ("
                   "list_id, product_id, status"
                   ") VALUES ("
                   "?, ?, ?::item_status"
                   ")")]
      (k/exec-raw [sql [(:id listdata) product-id status] :results])
      (find-by-id (:id listdata)))))

(defn remove-product [listdata product-id]
  (k/delete "lists_products"
    (k/where {:list_id (:id listdata)
            :product_id product-id}))
   (update-in listdata [:products]
     (fn [products] (remove #(= (:id %) product-id) products))))

(defn- get-product-ids-for
  "Gets a set of all product ids that belong to a particular list"
  [listdata]
  (into #{}
    (map :product_id
      (k/select "lists_products"
        (k/fields :product_id)
        (k/where {:list_id (:id listdata)})))))

(defn update-list [listdata]
  (k/update e/lists
    (k/set-fields (dissoc listdata :id :products))
    (k/where {:id (:id listdata)}))
  (let [existing-product-ids (get-product-ids-for listdata)
        updated-product-ids (->> (:products listdata)
                                 (map :id)
                                 (into #{}))
        to-add (difference updated-product-ids existing-product-ids)
        to-remove (difference existing-product-ids updated-product-ids)]
    (doseq [prod-id to-add]
      (add-product listdata prod-id))
    (doseq [prod-id to-remove]
      (remove-product listdata prod-id))
    (find-by-id (:id listdata))))

(defn delete-list [listdata]
  (k/delete e/lists
    (k/where {:id (:id listdata)})))
