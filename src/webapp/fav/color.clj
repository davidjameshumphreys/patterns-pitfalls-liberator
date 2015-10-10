(ns webapp.fav.color
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [liberator.core :refer [resource]]
            [webapp.common-resource :refer [do-post do-get]]
            [webapp.bidi-helper :refer [get-bidi-path-for]]
            [webapp.schemata :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; model helpers for this resource

(def colors (atom {}))

(defn update-count
  "Update the atom a count of item."
  [a item]
  (swap! a (fn [current]
             (let [val (get current item 0)]
               (assoc current
                 item (inc val))))))

(defn represent-data
  "Take the data in the atom and turn into a form suitable for
  rendering"
  [a]
  (->> a
       (sort-by (fn [[_ v]] v))
       reverse
       (map (fn [[k v]]
              {:count v
               :color (str/join "" (map (fn [i]
                                          (format "%02x" i)) k))}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn render-color [{:keys [data request error] :as ctx}]
  (let [render (:render-fn request)]
    (render "fav-color"
            (merge {:results (represent-data data)}
                   (when error
                     {:error (str error)
                      :is-error true})))))

(def color-post
  "Post your favourite colour to this endpoint."
  (resource
   (do-post (fn malformed? [ctx]
              (-> ctx
                  :request
                  :params
                  s/color-coerce))
            (fn exists [{:keys [request coerced-params] :as ctx}]
              (log/trace (str coerced-params))
              {:data true})
            (fn post! [{:keys [request data coerced-params] :as ctx}]
              (update-count colors (-> coerced-params
                                       :color)))
            (fn redirect [_] {:location "/fav/color/"})
            :handle-malformed render-color)))

(def color-get
  "Show the current state of the favourite colors."
  (resource
    (do-get
      (constantly nil)
      (fn exists? [_]
        {:data @colors})
      render-color)))
