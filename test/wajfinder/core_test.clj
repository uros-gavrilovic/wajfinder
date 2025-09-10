(ns wajfinder.core-test
  (:require  [midje.sweet :refer [facts fact => throws roughly just]]
             [wajfinder.database :as db]))

(facts "about the database connection"
    (fact "ping should return status :ok when Neo4j is reachable"
        (let [result (db/ping)]
        (:status result) => :ok
        (:message result) => "Neo4j is reachable")))