(ns wajfinder.core
  (:gen-class)
  (:require [wajfinder.database :as db]
            [wajfinder.path :as path]))

(defn -main
  [& args]
  (db/purge!)

  (db/create-city! "A")
  (db/create-city! "B")
  (db/create-city! "C")
  (db/create-city! "D")
  (db/create-city! "E")
  (db/create-city! "F")

  (db/create-road! "A" "B" 2)
  (db/create-road! "A" "C" 4)
  (db/create-road! "B" "C" 3)
  (db/create-road! "B" "D" 1)
  (db/create-road! "B" "E" 5)
  (db/create-road! "C" "D" 2)
  (db/create-road! "D" "E" 1)
  (db/create-road! "E" "F" 2)
  (db/create-road! "D" "F" 4)

  (def djikstra-result (path/djikstra "A"))
  (println djikstra-result)
  (println (path/path-to djikstra-result "F"))

  (println "Starting WajFinder..."))

(-main)