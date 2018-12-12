(ns leiningen.dynamodb-local
  (:require [dynamodb-local.core :as core]
            [leiningen.core.main :as main]))

(defn dynamodb-local
  "Run DynamoDB Local for the lifetime of the given task."
  [project & args]
  (let [info (core/create-dynamo-db-logger main/info)]
    (core/ensure-installed info)
    (let [dynamo-process (core/start-dynamo info project)]
      (core/handle-shutdown info dynamo-process)
      (if (seq args)
        (main/apply-task (first args) project (rest args))
        (while true (Thread/sleep 5000))))))
