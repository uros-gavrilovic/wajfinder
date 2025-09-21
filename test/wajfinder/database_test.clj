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

        (fact "edit-city! updates city attributes"
              (db/create-city! "Polana")
              (db/edit-city! "Polana" {:name "Chernaya Polana" :population 100})
              (let [cities (db/get-all-cities)]
                (some #(= "Chernaya Polana" (:name %)) cities) => truthy))

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

(facts "about road CRUD"
       (against-background
        [(before :facts (db/purge!))
         (after :facts (db/purge!))]

        (fact "create-road! adds a road between two cities"
              (db/create-city! "Novodimitrovsk")
              (db/create-city! "Svetlojarsk")
              (db/create-road! "Novodimitrovsk" "Svetlojarsk" 100)
              (db/count-cities) => 2
              (db/count-roads) => 1))

       (fact "delete-road! removes a road between two cities"
             (db/create-city! "Novodimitrovsk")
             (db/create-city! "Svetlojarsk")
             (db/create-road! "Novodimitrovsk" "Svetlojarsk" 100)
             (db/count-roads) => 1
             (db/delete-road! "Novodimitrovsk" "Svetlojarsk")
             (db/count-roads) => 0)

       (fact "edit-road! updates road attributes"
             (db/create-city! "Novodimitrovsk")
             (db/create-city! "Svetlojarsk")
             (db/create-road! "Novodimitrovsk" "Svetlojarsk" 100)
             (db/edit-road! "Novodimitrovsk" "Svetlojarsk" {:distance 150})
             (let [road (first (db/get-all-roads))]
               (:distance road) => 150
               (:from road) => "Novodimitrovsk"
               (:to road) => "Svetlojarsk")))

(facts "about has-city?"
       (against-background
        [(before :facts (db/purge!))
         (after :facts (db/purge!))]

        (fact "fails if city does not exist"
              (db/has-city? "Nadezdhino") => false)

        (fact "returns true if city exists"
              (db/create-city! "Markovo")
              (db/has-city? "Markovo") => true)

        (fact "returns false after the city is deleted"
              (db/delete-city! "Pavlovo")
              (db/has-city? "Pavlovo") => false)))