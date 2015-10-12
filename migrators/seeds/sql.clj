(ns seeds.sql
  (:require [clojure.java.jdbc :as j]))

(defn run [target & args]
  (j/with-db-connection [db {:connection-uri (-> target :db :url)}]
    (j/insert! db :favourite_colors {:popularity 0, :red 255 :green 0 :blue 0})
    (j/insert! db :favourite_colors {:popularity 42, :red 0 :green 0 :blue 255})
    (j/insert! db :names {:id "david" :popularity 4})
                        (println (j/query db ["select * from favourite_colors"]))))
