(defproject wajfinder "0.1.0-SNAPSHOT"
  :description "WajFinder is a simple application that finds the optimal path between two cities.
                Uses Neo4j as the database. 
                Inspired by Bohemia Interactive's DayZ game map."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repositories [["clojars" "https://repo.clojars.org/"]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [midje "1.10.10"] ; Testing framework
                 [ring "1.12.0"] ; Web requests and responses
                 [ring/ring-mock "0.6.2"] ; Mocks REST requests
                 [compojure "1.7.0"] ; Routing for Web requests
                 [clj-http "3.12.3"] ; HTTP client
                 [cheshire "5.11.0"] ; JSON parser
                 [environ "1.2.0"] ; Environment variable handling
                 [ring/ring-core "1.12.2"] ; REST requests and responses
                 [ring/ring-jetty-adapter "1.12.2"]] ; Web server
  :plugins [[lein-environ "1.2.0"]
            [lein-midje "3.2.1"]
            [lein-cljfmt "0.8.2"]] ; Code formatter
  :main ^:skip-aot wajfinder.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})