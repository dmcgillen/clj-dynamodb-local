(ns dynamodb-local.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [dynamodb-local.core :as sut]
            [environ.core :refer [env]]
            [medley.core :as medley])
  (:import [java.io File]))

(def log-nothing (constantly nil))

(def ^:private dynamo-directory
  "The directory where DynamoDB Local is installed."
  (str (System/getProperty "user.home") File/separator ".clj-dynamodb-local"))

(def ^:private dynamo-lib-paths
  "String containing the paths to the dynamo libs and jar to be used
  when building the java command to start DynamoDB Local."
  (str "-Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar"))

(deftest build-dynamo-command
  (testing "Command is built correctly from options"
    (doseq [[dynamodb-local-config command] [[nil
                                              (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)]
                                             [{}
                                              (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)]
                                             [{:port "9999"}
                                              (str "java  " dynamo-lib-paths " -port 9999 -dbPath " dynamo-directory)]
                                             [{:in-memory? true}
                                              (str "java  " dynamo-lib-paths " -port 8000 -inMemory")]
                                             [{:in-memory? false}
                                              (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)]
                                             [{:db-path "some/path/that/will/be/ignored" :in-memory? true}
                                              (str "java  " dynamo-lib-paths " -port 8000 -inMemory")]
                                             [{:db-path "some/path"}
                                              (str "java  " dynamo-lib-paths " -port 8000 -dbPath some/path")]
                                             [{:shared-db? true}
                                              (str "java  " dynamo-lib-paths " -port 8000 -sharedDb -dbPath " dynamo-directory)]
                                             [{:shared-db? false}
                                              (str "java  " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)]
                                             [{:jvm-opts ["opt1" "opt2"]}
                                              (str "java opt1 opt2 " dynamo-lib-paths " -port 8000 -dbPath " dynamo-directory)]]]
      (let [project  (-> {:some-key "some-val"}
                         (medley/assoc-some :dynamodb-local dynamodb-local-config))]
        (is (= (#'sut/build-dynamo-command log-nothing project)
               command)))))
  (testing "Port can be specified as an environment variable"
    (with-redefs [env (constantly "7777")]
      (doseq [[dynamodb-local-config port] [[{} "7777"]
                                            [{:port "9999"} "9999"]]]
        (let [project {:some-key "some-val"
                       :dynamodb-local dynamodb-local-config}]
          (is (= (#'sut/build-dynamo-command log-nothing project)
                 (str "java  " dynamo-lib-paths " -port " port " -dbPath " dynamo-directory))))))))

(deftest ensure-installed
  (testing "DynamoDB local is downloaded and unpacked if it hasn't been already"
    (with-redefs [sut/exists? (constantly false)
                  sut/download-dynamo (constantly :download-result)
                  sut/unpack-dynamo (constantly :unpack-result)]
      (is (= (sut/ensure-installed log-nothing)
             :unpack-result))))
  (testing "DynamoDB local not downloaded if it already has been"
    (with-redefs [sut/exists? (constantly true)]
      (is (nil? (sut/ensure-installed log-nothing))))))
