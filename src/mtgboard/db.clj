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

(def migrations [init-schema])

(defn init!
  []
  (let [database (or (System/getenv "MTGBOARD_DB") "mtgboard")
        user (or (System/getenv "MTGBOARD_DBUSER") "mtgboard")
        password (or (System/getenv "MTGBOARD_DBPASSWORD") "password")
        db (postgres {:db database :user user :password password})]
    (default-connection db)
    (migrate-all (map->SqlDatabase db) migrations)))