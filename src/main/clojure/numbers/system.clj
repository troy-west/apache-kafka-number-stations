(ns numbers.system
  (:require [clojure.tools.logging :as log]
            [numbers.compute :as compute]
            [numbers.http :as http])
  (:import numbers.Topology
           (org.apache.kafka.streams StreamsBuilder StreamsConfig))
  (:gen-class))

(defn start!
  [port]
  (let [builder (StreamsBuilder.)]
    ;;    (numbers.compute/topology builder)

    (numbers.Topology/topology builder)
    (http/start! port (compute/start! (.build builder) (StreamsConfig. numbers.Topology/config)))))

(defn -main
  [& args]
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread ex]
       (log/error ex "uncaught exception on" (.getName thread)))))
  (start! (or (some-> (first args) Integer/parseInt) 8080)))
