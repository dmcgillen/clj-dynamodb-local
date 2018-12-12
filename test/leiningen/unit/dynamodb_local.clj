(ns leiningen.unit.dynamodb-local
  (:require [dynamodb-local.core :refer :all]
            [environ.core :refer [env]]
            [midje
             [sweet :refer :all]
             [util :refer [testable-privates]]])
  (:import [java.io File]))

(testable-privates dynamodb-local.core build-dynamo-command)

(def log-nothing (constantly nil))

(def ^:private dynamo-directory
  "The directory where DynamoDB Local is installed."
  (str (System/getProperty "user.home") File/separator ".clj-dynamodb-local"))

(def ^:private dynamo-lib-paths
  "String containing the paths to the dynamo libs and jar to be used
  when building the java command to start DynamoDB Local."
  (str "-Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar"))

(fact-group
 :unit

 (tabular
  (fact "Build dynamo command correctly builds command based on options set in the project map"
        (let [project (merge {:some-key "some-val"}
                             (when ?dynamodb-local-config
                               {:dynamodb-local ?dynamodb-local-config}))]
          (build-dynamo-command log-nothing project)) => ?command)
  ?dynamodb-local-config                                       ?command
  nil                                                          (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)
  {}                                                           (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)
  {:port "9999"}                                               (str "java  " dynamo-lib-paths " -port 9999 -dbPath " dynamo-directory)
  {:in-memory? true}                                           (str "java  " dynamo-lib-paths " -port 8000 -inMemory")
  {:in-memory? false}                                          (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)
  {:db-path "some/path/that/will/be/ignored" :in-memory? true} (str "java  " dynamo-lib-paths " -port 8000 -inMemory")
  {:db-path "some/path"}                                       (str "java  " dynamo-lib-paths " -port 8000 -dbPath some/path")
  {:shared-db? true}                                           (str "java  " dynamo-lib-paths " -port 8000 -sharedDb -dbPath " dynamo-directory)
  {:shared-db? false}                                          (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)
  {:jvm-opts ["opt1" "opt2"]})                                 (str "java opt1 opt2 " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)

  (tabular
   (fact "Build dynamo command allows port to be specified as an environment variable"
         (let [project {:some-key "some-val"
                        :dynamodb-local ?dynamodb-local-config}]
           (build-dynamo-command log-nothing project) => (str "java  " dynamo-lib-paths " -port " ?port " -dbPath " dynamo-directory)
           (provided
            (env :dynamodb-port "8000") => "7777")))
   ?dynamodb-local-config ?port
   {}                     "7777"
   {:port "9999"}         "9999")

  (fact "Ensure installed does not download DynamoDB Local if it already has been"
        (ensure-installed log-nothing) => nil
        (provided
          (#'dynamodb-local.core/exists? anything) => true))

  (fact "Ensure installed downloads and unpacks DynamoDB Local if it hasn't been already"
        (ensure-installed log-nothing) => ...unpack-result...
        (provided
          (#'dynamodb-local.core/exists? anything) => false
          (#'dynamodb-local.core/download-dynamo log-nothing anything) => ...download-result...
          (#'dynamodb-local.core/unpack-dynamo log-nothing) => ...unpack-result...)))
