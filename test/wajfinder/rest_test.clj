(ns wajfinder.rest-test
  (:require [midje.sweet :refer :all]
            [wajfinder.rest :refer :all]
            [wajfinder.database :as db]
            [ring.mock.request :as mock]
            [cheshire.core :as json]))

(facts "REST API endpoints"
       (against-background
        [(before :facts (db/purge!))
         (after :facts  (db/purge!))]

        (fact "GET /ping returns status 200 OK"
              (let [response (wajfinder-routes (mock/request :get "/ping"))
                    body     (json/parse-string (:body response) true)]
                (:status response) => 200
                (:status body)   => "ok"
                (:message body)  => "Neo4j is reachable"))

        (fact "GET /cities returns empty list after purge"
              (let [response (wajfinder-routes (mock/request :get "/cities"))
                    body     (json/parse-string (:body response) true)]
                (:status response) => 200
                body => []))

        (fact "POST /cities creates a new city"
              (let [request  (-> (mock/request :post "/cities")
                                 (mock/body (json/generate-string {:name "Novodimitrovsk"}))
                                 (mock/content-type "application/json"))
                    response (wajfinder-routes request)]
                (:status response) => 201))

        (fact "DELETE /cities/:name removes a city"
              (db/create-city! "Rify")
              (let [response (wajfinder-routes (mock/request :delete "/cities/Rify"))]
                (:status response) => 200
                (db/has-city? "Rify") => false))

        (fact "GET /shortest-path/:from/:to returns error if city missing"
              (let [response (wajfinder-routes (mock/request :get "/shortest-path/Chernogorsk/Chapvesk"))
                    body     (json/parse-string (:body response) true)]
                (:status response) => 404
                (:error body) => "City Chernogorsk not found"))

        (fact "GET /shortest-path/:from/:to returns a valid path if it exists"
              (db/create-city! "Shakovka")
              (db/create-city! "Guglovo")
              (db/create-road! "Shakovka" "Guglovo" 5)
              (let [response (wajfinder-routes (mock/request :get "/shortest-path/Shakovka/Guglovo"))
                    body     (json/parse-string (:body response) true)]
                (:status response) => 200
                (:path body) => ["Shakovka" "Guglovo"]
                (:distance body) => 5))))