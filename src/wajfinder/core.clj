(ns wajfinder.core
  (:gen-class)
  (:require [wajfinder.database :as db]))

(defn -main
  [& args]
  (println (db/ping)))