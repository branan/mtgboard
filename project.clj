(defproject mtgboard "0.1.0-SNAPSHOT"
  :description "Leaderboard for Magic: The Gathering League Play"
  :plugins [[lein-ring "0.8.5"]]
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [ragtime "0.3.3"]
                 [korma "0.3.0-RC5"]
                 [postgresql "9.1-901.jdbc4"]]
  :ring {:handler mtgboard.core/app
         :init mtgboard.core/init
         :port 5893
         :nrepl {:start? true}})
