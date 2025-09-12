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
  (let [result (run-query "MATCH (c:City) RETURN c.name AS name" {})]
    (mapv (fn [row]
            {:name (get-in row [:row 0])})
          (get-in result [:results 0 :data]))))

(defn count-cities []
  (count (get-all-cities)))

(defn create-road!
  "Creates a road relationship between two cities."
  [from to distance]
  (run-query
   "MATCH (a:City {name: $from}), (b:City {name: $to})
     CREATE (a)-[:ROAD {distance: $distance}]->(b)"
   {:from from :to to :distance distance}))

(defn edit-road!
  "Updates a road between two cities."
  [from to updates]
  (let [sets (->> updates
                  (map (fn [[k _]] (str "r." (name k) " = $" (name k))))
                  (str/join ", "))]
    (run-query
     (str "MATCH (a:City {name: $from})-[r:ROAD]->(b:City {name: $to}) "
          "SET " sets " "
          "RETURN r")
     (assoc updates :from from :to to))))

(defn delete-road!
  "Deletes a road between two cities."
  [from to]
  (run-query
   "MATCH (a:City {name: $from})-[r:ROAD]->(b:City {name: $to})
    DELETE r"
   {:from from :to to}))

(defn get-all-roads []
  (let [result (run-query
                "MATCH (fromCity)-[road:ROAD]->(toCity)
                 RETURN fromCity.name AS from, toCity.name AS to, road.distance AS distance"
                {})]
    (mapv (fn [row]
            {:from     (get-in row [:row 0])
             :to       (get-in row [:row 1])
             :distance (get-in row [:row 2])})
          (get-in result [:results 0 :data]))))

(defn count-roads []
  (count (get-all-roads)))