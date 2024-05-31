(ns idip.core
  (:require [compojure.core :refer [ANY GET POST defroutes]]
            [idip.redis :as redis]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response status]]))

(defn valid-telegram-id? [id]
  (re-matches #"\d+" id))

(defroutes app-routes
           (GET "/:id" [id]
             (if (valid-telegram-id? id)
               (if-let [value (redis/pop-value id)]
                 (response {:ip value})
                 (-> (response {:error "Telegram ID not found"}) (status 404)))
               (-> (response {:error "Invalid Telegram ID"}) (status 400))))
           (POST "/:id" [id :as {body :body}]
             (if (valid-telegram-id? id)
               (if-let [ip (get body "ip")]
                 (let [existing-value (redis/pop-value id)]
                   (redis/put-value id ip)
                   (if existing-value
                     (-> (response {:answer "Value updated"}) (status 200))
                     (-> (response {:answer "New value created"}) (status 201))))
                 (-> (response {:error "IP address is required"}) (status 400)))
               (-> (response {:error "Invalid Telegram ID"}) (status 400))))
           (ANY "/" []
             (-> (response {:error "Root path. Please specify a Telegram ID."}) (status 400))))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-response))

(defn -main []
  (run-jetty app {:port 3000 :join? false}))
