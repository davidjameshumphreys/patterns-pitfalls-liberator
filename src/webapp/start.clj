(ns webapp.start
  "The dependencies between components live in this namespace.
  Functions for starting & stopping the system."
  (:require [com.stuartsierra.component :as comp]
            [webapp.http-server :as http]
            [webapp.render-engine :as render]
            [webapp.fake-db :as db]
            [webapp.routes :refer [the-application]]
            [webapp.system :refer [system]]))

(def ^:private web-settings
  {:application #'the-application
   :settings    {:port   9000
                 :thread 2}})

(def ^:private render-settings
  {:path ""
   :suffix ".hbs"
   :global-vars {:css-root "/static/main.css"}})

(def ^:private database-settings
  {:url "jdbc:h2:file:./h2/dev"})

(defn start
  "Start the components."
  []
  (intern 'webapp.system 'system
    (comp/start
      (comp/system-map
        :database    (db/->FakeDB database-settings)
        :http-server (comp/using
                       (http/map->HttpServer web-settings)
                       [:render :database])
        :render (render/map->RenderEngine render-settings)))))

(defn stop
  "Stop the components."
  []
  (when (bound? #'system)
    (comp/stop-system system)))
