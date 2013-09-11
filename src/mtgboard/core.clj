(ns mtgboard.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]))

(defroutes v1-routes
  (GET "/players" [] "Listing players, yo")
  (GET "/players/:player" [player] "Listing a single player, yo")
  (GET "/matches" [] "Listing matches, yo. We should add query params to this one!")
  (GET "/matches/:match" [match] "Listing a single match, yo"))

(defroutes app-routes
  (context "/api/v1" [] v1-routes))

(def app
  (handler/site app-routes))

(defn init
  "I should be setting up a database connection"
  []
  (println "When I grow up, I'm going to make database connections just like my daddy!"))
