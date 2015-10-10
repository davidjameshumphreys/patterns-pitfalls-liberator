(ns webapp.start
  "The dependencies between components live in this namespace.
  Functions for starting & stopping the system."
  (:require [com.stuartsierra.component :as comp]
            [webapp.http-server :as http]
            [webapp.render-engine :as render]
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

(defn start
  "Start the components."
  []
  (intern 'webapp.system 'system
    (comp/start
      (comp/system-map
        :http-server (comp/using
                       (http/map->HttpServer web-settings)
                       [:render])
        :render (render/map->RenderEngine render-settings)))))

(defn stop
  "Stop the components."
  []
  (when (bound? #'system)
    (comp/stop-system system)))
