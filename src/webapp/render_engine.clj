(ns webapp.render-engine
  (:require [clojure.set :refer [rename-keys]]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [hbs.core :as hcore]))

(defn output-render
  "A render fn that has all of the global vars merged into the
  context."
  [request template context]
  (let [merged-data (merge
                     (-> request
                         (select-keys #{:global-render-vars})
                         (rename-keys {:global-render-vars :globals}))
                     context)]
    (hcore/render-file template merged-data)))

(defrecord RenderEngine [path suffix global-vars]
  component/Lifecycle
  (start [this]
    (try
      (let [suffix (if (re-find #"^\." suffix)
                     suffix
                     (str \. suffix))]
        (log/infof "Template settings: [path '%s', suffix '%s']" path suffix)
        (log/tracef "Globals: %s" (with-out-str (clojure.pprint/pprint global-vars)))
        (hcore/set-template-path! path suffix))
      (catch Exception e
        (log/error e)))
    (assoc this
      :global-vars global-vars))

  (stop [this]
    ;; nothing to do here, yet.
    this))
