(ns webapp.fav.name
  (:require [clojure.java.jdbc :as jdbc]
            [webapp.common-resource :refer [do-post do-get]]
            [liberator.core :refer [resource]]
            [webapp.schemata :refer [valid-name]]
            [webapp.bidi-helper :refer [get-bidi-path-for]]
            [clojure.tools.logging :as log]))

(defn get-data [database]
  (jdbc/query database "select * from names"))

(defn get-datum [database id]
  (if-let [data (not-empty (jdbc/query database ["select * from names where id = ?" id]))]
    (first data)
    {:id id :popularity 0}))

(defn update-data [database id]
  (jdbc/with-db-transaction
    [db database]
    (if-let [data (not-empty (jdbc/query db ["select * from names where id = ?" id]))]
      (jdbc/update! db :names {:popularity (-> data first :popularity inc)} ["id = ?" id])
      (jdbc/insert! db :names {:id id :popularity 1}))))

(defn render-name [{:keys [data request error] :as ctx}]
  (let [render (:render-fn request)]
    (render "fav-name"
            (merge {:results data}
                   (when error
                     {:error    (str error)
                      :is-error true})))))

(def name-post
  (resource
    (do-post
      (fn [ctx]
        (-> ctx :request :params valid-name))
      (fn exists [{:keys [request coerced-params] :as ctx}]
        (let [data (get-datum (:database request) (:id coerced-params))]
          (log/info "data is: " data)
          {:data data}))
      (fn post! [{:keys [request data coerced-params] :as ctx}]
        (update-data (:database request) (:id coerced-params)))
      (fn redirect [{:keys [request]}]
        {:location ((get-bidi-path-for request) :name/get)})
      :handle-malformed render-name)))

(def name-get
  "Show the current state of the favourite colors."
  (resource
    (do-get
      (constantly nil)
      (fn exists? [{:keys [request]}]
        (let [database (:database request)]
          {:data (get-data database)}))
      render-name)))
