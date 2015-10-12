(ns webapp.fake-db
  "Doing a fake db instead of needing an external resource."
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource]))

(defonce the-data (atom {:users []}))

(defn- pool [driver url]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass driver)
               (.setJdbcUrl url)

               (.setMinPoolSize 1)
               (.setMaxPoolSize 8)
               (.setAcquireIncrement 1)
               (.setPreferredTestQuery "select 1")

               (.setTestConnectionOnCheckout true)
               (.setCheckoutTimeout 7000)
               (.setAcquireRetryAttempts 3)
               (.setAcquireRetryDelay 1000)

               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(defrecord FakeDB [settings]
  component/Lifecycle
  (start [this]
    (let [value (pool "org.h2.Driver" (:url settings))]
      (assoc this :database value)))

  (stop [this]
    (-> this :database :datasource .close)
    (dissoc this :database)))