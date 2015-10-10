(ns webapp.fake-db
  "Doing a fake db instead of needing an external resource."
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defonce the-data (atom {:users []}))

(defrecord FakeDB [settings]
  component/Lifecycle
  (start [this]
    (assoc this :database the-data))

  (stop [this]
    this))