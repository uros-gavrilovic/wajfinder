(ns wajfinder.core
  (:gen-class)
  (:require [wajfinder.database :as db]))

(defn -main
  [& args]
  (db/purge!)
  (db/create-city! "Chernogorsk")
  (db/create-city! "Berezino")
  (db/create-road! "Chernogorsk" "Berezino" 100)
  (println (db/count-cities)))

(-main)