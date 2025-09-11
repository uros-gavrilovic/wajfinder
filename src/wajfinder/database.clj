(ns wajfinder.database
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as str]
            [environ.core :refer [env]]))

(def neo4j-url (env :neo4j-url))
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

(defn purge!
  "Purges the database."
  []
  (run-query "MATCH (c:City) DETACH DELETE c" {})
  (run-query "MATCH (r:Road) DETACH DELETE r" {})
  {:status :ok :message "Cities and roads purged"})

(defn create-city!
  "Creates a city node in the database."
  [name]
  (run-query "CREATE (c:City {name: $name}) RETURN c"
             {:name name}))

(defn edit-city!
  "Edits a city's name and attributes."
  [old-name updates]
  (let [sets (->> updates
                  (map (fn [[k _]] (str "c." (name k) " = $" (name k))))
                  (str/join ", "))]
    (run-query (str "MATCH (c:City {name: $old}) "
                    "SET " sets " "
                    "RETURN c")
               (assoc updates :old old-name))))

(defn delete-city!
  "Deletes a city and its connected roads."
  [name]
  (run-query "MATCH (c:City {name: $name}) DETACH DELETE c"
             {:name name}))

(defn get-all-cities []
  (run-query "MATCH (c:City) RETURN c.name" {}))

(defn count-cities []
  (let [cities-result (get-all-cities)
        rows          (get-in cities-result [:results 0 :data])]
    (count rows)))

(defn create-road!
  "Creates a road relationship between two cities."
  [from to distance]
  (run-query
   "MATCH (a:City {name: $from}), (b:City {name: $to})
     CREATE (a)-[:ROAD {distance: $distance}]->(b)"
   {:from from :to to :distance distance}))

(defn get-all-roads []
  (run-query "MATCH (fromCity)-[road]->(toCity) RETURN fromCity.name, toCity.name, road" {}))

(defn count-roads []
  (let [roads-result (get-all-roads)
        rows        (get-in roads-result [:results 0 :data])]
    (count rows)))
