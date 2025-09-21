(ns wajfinder.core
  (:require [wajfinder.rest :refer [wajfinder-routes]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            ;; [wajfinder.database :as db]
            [environ.core :refer [env]]))

(def server (atom nil))
(def server-port (Integer/parseInt (env :server-port "9000")))

(def app
  (-> wajfinder-routes
      wrap-keyword-params
      wrap-params))

(defn start-server!
  "Starts Wajfinder server on port given by the enviroment variables"
  []
  (when-not @server
    (println "Starting WajFinder on port" server-port "...")
    (reset! server (run-jetty app {:port server-port :join? false}))))

(defn stop-server!
  "Stops the running WajFinder server"
  []
  (when @server
    (println "Stopping WajFinder...")
    (.stop @server)
    (reset! server nil)))

(defn -main
  []
  (start-server!))

;; (start-server!)
;; (stop-server!)

;; (db/create-city! "ue")
;; (db/get-all-cities)
;; (db/purge!)