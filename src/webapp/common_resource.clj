(ns webapp.common-resource
  "Lots of common resource goodness, helper functions &c."
  (:require [liberator.core :refer [negotiate-media-type]]
            [schema.utils :refer [error?]]
            [clojure.tools.logging :as log]
            [cemerick.friend :as friend])
  (:import (javax.xml.ws ProtocolException)))

(defn- make-malformed-coercer
  "A wrapper for checking the malformed state using Schema coercers.
  Liberator malformed expects [malformed? {:some data}] to be returned
  from the function.

  There is a known
  issue (https://github.com/clojure-liberator/liberator/issues/94) to
  work around so that we can return the errors in a rendered form if
  we need to."
  [coerce-fn]
  (fn malformed? [ctx]
    (let [result (coerce-fn ctx)]
      (log/info result)
      (if (error? result)
        [true (merge result
                     (try (negotiate-media-type ctx)
                          (catch ProtocolException _
                            {:representation {:media-type "application/json"}})))]
        [false {:coerced-params result}]))))

(def base
  {:allowed-methods       [:get :post]
   :available-media-types ["text/html" "application/json"]})

(def user-roles
  #{:account/user :account/admin})

(def authorised-base
  (merge base
         {:authorized? (fn [{:keys [request] :as ctx}]
                         (friend/authorized? user-roles (friend/identity request)))}))

(def post
  (merge base
         {:allowed-methods [:post]}))

(defn do-post
  "A simple post endpoint."
  [check-vals-coercer exists? post! post-redirect? & {:as overrides}]
  (merge post
         {:malformed?     (make-malformed-coercer check-vals-coercer)
          :exists?        exists?
          :post!          post!
          :post-redirect? post-redirect?}
         overrides))

(defn do-get
  "A simple get endpoint."
  [check-vals-coercer exists? render & {:as overrides}]
  (merge base
         {:allowed-methods [:get]
          :malformed? (make-malformed-coercer check-vals-coercer)
          :exists?         exists?
          :handle-ok       render}
         overrides))
