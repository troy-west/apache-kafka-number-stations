package numbers;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Properties;

public class Topology {
    public static final Properties config = new Properties() {
        {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "number-stations-compute");
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "numbers.MessageSerde");
        }
    };

    public static KStream<String, Message> createStream(StreamsBuilder builder) {
        return builder.stream("radio-logs", Consumed.with(new TimeExtractor()));
    }

    public static void topology(StreamsBuilder builder) {
        correlate(translate(filterKnown(createStream(builder))));
    }

    public static KStream<String, Message> filterKnown(KStream<String, Message> stream) {
        return stream.filter((key, message) -> {
            if (message.getType() != null) {
                return Translator.numberIndex.containsKey(message.getType());
            } else {
                return false;
            }
        });
    }

    public static KStream<String, Message> translate(KStream<String, Message> stream) {
        return stream.mapValues(message -> {
            if (message.getType() != null && message.getContent() != null) {
                message.setContent(new String[]{"" + Translator.translateNumbers(message.getType(), message.getContent())});
            }
            return message;
        });
    }

    public static KTable<Windowed<String>, Message> correlate(KStream<String, Message> stream) {
        return stream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                .aggregate(
                        () -> null,
                        (key, message, aggregation) -> {
                            if (aggregation == null) {
                                return message;
                            }

                            String[] aggCurr = new String[aggregation.getContent().length + 1];
                            System.arraycopy(aggregation.getContent(), 0, aggCurr, 0, aggregation.getContent().length);
                            aggCurr[aggregation.getContent().length] = message.getContent()[0];

                            Message copy = aggregation.copy();
                            copy.setContent(aggCurr);
                            return copy;

                        }, Materialized.as("PT10S-Store"));
    }
}
