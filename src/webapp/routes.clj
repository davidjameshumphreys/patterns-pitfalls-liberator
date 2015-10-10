(ns
  webapp.routes
  "Combines all of the HTTP handlers, with the exposed routes
with middleware."
  (:require [bidi.bidi :as b]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes]]
            [liberator.dev :refer [wrap-trace]]
            [compojure.route :as C :refer [not-found resources]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [webapp.bidi-helper :refer [enable-bidi-path-for]]
            [ring.util.response :refer [resource-response]]
            [webapp.examples.ends :as ex]
            [webapp.fav.color :refer [color-post color-get]]
            [webapp.fav.date :refer [date-post date-get]]
            [webapp.render-engine :refer [output-render]]))

(defn- idx [_]
  (resource-response "/index.html"))

(def ^{:private true
       :doc     "This allows us to refer to all of our routes by keywords."}
     handlers
  {:main/index #'idx
   :color/post #'color-post
   :color/get  #'color-get
   :date/post  #'date-post
   :date/get   #'date-get
   :simple/get #'webapp.examples.ends/simple-get
   :simple/get-type #'webapp.examples.ends/simple-get-with-type})

(defn- build-routes
  "Given the handler map, build the routes."
  [hm]
  ["/"
   {:get  [["" (:main/index hm)]
           ["fav/color/" (:color/get hm)]
           ["fav/date/" (:date/get hm)]
           ["simple/" (:simple/get hm)]
           ["simple-type/" (:simple/get-type hm)]]
    :post [["fav/color/" (:color/post hm)]
           ["fav/date/" (:date/post hm)]
           ["simple-type/" (:simple/get-type hm)]]}])

(defroutes server-routes
  (b/make-handler (build-routes handlers))
  (C/resources "/static" {:root "static"})
  (not-found (-> "not-found.html"
                 io/resource
                 (slurp :encoding "UTF-8"))))

(defn wrap-bidi-handlers
  "Wraps a path-for function that can be used in any handler to lookup a route"
  [handler bidi-routes-fn bidi-handlers]
  (fn wrap-bidi-handlers-fn [request]
    (handler (enable-bidi-path-for request bidi-routes-fn bidi-handlers))))

(defn wrap-renderer
  "Middleware to assoc a template renderer that knows about global vars at the application level."
  [handler globs]
  (fn [req]
    (let [modified-req (assoc req :global-render-vars globs)
          func         (partial output-render modified-req)]
      (handler (assoc modified-req
                 :render-fn func)))))

(defn wrap-database
  [handler db]
  (fn [req]
    (let [modified-req (assoc req :db db)]
      (handler modified-req))))

(defn the-application
  "All of the webapp routes and middleware.  By defining in this way we can pass in various settings (i.e. to add a database create a middleware to wrap the request)"
  [component-settings]
  (log/infof "Keys available : %s" (with-out-str (clojure.pprint/pprint (keys component-settings))))
  (-> #'server-routes
      (wrap-trace :ui true)  ;; <- liberator trace
      (wrap-renderer (-> component-settings :render :global-vars))
      (wrap-database (-> component-settings :db))
      wrap-keyword-params
      wrap-params
      wrap-json-params
      (wrap-bidi-handlers build-routes handlers)
      ))
