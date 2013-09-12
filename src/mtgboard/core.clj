(ns mtgboard.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [mtgboard.db :as db]))

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
  []
  (db/init!))
