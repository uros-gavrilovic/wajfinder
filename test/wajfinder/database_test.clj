(ns wajfinder.database-test
  (:require [midje.sweet :refer :all]
            [wajfinder.database :as db]))

(facts "about the database connection"
       (fact "ping returns status :ok"
             (let [result (db/ping)]
               (:status result) => :ok
               (:message result) => "Neo4j is reachable"))

       (fact "ping returns a map"
             (let [result (db/ping)]
               (map? result) => true)))

(facts "about the purge function"
       (fact "after purging there should be no cities or roads"
             (let [purge-result (db/purge!)
                   city-count  (db/count-cities)
                   road-count  (db/count-roads)]
               (:status purge-result) => :ok
               city-count => 0
               road-count => 0)))

(facts "about City CRUD"
       (against-background
        [(before :facts (db/purge!))
         (after :facts (db/purge!))]

        (fact "create-city! adds a city"
              (db/create-city! "Novodimitrovsk")
              (db/count-cities) => 1)

        ;; {:results
        ;;  [{:columns ["c.name"]
        ;;  :data [{:row ["Chernaya Polana"]}
        ;; {:row ["Elektro"]}]}]}

        (fact "edit-city! updates city attributes"
              (db/create-city! "Polana")
              (db/edit-city! "Polana" {:name "Chernaya Polana" :population 100})
              (some #(= "Chernaya Polana" (get-in % [:row 0]))
                    (:data (first (:results (db/get-all-cities))))) => truthy)

        (fact "delete-city! removes a city"
              (db/create-city! "Elektrozavodsk")
              (db/count-cities) => 1
              (db/delete-city! "Elektrozavodsk")
              (db/count-cities) => 0)

        (fact "delete-city! removes city and its connected roads"
              (db/create-city! "Elektrozavodsk")
              (db/create-city! "Balota airfield")
              (db/create-road! "Elektrozavodsk" "Balota airfield" 5)
              (db/count-cities) => 2
              (db/count-roads) => 1
              (db/delete-city! "Elektrozavodsk")
              (db/count-cities) => 1
              (db/count-roads) => 0)))