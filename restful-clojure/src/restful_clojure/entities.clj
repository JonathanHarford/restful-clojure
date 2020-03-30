(ns restful-clojure.entities
  (:require [korma.core :as k]
            [restful-clojure.db]))

(declare users lists products)

(k/defentity users
  (k/pk :id)
  (k/table :users)
  (k/has-many lists)
  (k/entity-fields :name :email))

(k/defentity lists
  (k/pk :id)
  (k/table :lists)
  (k/belongs-to users {:fk :user_id})
  (k/many-to-many products :lists_products {:lfk :list_id
                                          :rfk :product_id})
  (k/entity-fields :title))

(k/defentity products
  (k/pk :id)
  (k/table :products)
  (k/many-to-many lists :lists_products {:lfk :product_id
                                       :rfk :list_id})
  (k/entity-fields :title :description))

(k/defentity auth-tokens
  (k/pk :id)
  (k/table :auth_tokens)
  (k/belongs-to users {:fk :user_id}))
