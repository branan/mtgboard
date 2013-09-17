(ns mtgboard.db
  (:require [korma.db :refer [postgres default-connection]]
            [korma.core :refer :all]
            [ragtime.core :refer :all]
            [ragtime.sql.database :refer :all]))

(declare players matches)

(defentity players)
(defentity matches)

(defn list-players
  []
  (select players))

(defn list-matches
  []
  (select matches))

(defn list-matches-for-player
  [id]
  (select matches (where (or {:player1_id id} {:player2_id id}))))

(defn get-player
  [id]
  (first (select players (where {:id id}))))

(defn get-match
  [id]
  (first (select matches (where {:id id}))))

(defn create-player
  [name]
  (insert players (values {:name name})))

(defn create-match
  [player1 player2 score1 score2]
  (insert matches (values
                   {:player1_id player1
                    :player2_id player2
                    :player1_wins score1
                    :player2_wins score2})))

(defn init-schema-up!
  [_db]
  (exec-raw "CREATE TABLE players (id SERIAL PRIMARY KEY, name TEXT);")
  (exec-raw "CREATE TABLE matches (id SERIAL PRIMARY KEY, player1_id INTEGER REFERENCES players(id), player2_id INTEGER REFERENCES players(id), player1_wins INTEGER NOT NULL, player2_wins INTEGER NOT NULL);"))

(defn init-schema-down!
  [_db]
  (exec-raw "DROP TABLE players;")
  (exec-raw "DROP TABLE matches;"))

(def init-schema
  {:id "init-schema"
   :up init-schema-up!
   :down init-schema-down!})

(def migrations
  [init-schema])

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

(defn init!
  []
  (let [database (or (System/getenv "MTGBOARD_DB") "mtgboard")
        user (or (System/getenv "MTGBOARD_DBUSER") "mtgboard")
        password (or (System/getenv "MTGBOARD_DBPASSWORD") "password")
        host (System/getenv "MTGBOARD_DBHOST")
        port (System/getenv "MTGBOARD_DBPORT")
        db (postgres (dbconfig database user password host port))]
    (default-connection db)
    (migrate-all (map->SqlDatabase db) migrations)))