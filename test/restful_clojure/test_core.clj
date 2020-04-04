;; Common utilities to use across tests
(ns restful-clojure.test-core
  (:require [korma.db :as k]))

(defn substring? [sub st]
  (if (nil? st)
    false
    (not= (.indexOf st sub) -1)))

(defn with-rollback
  "Test fixture for executing a test inside a database transaction
  that rolls back at the end so that database tests can remain isolated"
  [test-fn]
  (k/transaction
    (test-fn)
    (k/rollback)))
