(ns mtgboard.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :refer [response redirect]]
            [ring.middleware.json :as json]
            [mtgboard.db :as db]))


(defn filter-player-for-match
  "Return only the keys we want when player data is embedded in a match response"
  [player]
  (select-keys player [:name :wins :id]))

(defn fixup-match
  [match]
  (let [player1 (assoc (db/get-player (:player1_id match)) :wins (:player1_wins match))
        player2 (assoc (db/get-player (:player2_id match)) :wins (:player2_wins match))]
    (-> match
        (assoc :players (map filter-player-for-match [player1 player2]))
        (dissoc :player1_id :player2_id :player1_wins :player2_wins))))

(defn get-match
  [match]
  (let [id (Integer/valueOf match)
        match (db/get-match id)]
    (fixup-match match)))

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
       (response (get-match match)))
  (POST "/players" [name]
        (response (db/create-player name)))
  (POST "/matches" [player1_id player2_id player1_wins player2_wins]
        (response (db/create-match player1_id player2_id player1_wins player2_wins))))

(defroutes app-routes
  (GET "/" [] (redirect "/index.html"))
  (route/resources "/")
  (context "/api/v1" [] (-> v1-routes json/wrap-json-response json/wrap-json-params)))

(def app
  (handler/site app-routes))

(defn add-field
  [conf val key]
  (if val
    (assoc conf key val)
    conf))

(defn dbconfig
  [db user pass host port]
  (-> {:db db :user user :password pass}
      (add-field host :host)
      (add-field port :port)))

(defn init
  []
  (let [database (or (System/getenv "MTGBOARD_DB") "mtgboard")
        user (or (System/getenv "MTGBOARD_DBUSER") "mtgboard")
        password (or (System/getenv "MTGBOARD_DBPASSWORD") "password")
        host (System/getenv "MTGBOARD_DBHOST")
        port (System/getenv "MTGBOARD_DBPORT")]
    (db/init! (dbconfig database user password host port))))
