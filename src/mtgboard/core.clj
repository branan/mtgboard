(ns mtgboard.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [ring.util.response :refer [response]]
            [ring.middleware.json :as json]
            [mtgboard.db :as db]))


(defn filter-player-for-match
  "Return only the keys we want when player data is embedded in a match response"
  [player]
  (select-keys player [:name :wins :id]))

(defn get-match
  [match]
  (let [id (Integer/valueOf match)
        match (db/get-match id)
        player1 (assoc (db/get-player (:player1_id match)) :wins (:player1_wins match))
        player2 (assoc (db/get-player (:player2_id match)) :wins (:player2_wins match))]
    (-> match
        (assoc :players (map filter-player-for-match [player1 player2]))
        (dissoc :player1_id :player2_id :player1_wins :player2_wins))))

(defn get-player
  [player]
  (let [id (Integer/valueOf player)
        player (db/get-player id)
        matches (map #(get-match (:id %)) (db/list-matches-for-player id))]
    (assoc player :matches matches)))

(defroutes v1-routes
  (GET "/players" []
       (response (db/list-players)))
  (GET "/players/:player" [player]
       (response (get-player player)))
  (GET "/matches" []
       (response (db/list-matches)))
  (GET "/matches/:match" [match]
       (response (get-match match))))

(defroutes app-routes
  (context "/api/v1" [] (-> v1-routes json/wrap-json-response)))

(def app
  (handler/site app-routes))

(defn init
  []
  (db/init!))
