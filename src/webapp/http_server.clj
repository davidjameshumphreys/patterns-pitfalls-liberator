(ns webapp.http-server
  "A basic Lifecycle component for serving HTTP using the httpkit
  server."
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]))

(defrecord HttpServer [application settings]
  component/Lifecycle

  (start [this]
    (log/infof "started with [%s]" (with-out-str (clojure.pprint/pprint this)))
    (let [http-settings (merge {:port 8080 :thread 2}
                               (select-keys settings #{:port :thread}))
          shutdown-hook (run-server (application this) http-settings)]
      (log/infof "Started the webserver on port %s, threads: %s"
                 (-> http-settings :port str)
                 (-> http-settings :thread str))
      (assoc this
        :shutdown-hook shutdown-hook
        :settings      settings)))

  (stop [{:keys [shutdown-hook] :as this}]
    (if shutdown-hook
      (do
        (log/info "Stopped the webserver")
        (shutdown-hook)
        (dissoc this :shutdown-hook)))))
