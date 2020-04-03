(ns restful-clojure.models.users
  (:require [restful-clojure.entities :as e]
            [buddy.hashers :as hashers]
            [korma.core :as k]))

(def user-levels
  {"user" ::user
   "admin" ::admin})
(derive ::admin ::user)

(defn- with-kw-level [user]
  (assoc user :level
              (get user-levels (:level user) ::user)))

(defn- with-str-level [user]
  (assoc user :level (if-let [level (:level user)]
                       (name level)
                       "user")))

(defn find-all []
  (k/select e/users))

(defn find-by [field value]
  (some-> (k/select* e/users)
          (k/where {field value})
          (k/limit 1)
          k/select
          first
          (dissoc :password_digest)
          with-kw-level))

(defn find-by-id [id]
  (find-by :id id))

(defn for-list [listdata]
  (find-by-id (listdata :user_id)))

(defn find-by-email [email]
  (find-by :email email))

(defn create [user]
  (-> (k/insert* e/users)
      (k/values (-> user
                  (assoc :password_digest (hashers/encrypt (:password user)))
                  with-str-level
                  (dissoc :password)))
      k/insert
      (dissoc :password_digest)
      with-kw-level))

(defn update-user [user]
  (k/update e/users
    (k/set-fields (-> user
                    (dissoc :id :password)
                    with-str-level))
    (k/where {:id (user :id)})))

(defn count-users []
  (let [agg (k/select e/users
              (k/aggregate (count :*) :cnt))]
    (get-in agg [0 :cnt] 0)))

(defn delete-user [user]
  (k/delete e/users
    (k/where {:id (user :id)})))

(defn password-matches?
  "Check to see if the password given matches the digest of the user's saved password"
  [id password]
  (some-> (k/select* e/users)
            (k/fields :password_digest)
            (k/where {:id id})
            k/select
            first
            :password_digest
            (->> (hashers/check password))))
