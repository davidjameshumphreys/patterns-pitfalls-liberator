(ns webapp.schemata
  (:require [schema.core :as schema]
            [clojure.string :as str]
            [schema.coerce :as coerce]
            [schema.utils :refer [error? error]]
            [clojure.tools.logging :as log]))

(def email
  "An email is just a string, but we want to deal with it differently
  to other strings (lowercase everything)."
  (schema/named schema/Str "email"))

(def valid-email
  (schema/named
    (schema/both
      email
      (schema/pred #(re-find #"[a-zA-Z0-9_.]+@[a-z]+\.[a-z]+")))
    "must be a valid email address"))

(def color-names
  {:black [  0   0   0]
   :white [255 255 255]
   :red   [255   0   0]
   :green [  0 255   0]
   :blue  [  0   0 255]})

(defn triple [st]
  (log/trace "triple" st)
  (condp = (count st)
    6
    (map #(Integer/parseInt (apply str %), 16) (partition 2 st))
    3
    (map #(* 16 (Integer/parseInt (str %) 16)) st)))

(defn- good? [n s]
  (log/info "good" n s)
  (re-matches (re-pattern (format "(?i)[A-F0-9]{%d}" n)) s))

(def triple-color
  (schema/named (schema/pred #(= 3 (count %))) "three-color"))

(def named-color
  (schema/named (schema/pred #(= 3 (count %))) "named-color"))

(defn name->color [s]
  (log/trace "named" s)
  (-> s
      keyword
      color-names))

(def color
  (schema/both schema/Str
               (schema/either
                 triple-color
                 named-color)))

(def month (schema/both schema/Int
                        (apply schema/enum (range 1 (inc 12)))))
(def day (schema/both schema/Int (apply schema/enum (range 1 (inc 31)))))

(def color-coerce
  "A function that will take input that matches the schema using the type matches."
  (coerce/coercer {:color color} {schema/Str   (coerce/safe (comp str/lower-case str/trim))
                                  triple-color (coerce/safe triple)
                                  named-color  (coerce/safe name->color)}))

(def d29
  (schema/pred #(<= 1 % 29)))
(def d30
  (schema/pred #(<= 1 % 30)))
(def d31
  (schema/pred #(<= 1 % 31)))

(defn valid-day
  "Create a validation that restricts the day of the month based on the month given."
  [m]
  (let [months  (zipmap (next (range))
                        ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])
        allowed (zipmap (next (range))
                        [d31   d29   d31    d30   d31   d30   d31   d31   d30   d31   d30   d31])
        current (get allowed m)]
    (schema/named
      current
      (format "Valid days for %s" (get months m)))))

(def date-coerce
  "Coerce the user's favourite day of the year."
  (coerce/coercer {:day   day
                   :month month}
                  {schema/Str (coerce/safe str/trim)
                   schema/Int (coerce/safe (comp #(Integer/parseInt %) str/trim))}))

(defn check-date [input-map]
  (let [first-check (date-coerce input-map)]
    (if-not (error? first-check)
      (try
        (schema/validate {:day   (valid-day (:month first-check))
                          :month schema/Any}
                         first-check)
        (catch Exception e
          (error e)))
      first-check)))


(def valid-name
  (coerce/coercer {:id (schema/both schema/Str (schema/pred #(>= 24 (count %))))} {schema/Str (coerce/safe str/trim)}))