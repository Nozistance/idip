(ns idip.redis
  (:require [taoensso.carmine :as car :refer [wcar]]))

(def redis-conn
  {:pool {}
   :spec {:host "redis"
          :port 6379}})

(defn get [id]
  (wcar redis-conn (car/get id)))

(defn put [id ip]
  (wcar redis-conn
        (car/multi)
        (car/get id)
        (car/set id ip)
        (car/expire id 86400)
        (car/exec)))
