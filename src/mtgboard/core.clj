(ns mtgboard.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :refer [response redirect]]
            [ring.middleware.json :as json]
            [ring.middleware.params :as params]
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
        matches (map #(db/get-match (:id %)) (db/list-matches-for-player id))]
    (assoc player :matches matches)))

(defn player-score
  [id match]
  (if (= id (:player1_id match))
    (:player1_wins match)
    (:player2_wins match)))

(defn opponent-score
  [id match]
  (if (= id (:player1_id match))
    (:player2_wins match)
    (:player1_wins match)))

(defn games-won
  [id matches]
  (reduce + 0 (map #(player-score id %) matches)))

(defn games-lost
  [id matches]
  (reduce + 0 (map #(opponent-score id %) matches)))

(defn winning-matches
  [matches player]
  (filter #(= (:player1_id %) player) matches))

(defn get-player-score
  [player]
  (let [id (Integer/valueOf (:id player))
        matches (map #(db/get-match (:id %)) (db/list-matches-for-player id))
        total (count matches)
        wins (count (winning-matches matches id))]
    (-> player
        (assoc :matches-won wins)
        (assoc :matches-lost (- total wins))
        (assoc :games-won (games-won id matches))
        (assoc :games-lost (games-lost id matches))
        (assoc :rating (if-not (zero? total)
                         (/ wins total)
                         -1)))))

(defn show-leaderboard
  []
  (->> (db/list-players)
       (map get-player-score)
       (sort-by :rating)
       (reverse)))

(defn list-players
  []
  (sort-by :name (db/list-players)))

(defroutes v1-routes
  (GET "/leaderboard" []
       (response (show-leaderboard)))
  (GET "/players" []
       (response (list-players)))
  ;; (GET "/players/:player" [player]
  ;;      (response (get-player player)))
  ;; (GET "/matches" []
  ;;      (response (db/list-matches)))
  ;; (GET "/matches/:match" [match]
  ;;      (response (get-match match)))
  (POST "/players" [name]
        (response (db/create-player name)))
  (POST "/matches" [winner loser winner-score loser-score]
        (let [winner (Integer/valueOf winner)
              loser (Integer/valueOf loser)
              winner-score (Integer/valueOf winner-score)
              loser-score (Integer/valueOf loser-score)]
          (if (< winner-score loser-score)
            (response "")
            (response (db/create-match winner loser winner-score loser-score))))))

(defroutes app-routes
  (GET "/" [] (redirect "/index.html"))
  (route/resources "/")
  (context "/api/v1" [] (-> v1-routes params/wrap-params json/wrap-json-response json/wrap-json-params)))

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
