(ns numbers.radio
  (:require [numbers.image :as image]
            [numbers.kafka :as kafka])
  (:import (org.apache.kafka.clients.producer ProducerRecord KafkaProducer)
           (numbers Message))
  (:gen-class))

(defn listen
  "Nearly three hours of Numbers Station broadcast from 1557125670763 to 1557135278803"
  []
  (image/obsfuscate image/source))

(defn sample
  []
  (take 20 (listen)))

(defn stations
  []
  (map #(format "%03d" %1) (range (image/height image/source))))

(defn produce
  "Send the radio burst to the radio-logs topic on Kafka"
  []
  ;; implement me!
  (with-open [^KafkaProducer producer (kafka/producer)]
    (doseq [message (listen)]
      (.send producer (ProducerRecord. "radio-logs" (:name message) message)))))

(defn -main
  [& argv]
  (produce))

(defn ->Message [{:keys [time type name long lat content]}]
  (Message. time type name long lat (into-array String content)))

(defn java-listen []
  (map ->Message (listen)))

(defn java-sample []
  (map ->Message (sample)))