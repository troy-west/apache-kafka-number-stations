package numbers;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Properties;

public class Topology {
    public static final Properties config = new Properties() {
        {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-default");
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "numbers.MessageSerde");
        }
    };

    public static KStream<String, Message> createStream(StreamsBuilder builder) {
        return builder.stream("radio-logs", Consumed.with(new TimeExtractor()));
    }

    public static void topology(StreamsBuilder builder) {
        correlate(translate(filterRecognized(createStream(builder))));
    }

    public static KStream<String, Message> filterRecognized(KStream<String, Message> stream) {
        return stream.filter(new Predicate<String, Message>() {
            @Override
            public boolean test(String key, Message value) {
                if (value.type != null) {
                    return Translator.numberIndex.containsKey(value.type);
                } else {
                    return false;
                }
            }
        });
    }

    public static KStream<String, Message> translate(KStream<String, Message> stream) {
        return stream.mapValues(new ValueMapper<Message, Message>() {
            @Override
            public Message apply(Message message) {
                if (message.type != null && message.content != null) {
                    message.numbers = new int[] { Translator.translateNumbers(message.type, message.content) };
                }

                message.content = null;

                return message;
            }
        });
    }

    public static KTable<Windowed<String>, Message> correlate(KStream<String, Message> stream) {
        return stream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                .aggregate(
                        new Initializer<Message>() {
                            @Override
                            public Message apply() {
                                return null;
                            }
                        }, new Aggregator<String, Message, Message>() {
                            @Override
                            public Message apply(String key, Message value, Message aggregation) {
                                if (aggregation == null) {
                                    aggregation = new Message() {
                                            {
                                                time = value.time;
                                                type = value.type;
                                                name = value.name;
                                                longitude = value.longitude;
                                                latitude = value.latitude;
                                                numbers = new int[0];
                                            }
                                        };
                                }

                                int[] numbers = new int[aggregation.numbers.length + value.numbers.length];

                                for(int i = 0; i < aggregation.numbers.length; i++) {
                                    numbers[i] = aggregation.numbers[i];
                                }

                                for(int i = 0; i < value.numbers.length; i++) {
                                    numbers[i + aggregation.numbers.length] = value.numbers[i];
                                }

                                aggregation.numbers = numbers;

                                return aggregation;
                            }
                        }, Materialized.as("PT10S-Store"));
    }
}
