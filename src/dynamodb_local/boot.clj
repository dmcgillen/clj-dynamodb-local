(ns dynamodb-local.boot
  {:boot/export-tasks true}
  (:require [boot.core :as boot]
            [boot.util :as util]
            [clojure.string :as string]
            [dynamodb-local.core :as core]))

(defn- info
  [& message]
  (->> (map str message)
       (string/join " ")
       (util/info))
  (util/info "\n"))

(boot/deftask
  dynamodb-local
  [p project CONFIG edn "The configuration of the dynamo db process"]
  (boot/with-pass-thru _
    (let [info (core/create-dynamo-db-logger info)]
      (core/ensure-installed info)
      (let [dynamo-process (core/start-dynamo info project)]
        (core/handle-shutdown info dynamo-process)))))
