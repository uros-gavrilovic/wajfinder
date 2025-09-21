(ns wajfinder.rest
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [wajfinder.path :as path]
            [wajfinder.database :as db]))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json"}
   :body    (json/generate-string data)})

(defroutes wajfinder-routes
  (GET "/ping" []
    (json-response (db/ping)))

  (DELETE "/purge" []
    (json-response (db/purge!)))

  (GET "/cities" []
    (json-response (db/get-all-cities)))

  (POST "/cities" [name]
    (let [city (db/create-city! name)]
      (json-response city 201)))

  (PUT "/cities/:old-name" [old-name :as req]
    (let [body-string (slurp (:body req))
          updates   (json/parse-string body-string true)]
      (json-response (db/edit-city! old-name updates))))

  (DELETE "/cities/:name" [name]
    (json-response (db/delete-city! name)))

  (GET "/roads" []
    (json-response (db/get-all-roads)))

  (POST "/roads" [from to distance]
    (let [dist (Long/parseLong distance)]
      (json-response (db/create-road! from to dist))))

  (PUT "/roads" [:as req]
    (let [body-string (slurp (:body req))
          body-json (json/parse-string body-string true)
          from      (:from body-json)
          to        (:to body-json)
          updates   (:updates body-json)]
      (json-response (db/edit-road! from to updates))))

  (DELETE "/roads" [from to]
    (json-response (db/delete-road! from to)))

  (GET "/shortest-path/:from/:to" [from to]
    (cond
      (not (db/has-city? from))
      (json-response {:error (str "City " from " not found")} 404)
      (not (db/has-city? to))
      (json-response {:error (str "City " to " not found")} 404)

      :else
      (let [nodes (path/dijkstra from)
            shortest-path (path/path-to nodes to)]
        (if (empty? shortest-path)
          (json-response {:error (str "No path found between " from " and " to)} 404)
          (json-response {:path shortest-path
                          :distance (:distance (nodes to))})))))


  (route/not-found (json-response {:error "Not Found"} 404)))
