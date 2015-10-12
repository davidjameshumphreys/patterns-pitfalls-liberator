(defproject patterns-pitfalls-liberator "0.1.0-SNAPSHOT"
  :description "Sample code written for a talk on Liberator"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :pedantic? true
  :source-paths ["dev" "src" "test" "migrators"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.1.16"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.stuartsierra/component "0.2.2"]
                 [bidi "1.10.4"
                  :exclusions [org.clojure/clojurescript
                               org.clojure/data.json
                               com.keminglabs/cljx]]
                 [liberator "0.13"
                  :exclusions [org.clojure/tools.logging]]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.2.2"
                  :exclusions [org.clojure/tools.reader]]

                 ;; handlebars for output templates
                 [hbs "0.8.0"]
                 ;; the logging framework for the project has been decided by one library!
                 [org.slf4j/slf4j-simple "1.7.10"]

                 [ring/ring-codec "1.0.0"]
                 [ring/ring-json "0.3.1"]
                 [ring-mock "0.1.5"]
                 [org.clojure/data.json "0.2.5"]
                 [prismatic/schema "0.3.7"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [com.cemerick/friend "0.2.0"]

                 [com.h2database/h2 "1.4.189"]
                 [com.mchange/c3p0 "0.9.5.1"]
                 [joplin.core "0.3.3"]
                 [joplin.jdbc "0.3.3"]
                 [honeysql "0.6.1"]]

  :aliases {"seed" ["run" "-m" "joplin-alias/seed"]
            "up"   ["run" "-m" "joplin-alias/migrate" "dev" "sql-dev"]})
