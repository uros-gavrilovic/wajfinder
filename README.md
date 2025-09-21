# wajfinder

WaJfinder is Clojure based project that uses Djikstra alogirthm to find the shortest path between two cities.
It started out as a hobby project to find out the shortest path between cities for a video game called DayZ so I could quickly traverse the map.

## Installation

1) Pull the project on 'master' branch from GitHub
2) Install Docker
3) Change directory to the project, and use command `docker compose up` to start Neo4J database
4) Create folder profiles.clj to create user specific enviroment variables (eg. below)
   ```
   {:dev {:env {:server-port 9000
             :neo4j-url "http://localhost:7474/db/neo4j/tx/commit"
             :neo4j-username "neo4j"
             :neo4j-password "password"}}}
   ```
5) Start the project with either `lein repl` and manually start the server or with `lein run` that will start it automatically

## Usage

Here is [REST API collection](https://mastercode-5328.postman.co/workspace/5f1a621d-6ddc-4aa4-bc34-bd99359c9d19/collection/27135517-06826996-8a5f-4a7b-983e-bed670f3fc36?action=share&source=copy-link&creator=27135517) in Postman
if you want to test it directly through the HTTP requests.

### Bugs
There are absolutely no bugs whatsoever and everything works as it should be. And if it doesn't, it's a feature.

### What might be coming up
I could add another algorigthm that could theoretically weight different routes between cities depending on the probability of a bandit attack, road quality, resource availability.
So if a road is difficult to traverse, it will have higher weight index, therefore it'll be likely replaced by a different, "easier" to traverse road.
