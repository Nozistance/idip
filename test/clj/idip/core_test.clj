(ns idip.core-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [idip.core :refer [app]]
            [idip.redis :as redis]
            [ring.mock.request :as mock]))

(use-fixtures
  :each (fn [f]
          (with-redefs
            [redis/get-value (fn [key]
                               (when (not= key "0")
                                 "mocked-value"))
             redis/set-value (fn [_ _] nil)] (f))))

(deftest test-get-value
  (testing "GET request with existing key"
    (let [response (app (mock/request :get "/123"))]
      (is (= (:status response) 200))
      (is (= (:body response) "{\"ip\":\"mocked-value\"}"))))

  (testing "GET request with unique key"
    (let [response (app (mock/request :get "/1111"))]
      (is (= (:status response) 200))
      (is (= (:body response) "{\"ip\":\"mocked-value\"}"))))

  (testing "GET request with invalid Telegram ID"
    (let [response (app (mock/request :get "/abc"))]
      (is (= (:status response) 400))
      (is (= (:body response) "{\"error\":\"Invalid Telegram ID\"}")))))

(deftest test-post-value
  (testing "POST request to create a new value"
    (let [response (app (-> (mock/request :post "/0")
                            (mock/json-body {:ip "value"})
                            (mock/header "Content-Type" "application/json")))]
      (is (= (:status response) 201))
      (is (= (:body response) "{\"answer\":\"New value created\"}"))))

  (testing "POST request to update an existing value"
    (let [response (app (-> (mock/request :post "/123")
                            (mock/json-body {:ip "new value"})
                            (mock/header "Content-Type" "application/json")))]
      (is (= (:status response) 200))
      (is (= (:body response) "{\"answer\":\"Value updated\"}"))))

  (testing "POST request with invalid Telegram ID"
    (let [response (app (-> (mock/request :post "/abc")
                            (mock/json-body "{\"new\":\"value\"}")
                            (mock/header "Content-Type" "application/json")))]
      (is (= (:status response) 400))
      (is (= (:body response) "{\"error\":\"Invalid Telegram ID\"}")))))

(deftest test-root-path
  (testing "ANY request to root path"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 400))
      (is (= (:body response) "{\"error\":\"Root path. Please specify a Telegram ID.\"}")))))