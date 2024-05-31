(ns idip.redis
  (:require [taoensso.carmine :as car :refer [wcar]]))

(def redis-conn {:spec {:host "localhost"}})

(defn pop-value [key]
  (wcar redis-conn
        (car/multi)
        (car/get key)
        (car/del key)
        (car/exec)))

(defn put-value [key value]
  (wcar redis-conn
        (car/multi)
        (car/set key value)
        (car/expire key 86400)
        (car/exec)))
