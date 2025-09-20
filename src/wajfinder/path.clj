(ns wajfinder.path
  (:require [wajfinder.database :as db]))

(defn init-graph
  "Loads the graph from the Neo4J database into memory"
  []
  (let [cities (db/get-all-cities) ; [{:name "A"} {:name "B"} ...]
        roads  (db/get-all-roads)] ; [{:from "A", :to "B", :distance 2} ...]
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

(defn evaluate-distance-and-path
  "Updates the adjacent node if there is a shorter path through the given source node"
  [adj-node edge-weight source-node]
  (println "adjacent: " adj-node)
  (println "start node:" source-node)
  (let [new-distance (+ (:distance source-node) edge-weight)]
    (println "new distance: " new-distance)
    (println "current distance: " (:distance adj-node))
    (if (< new-distance (:distance adj-node))
      (-> adj-node
          (assoc :distance new-distance)
          (assoc :shortest-path (conj (:shortest-path source-node)
                                      (:name source-node))))
      adj-node)))

(defn init-dijkstra
  "Initializes unsettled and settled sets and sets the distance of the source node from infinity to 0"
  [source-name]
  (let [graph (init-graph)
        source (assoc (graph source-name) :distance 0)]
    {:settled #{}
     :unsettled (sorted-set-by #(compare (:distance %1) (:distance %2)) source)
     :nodes (assoc graph source-name source)}))

(defn process-current-node
  "Processes the current node, updating distances and paths for its adjacent nodes, aka. relaxation step"
  [nodes settled current] ; current - node with the smallest distance in unsettled set
  (let [adjacent (:adjacent-nodes current)]
    (reduce (fn [acc [neighbor-name weight]]
              (if (settled neighbor-name)
                acc ; do not touch settled nodes
                (let [neighbor (get acc neighbor-name)
                      updated-neighbor (evaluate-distance-and-path neighbor weight current)]
                  (assoc acc neighbor-name updated-neighbor)))) ; update adjacent node with possibly shorter path
            nodes adjacent)))

(defn dijkstra
  "Main function that executes Dijkstra algorithm to find the shortest path to given source node"
  [source-name]
  (loop [{:keys [settled unsettled nodes]} (init-dijkstra source-name)]
    (if (empty? unsettled)
      nodes ; all shortest paths are found
      (let [current (first unsettled) ; grab the node with the smallest distance in the unsettled collection
            updated-nodes (process-current-node nodes settled current)
            new-settled (conj settled (:name current))
            neighbor-names (keys (:adjacent-nodes current))
            new-unsettled (into (disj unsettled current)
                                (map #(get updated-nodes %) neighbor-names))]
        (recur {:settled new-settled ; call the func again but with new arguments
                :unsettled new-unsettled
                :nodes updated-nodes})))))

;; {C {:name C, :distance 4, :shortest-path [A], :adjacent-nodes {D 2}},
;;  D {:name D, :distance 3, :shortest-path [A B], :adjacent-nodes {E 1, F 4}},
;;  E {:name E, :distance 4, :shortest-path [A B D], :adjacent-nodes {F 2}},
;;  F {:name F, :distance 6, :shortest-path [A B D E], :adjacent-nodes {}},
;;  A {:name A, :distance 0, :shortest-path [], :adjacent-nodes {B 2, C 4}},
;;  B {:name B, :distance 2, :shortest-path [A], :adjacent-nodes {C 3, D 1, E 5}}}

(defn path-to
  "Returns an array of nodes which represents the shortest path to the target node,
   or empty array if it's unreachable"
  [nodes target-node]
  (println nodes)
  (let [node (get nodes target-node)]
    (if (nil? node)
      [] ; unreachable node
      (conj (:shortest-path node) (:name node)))))
