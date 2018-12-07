(defproject clj-dynamodb-local "0.1.0"
  :description "A Leiningen plugin and boot task for providing a local DynamoDB instance"
  :url "https://github.com/dmcgillen/clj-dynamodb-local"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[environ "1.0.0"]
                 [net.lingala.zip4j/zip4j "1.3.2"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]]
                   :plugins [[lein-midje "3.1.3"]]}}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :creds :gpg}]])
