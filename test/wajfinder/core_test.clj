(ns wajfinder.core-test
  (:require [midje.sweet :refer :all]
            [wajfinder.core :as core]))

(facts "Core namespace"
       (fact "Main function should exist and be callable")
       (core/-main) => nil)
