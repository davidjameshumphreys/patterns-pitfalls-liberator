(ns webapp.bidi-helper
  (:require [clojure.tools.logging :as log]
            [bidi.bidi :as b]
            [ring.util.codec :refer [form-encode]]))

(defn bidi-finder
  "Encapsulates bidi's path-for with the routes and handlers it needs"
  [bidi-routes-fn bidi-handlers]
  (fn f
    ([resource-name url-params query-params]
     (let [routes (bidi-routes-fn bidi-handlers)
           handler (get bidi-handlers resource-name)]
       (str (apply b/path-for
                   routes
                   handler
                   (mapcat (juxt key (comp str val)) url-params))
            (when (seq query-params)
              (str "?" (form-encode query-params))))))
    ([resource-name args]
     (f resource-name args {}))
    ([resource-name]
     (f resource-name {} {}))))

(defn enable-bidi-path-for
  "Used in the middleware to add the route structure in to the request."
  [request bidi-routes-fn bidi-handlers]
  (assoc request ::url (bidi-finder bidi-routes-fn bidi-handlers)))

(defn get-bidi-path-for
  "Returns the bidi path-for fn with the associated routes. The returned fn requires
  keyword for the route, and two optional maps for path- and query-params."
  [request]
  (if-let [path-for (get request ::url)]
    path-for
    (do
      (log/errorf "Bidi routes missing")
      (constantly "missing-routes"))))
