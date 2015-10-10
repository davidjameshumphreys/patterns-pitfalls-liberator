(ns webapp.examples.ends
  (:require [liberator.core :refer [resource defresource]]))


(def simple-get
  (resource {:available-media-types ["text/html"]
             :exists?               (fn [ctx] {:data "My data"})
             :handle-ok             (fn [{:keys [data] :as ctx}]
                                      (println ctx)
                                      data)}))

(defn- json? [ctx]
  (-> ctx
      :representation
      :media-type
      (= "application/json")))

(def simple-get-with-type
  (resource {:available-media-types ["text/html" "application/json"]
             :allowed-methods       [:get]
             :exists?               (fn exists [ctx] {:data "My data"})
             :handle-ok             (fn ok [{:keys [data] :as ctx}]
                                      (if (json? ctx)
                                        {:data data
                                         :is-json true}
                                        data))}))
