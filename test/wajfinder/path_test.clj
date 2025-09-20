(ns wajfinder.path-test
  (:require [midje.sweet :refer :all]
            [wajfinder.database :as db]
            [wajfinder.path :as path]))

(defn setup-graph []
  (db/purge!)
  (doseq [city ["A" "B" "C" "D" "E" "F"]]
    (db/create-city! city))
  (db/create-road! "A" "B" 2)
  (db/create-road! "A" "C" 4)
  (db/create-road! "B" "C" 3)
  (db/create-road! "B" "D" 1)
  (db/create-road! "B" "E" 5)
  (db/create-road! "C" "D" 2)
  (db/create-road! "D" "E" 1)
  (db/create-road! "E" "F" 2)
  (db/create-road! "D" "F" 4))

(fact "Dijkstra algorithm succesfully finds the shortest path between two nodes"
      (setup-graph)
      (let [result (path/dijkstra "A")]
        (:distance (get result "F")) => 6
        (path/path-to result "F") => ["A" "B" "D" "E" "F"]))
