(ns webapp.fav.date
  (:require [liberator.core :refer [resource]]
            [webapp.common-resource :refer [do-post do-get]]
            [webapp.schemata :refer [check-date]]))

(def dates (atom {}))

(def q 1)

(defn update-count
  "For the day and month passed in update each of them independently of each other."
  [a {:keys [day month]}]
  (swap! a (fn [current]
             (let [dval (get-in current [:day day] 0)
                   mval (get-in current [:month month] 0)]
               (-> current
                 (assoc-in [:day day] (inc dval))
                 (assoc-in [:month month] (inc mval)))))))

(defn represent-data [data]
  (->> data
       :day
       (sort-by (fn [[_ v]] v))
       reverse
       (map (fn [[k v]]
              {:count v
               :day   k}))))

(defn render-date [{:keys [request data error]}]
  (let [render (:render-fn request)]
    (render "fav-date" (merge {:results (represent-data data)}
                              (when error
                                {:error (str error)
                                 :is-error true})))))

(def date-post
  "Post your favourite date"
  (resource
   (do-post
    (fn malformed?
      [{:keys [request] :as ctx}]
      (-> request
          :params
          check-date))
    (fn exists? [_]
      {:data @dates})
    (fn post! [{:keys [coerced-params data]}]
      (update-count dates coerced-params))
    (fn post-redirect? [_] {:location "/fav/date/"})
    :handle-malformed render-date)))

(def date-get
  (resource
   (do-get
     (constantly nil)
     (fn exists? [ctx]
       {:data @dates})
    render-date)))
