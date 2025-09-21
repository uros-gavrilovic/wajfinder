(ns wajfinder.core-test
  (:require [midje.sweet :refer :all]
            [wajfinder.core :as core]))

(facts "Core namespace"
       (fact "Starting and stopping the server works"
             (core/start-server!)
             (core/stop-server!)
             truthy))
