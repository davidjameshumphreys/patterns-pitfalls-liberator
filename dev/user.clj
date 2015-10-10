(ns user
  (:require [clojure.tools.namespace.repl :as repl :refer [refresh]]
            [clojure.test :as test]
            [clojure.tools.logging :as log])
  ;; using :use because then we don't need to def aliases
  ;; don't do this normally.
  (:use [webapp.start :only [start stop]]
        [webapp.system :only [system]]))

(defn reset
  "If the system is running, stop it then reload all namespaces and
  start the system"
  []
  (stop)
  (refresh)
  (start))

(defn test-all
  "Reset and then run all matching test namespaces"
  ([] (test-all #"^webapp\..*test$"))
  ([re]
     (reset)
     (test/run-all-tests re)))
