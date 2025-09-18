(ns wajfinder.path
  (:require [wajfinder.database :as db]))

(defn init-graph []
  (let [cities (db/get-all-cities)
        roads  (db/get-all-roads)]
    (reduce (fn [acc {:keys [from to distance]}]
              (-> acc
                  (update-in [from :adjacent-nodes] assoc to distance)))
            (into {} (map (fn [{:keys [name]}]
                            [name {:name name
                                   :distance ##Inf
                                   :shortest-path []
                                   :adjacent-nodes {}}])
                          cities))
            roads)))

(defn evaluate-distance-and-path [adj-node edge-weight source-node]
  (let [new-distance (+ (:distance source-node) edge-weight)]
    (if (< new-distance (:distance adj-node))
      (-> adj-node
          (assoc :distance new-distance)
          (assoc :shortest-path (conj (:shortest-path source-node)
                                      (:name source-node))))
      adj-node)))

(defn djikstra [source-name]
  (let [graph (init-graph)
        source (assoc (graph source-name) :distance 0)]
    (loop [settled #{}
           unsettled (sorted-set-by #(compare (:distance %1) (:distance %2)) source)
           nodes (assoc graph source-name source)]
      (if (empty? unsettled)
        nodes
        (let [current (first unsettled)
              adj (:adjacent-nodes current)
              updated-nodes
              (reduce (fn [acc [neighbor-name weight]]
                        (if (settled neighbor-name)
                          acc
                          (let [adj-node (get acc neighbor-name)
                                updated (evaluate-distance-and-path adj-node weight current)]
                            (assoc acc neighbor-name updated))))
                      nodes adj)]
          (recur (conj settled (:name current))
                 (into (disj unsettled current)
                       (map #(get updated-nodes %) (keys adj)))
                 updated-nodes))))))

;; {C {:name C, :distance 4, :shortest-path [A], :adjacent-nodes {D 2}},
;;  D {:name D, :distance 3, :shortest-path [A B], :adjacent-nodes {E 1, F 4}},
;;  E {:name E, :distance 4, :shortest-path [A B D], :adjacent-nodes {F 2}},
;;  F {:name F, :distance 6, :shortest-path [A B D E], :adjacent-nodes {}},
;;  A {:name A, :distance 0, :shortest-path [], :adjacent-nodes {B 2, C 4}},
;;  B {:name B, :distance 2, :shortest-path [A], :adjacent-nodes {C 3, D 1, E 5}}}

(defn path-to
  "Returns an array of nodes which represents the shortest path to the target node"
  [nodes target-node]
  (println nodes)
  (let [node (get nodes target-node)]
    (if (nil? node)
      [] ; unreahcable
      (conj (:shortest-path node) (:name node)))))
