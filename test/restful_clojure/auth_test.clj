(ns restful-clojure.auth-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [restful-clojure.auth :as auth]
            [restful-clojure.models.users :as u]
            [restful-clojure.test-core :as test-core]
            [korma.core :as k]))

(use-fixtures :each test-core/with-rollback)

(deftest authenticating-users
  (let [user (u/create {:name "Test" :email "user@example.com" :password "s3cr3t"})]

    (testing "Authenticates with valid token"
      (let [token (auth/make-token! (:id user))]
        (is (= user (auth/authenticate-token {} token)))))

    (testing "Does not authenticate with nonexistent token"
      (is (nil? (auth/authenticate-token {} "youhavetobekiddingme"))))

    (testing "Does not authenticate with expired token"
      (let [token (auth/make-token! (:id user))
            sql (str "UPDATE auth_tokens "
                     "SET created_at = NOW() - interval '7 hours' "
                     "WHERE id = ?")]
        (k/exec-raw [sql [token]])
        (is (nil? (auth/authenticate-token {} token)))))))

; (detest authorizing-users
;   (let [user (u/create {:name "User" :email "user@example.com" :password "s3cr3t"})
;         admin (u/create {:name "Admin" :email "admin@example.com" :password "sup3rs3cr3t" :restful-clojure.models.users/admin})]))
