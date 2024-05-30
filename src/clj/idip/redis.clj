(ns idip.redis
  (:require [taoensso.carmine :as car :refer [wcar]]))

(def redis-conn
  {:pool {}
   :spec {:host "redis"
          :port 6379}})

(defn get-value [key]
  (wcar redis-conn (car/get key)))

(defn set-value [key value]
  (wcar redis-conn
        (car/multi)
        (car/set key value)
        (car/expire key 86400)
        (car/exec)))
