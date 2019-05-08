package numbers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Properties;

class Topology {

    static final Properties config = new Properties() {
        {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-default");
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "numbers.JsonSerde");
        }
    };

    static KStream<String, JsonNode> createStream(StreamsBuilder builder) {
        return builder.stream("radio-logs", Consumed.with(new TimeExtractor()));
    }

    static KStream<String, JsonNode> filterRecognized(KStream<String, JsonNode> stream) {
        return stream.filter(new Predicate<String, JsonNode>() {
            @Override
            public boolean test(String key, JsonNode value) {
                if (value.hasNonNull("type")) {
                    return Translator.numberIndex.containsKey(value.get("type").textValue());
                } else {
                    return false;
                }
            }
        });
    }

    static KStream<String, JsonNode> translate(KStream<String, JsonNode> stream) {
        return stream.mapValues(new ValueMapper<JsonNode, JsonNode>() {
            @Override
            public JsonNode apply(JsonNode value) {
                ObjectNode obj = (ObjectNode) value;
                if (obj.hasNonNull("type") && obj.hasNonNull("numbers")) {
                    obj.put("number", Translator.translateNumbers(obj.get("type").textValue(), (ArrayNode) obj.get("numbers")));
                }
                obj.remove("numbers");
                return obj;
            }
        });
    }

    static KTable correlate(KStream<String, JsonNode> stream) {
        return stream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                .aggregate(new Initializer<Object>() {
                    @Override
                    public Object apply() {
                        return null;
                    }
                }, new Aggregator<String, JsonNode, Object>() {
                    @Override
                    public Object apply(String s, JsonNode jsonNode, Object o) {
                        return null;
                    }
                });
    }

}
