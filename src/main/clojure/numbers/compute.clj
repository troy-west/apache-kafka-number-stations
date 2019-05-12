(ns numbers.compute
  (:require [numbers.translate :as tx]
            [numbers.radio :as radio])
  (:import (java.util Properties)
           (org.apache.kafka.streams.processor TimestampExtractor)
           (org.apache.kafka.streams.kstream Consumed KStream Predicate ValueMapper Aggregator TimeWindows Initializer Materialized KTable ValueJoiner)
           (org.apache.kafka.streams StreamsBuilder StreamsConfig Topology KafkaStreams)
           (org.apache.kafka.streams.state QueryableStoreTypes ReadOnlyWindowStore)))

(def config
  (let [props (Properties.)]
    (.putAll props {"application.id"      "compute-radio-logs"
                    "bootstrap.servers"   "localhost:9092"
                    "default.key.serde"   "org.apache.kafka.common.serialization.Serdes$StringSerde"
                    "default.value.serde" "numbers.serdes.JsonSerde"})
    props))

(def ^TimestampExtractor extractor
  (reify TimestampExtractor
    (extract [_ record _]
      (:time (.value record)))))

(defn stream
  [^StreamsBuilder builder]
  (.stream builder "radio-logs" (Consumed/with ^TimestampExtractor extractor)))

(defn filter-known
  "Filter the input stream, keeping only tx/known? elements"
  [^KStream messages]
  (.filter messages (reify Predicate
                      (test [_ _ message]
                        (tx/known? message)))))

(defn translate
  "Translate the input stream, converting from text to numeric content"
  [^KStream messages]
  (.mapValues messages (reify ValueMapper
                         (apply [_ message]
                           (tx/translate message)))))

(defn correlate
  "Correlate the input stream, grouping by station-id then windowing every 10s"
  [^KStream messages store-name]
  (-> (.groupByKey messages)
      (.windowedBy (TimeWindows/of 10000))
      (.aggregate (reify Initializer
                    (apply [_] nil))
                  (reify Aggregator
                    (apply [_ _ v agg]
                      (if (not agg)
                        v
                        (update agg :content conj (first (:content v))))))
                  (Materialized/as ^String store-name))))

(defn branch-scott-base
  "Branch between messages above and below -75 latitude"
  [^KStream messages]
  (.branch messages (into-array Predicate
                                [(reify Predicate
                                   (test [_ _ message]
                                     (>= (:lat message) -75)))
                                 (reify Predicate
                                   (test [_ _ message]
                                     (< (:lat message) -75)))])))

(defn topology
  [builder]
  (-> (stream builder)
      (filter-known)
      (translate)
      (correlate "PT10S-Store")))

(defn start!
  [^Topology topology ^StreamsConfig config]
  (let [^KafkaStreams streams (KafkaStreams. topology config)]
    (.start streams)
    streams))

(defn slice
  ([streams]
   (slice streams "PT10S-Store"))
  ([streams store-name]
   (slice streams store-name (radio/stations)))
  ([streams store-name stations]
   (slice streams store-name stations 1557125660763 1557135288803))
  ([streams store-name stations start end]
   (let [store  (.store streams store-name (QueryableStoreTypes/windowStore))
         rows   (for [station stations]
                  (with-open [iter (.fetch ^ReadOnlyWindowStore store station ^long start ^long end)]
                    (doall (map #(.value %1) (iterator-seq iter)))))
         width  (apply max (map count rows))
         height (count rows)]
     [(mapcat (fn [row]
                (concat (map (fn [value] (map #(Integer/parseInt %) (.getContent value))) row)
                        (repeat (- width (count row)) [0 0 0]))) rows)
      width
      height])))
