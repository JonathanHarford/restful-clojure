(ns restful-clojure.handler-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [cheshire.core :as json]
            [restful-clojure.handler :as handler]
            [restful-clojure.test-core :as test-core]
            [restful-clojure.models.users :as u]
            [restful-clojure.models.lists :as l]
            [restful-clojure.auth :as auth]
            [restful-clojure.models.products :as p]
            [ring.mock.request :as req]))

; WIll be rebound in test
(def ^{:dynamic true} *session-id* nil)

(defn with-session [t]
  (let [user (u/create {:name "Some admin"
                        :email "theadmin@example.com"
                        :password "sup3rs3cr3t"
                        :level :restful-clojure.auth/admin})
        session-id (auth/make-token! (:id user))]
    (with-bindings {#'*session-id* session-id}
      (t))
    (u/delete-user user)))

(use-fixtures :each test-core/with-rollback)
(use-fixtures :once with-session)

(defn with-auth-header [req]
  (req/header req "Authorization" (str "Token " *session-id*)))

(deftest main-routes
  (testing "list users"
    (let [response (handler/app (with-auth-header (req/request :get "/users")))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))

  (testing "lists endpoint"
    (let [response (handler/app (with-auth-header (req/request :get "/lists")))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))

  (testing "products endpoint"
    (let [response (handler/app (with-auth-header (req/request :get "/products")))]
      (is (= (:status response) 200))
      (is (= (get-in response [:headers "Content-Type"]) "application/json; charset=utf-8"))))

  (testing "not-found route"
    (let [response (handler/app (req/request :get "/bogus-route"))]
      (is (= (:status response) 404)))))

(deftest creating-user
  (testing "POST /users"
    (let [user-count (u/count-users)
          response (handler/app (-> (req/request :post "/users")
                                    with-auth-header
                                    (req/body (json/generate-string {:name "Joe Test"
                                                                     :email "joe@example.com"
                                                                     :password "s3cret"}))
                                    (req/content-type "application/json")
                                    (req/header "Accept" "application/json")))]
      (is (= (:status response) 201))
      (is (test-core/substring? "/users/" (get-in response [:headers "Location"])))
      (is (= (inc user-count) (u/count-users))))))

(deftest retrieve-user-stuff
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com" :password "s3cret"})
        initial-count (u/count-users)]

    (testing "GET /users"
      (doseq [i (range 4)]
        (u/create {:name "Person" :email (str "person" i "@example.com") :password "s3cret"}))
      (let [response (handler/app (with-auth-header (req/request :get "/users")))
            resp-data (json/parse-string (:body response))]
        (is (= (:status response 200)))
        ; A person's email contained in the response body
        (is (test-core/substring? "person3@example.com" (:body response)))
        ; All results present (including the user created in the let form)
        (is (= (+ initial-count 4) (count (get resp-data "results" []))))
        ; "count" field present
        (is (= (+ initial-count 4) (get resp-data "count" [])))))

    (testing "GET /users/:id"
      (let [response (handler/app (with-auth-header (req/request :get (str "/users/" (:id user)))))]
        (is (= (:body response) (json/generate-string user)))))

    (testing "GET /users/:id/lists"
      (let [my-list (l/create {:user_id (:id user) :title "Wonderful Stuffs"})
            response (handler/app (with-auth-header (req/request :get (str "/users/" (:id user) "/lists"))))]
        (is (= (:body response) (json/generate-string [(dissoc my-list :user_id)])))))))

(deftest deleting-user
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com" :password "s3cr3t"})]

    (testing "DELETE /users/:id"
      (let [response (handler/app (with-auth-header (req/request :delete (str "/users/" (:id user)))))]
        ; okay/no content status
        (is (= (:status response) 204))
        ; redirected to users index
        (is (= "/users" (get-in response [:headers "Location"])))
        ; user no longer exists in db
        (is (nil? (u/find-by-id (:id user))))))))

(deftest creating-list
  (testing "POST /lists"
    (let [list-count (l/count-lists)
          user (u/create {:name "John Doe" :email "j.doe@mytest.com" :password "s3cr3t"})
          response (handler/app (-> (req/request :post "/lists")
                                    with-auth-header
                                    (req/body (str "{\"user_id\":" (:id user) ",\"title\":\"Amazing Accoutrements\"}"))
                                    (req/content-type "application/json")
                                    (req/header "Accept" "application/json")))]
      (is (= (:status response) 201))
      (is (test-core/substring? "/users/" (get-in response [:headers "Location"])))
      (is (= (inc list-count) (l/count-lists))))))

(deftest retrieving-list
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com" :password "s3cret"})
        listdata (l/create {:user_id (:id user) :title "Root Beers of Iowa"})]

    (testing "GET /lists"
      (doseq [i (range 4)]
        (l/create {:user_id (:id user) :title (str "List " i)}))
      (let [response (handler/app (with-auth-header (req/request :get "/lists")))
            resp-data (json/parse-string (:body response))]
        (is (= (:status response 200)))
        ; A list title
        (is (test-core/substring? "List 3" (:body response)))
        ; All results present
        (is (= 5 (count (get resp-data "results" []))))
        ; "count" field present
        (is (= 5 (get resp-data "count" [])))))

    (testing "GET /lists/:id"
      (let [response (handler/app (with-auth-header (req/request :get (str "/lists/" (:id listdata)))))]
        (is (= (:body response) (json/generate-string listdata)))))))

(deftest deleting-list
  (let [user (u/create {:name "John Doe" :email "j.doe@mytest.com" :password "s3cr3t"})
        listdata (l/create {:user_id (:id user) :title "Root Beers of Iowa"})]

    (testing "DELETE /lists/:id"
      (let [response (handler/app (with-auth-header (req/request :delete (str "/lists/" (:id listdata)))))]
        ; okay/no content status
        (is (= (:status response) 204))
        ; redirected to users index
        (is (= "/lists" (get-in response [:headers "Location"])))
        ; list no longer exists in db
        (is (nil? (l/find-by-id (:id listdata))))))))

(deftest creating-product
  (testing "POST /products"
    (let [prod-count (p/count-products)
          response (handler/app (-> (req/request :post "/products")
                                    with-auth-header
                                    (req/body (str "{\"title\":\"Granny Smith\",\"description\":\"Howdya like them apples?\"}"))
                                    (req/content-type "application/json")
                                    (req/header "Accept" "application/json")))]
      (is (= (:status response) 201))
      (is (test-core/substring? "/products/" (get-in response [:headers "Location"])))
      (is (= (inc prod-count) (p/count-products))))))

(deftest retrieving-product
  (testing "GET /products"
    (doseq [i (range 5)]
      (p/create {:title (str "Product " i)}))
    (let [response (handler/app (with-auth-header (req/request :get "/products")))
          resp-data (json/parse-string (:body response))]
      (is (= (:status response 200)))
      ; Product name contained in the response body
      (is (test-core/substring? "Product 4" (:body response)))
      ; All results present
      (is (= 5 (count (get resp-data "results" []))))
      ; "count" field present
      (is (= 5 (get resp-data "count" []))))))
