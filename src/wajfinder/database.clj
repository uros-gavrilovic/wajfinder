(ns wajfinder.database
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [environ.core :refer [env]]))

(def neo4j-url "http://localhost:7474/db/neo4j/tx/commit")

(def username (env :neo4j-username))
(def password (env :neo4j-password))


(defn run-query
  "Runs a Cypher query on the Neo4j database."
  [cypher params]
  (let [body {:statements [{:statement cypher :parameters params}]}
        response (client/post neo4j-url
                              {:headers {"Content-Type" "application/json"}
                               :body (json/generate-string body)
                               :basic-auth [username password]
                               :throw-exceptions false})
        parsed (json/parse-string (:body response) true)]
    parsed))

(defn ping
  "Pings the Neo4J to test connection."
  []
  (let [result (run-query "RETURN 1" {})]
    (println "Pinging Neo4j database...")
    (if (get-in result [:results 0 :data 0 :row 0])
      {:status :ok :message "Neo4j is reachable"}
      {:status :error :message "Neo4j ping failed"})))