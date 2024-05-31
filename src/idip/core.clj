(ns idip.core
  (:require [compojure.core :refer [ANY GET POST defroutes]]
            [idip.redis :as redis]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :refer [response status]]))

(defn valid-id? [id]
  (re-matches #"\d+" id))

(defroutes app-routes
           (GET "/:id" [id]
             (if (valid-id? id)
               (if-let [ip (redis/get id)]
                 (-> (response {:ip ip}) (status 200))
                 (-> (response {:error "Telegram ID not found"}) (status 404)))
               (-> (response {:error "Invalid Telegram ID"}) (status 400))))
           (POST "/:id" [id :as {body :body}]
             (if (valid-id? id)
               (let [ip (get body "ip")
                     old-ip (redis/put id ip)]
                 (if ip
                   (if old-ip
                     (-> (response {:answer "ID:IP has been updated" :old-ip old-ip}) (status 200))
                     (-> (response {:answer "New ID:IP has been added" :old-ip nil}) (status 201)))
                   (if old-ip
                     (-> (response {:answer "ID:IP has been removed" :old-ip old-ip}) (status 200))
                     (-> (response {:error "No ID:IP to remove" :old-ip nil}) (status 404)))))
               (-> (response {:error "Invalid Telegram ID"}) (status 400))))
           (ANY "/" []
             (-> (response {:error "Root path. Please specify a Telegram ID."}) (status 400))))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-response))

(defn -main []
  (run-jetty app {:port 3000 :join? false}))