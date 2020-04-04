(defproject restful-clojure "0.1.0-SNAPSHOT"
  :description "An example RESTful shopping list application back-end written in Clojure to accompany a tutorial series on kendru.github.io"
  :url "https://github.com/kendru/restful-clojure"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}

  ;; :dependencies [[org.clojure/clojure "1.10.0"]
  ;;                [buddy/buddy-auth "2.2.0"]
  ;;                [buddy/buddy-hashers "1.4.0"]
  ;;                [cheshire "5.10.0"]
  ;;                [compojure "1.6.1"]
  ;;                [crypto-random "1.2.0"]
  ;;                [environ "1.1.0"]
  ;;                [korma "0.5.0-RC1"]
  ;;                [ragtime "0.3.4"]
  ;;                [ring/ring-core "1.8.0"]
  ;;                [ring/ring-defaults "0.3.2"]
  ;;                [ring/ring-jetty-adapter "1.8.0"]
  ;;                [ring/ring-json "0.2.0"]
  ;;                [javax.servlet/servlet-api "2.5"]
  ;;                [ring/ring-mock "0.4.0"]]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [compojure "1.1.6"]
                 [cheshire "5.3.1"]
                 [ring/ring-json "0.2.0"]
                 [korma "0.3.0-RC5"]
                 [postgresql "9.3-1102.jdbc41"]
                 [ragtime "0.3.4"]
                 [environ "0.4.0"]
                 [buddy/buddy-hashers "0.4.0"]
                 [buddy/buddy-auth "0.4.0"]
                 [crypto-random "1.2.0"]
                 [ring-mock "0.1.5"]]

  ; The lein-ring plugin allows us to easily start a development web server
  ; with "lein ring server". It also allows us to package up our application
  ; as a standalone .jar or as a .war for deployment to a servlet contianer
  ; (I know... SO 2005).

  ;; :plugins [[lein-ring "0.12.5"]
  ;;           [lein-environ "1.1.0"]
  ;; ]

  :plugins [[lein-cljfmt "0.6.7"]
            [lein-environ "0.4.0"]
            [lein-ring "0.8.10"]
            [org.clojure/core.unify "0.5.7"]]

  ; See https://github.com/weavejester/lein-ring#web-server-options for the
  ; various options available for the lein-ring plugin
  :ring {:handler restful-clojure.handler/app
         :nrepl {:start? true
                 :port 9998}}

  ; Have ragtime default to loading the database URL from an environment
  ; variable so that we don't keep production credentials in our
  ; source code. Note that for our dev environment, we set this variable
  ; with Puppet (see default.pp).
  :ragtime {:migrations ragtime.sql.files/migrations}

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
         ; Since we are using environ, we can override these values with
         ; environment variables in production.
         :env {:restful-db "restful_dev"
               :restful-db-user "restful_dev"
               :restful-db-pass "pass_dev"}}
   :test {:ragtime {:database "jdbc:postgresql://localhost:5432/restful_test?user=restful_test&password=pass_test"}
          :env {:restful-db "restful_test"
                :restful-db-user "restful_test"
                :restful-db-pass "pass_test"}}})
