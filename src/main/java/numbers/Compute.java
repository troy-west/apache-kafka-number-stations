package numbers;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class Compute {

    private static final Logger logger = LoggerFactory.getLogger(Compute.class);

    public static final Properties config = new Properties() {
        {
            put(StreamsConfig.APPLICATION_ID_CONFIG, "compute-radio-logs");
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde");
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "numbers.MessageSerde");
        }
    };

    public static KStream<String, Message> createStream(StreamsBuilder builder) {
        return builder.stream("radio-logs", Consumed.with(new MessageTimeExtractor()));
    }

    public static KStream<String, Message> filterKnown(KStream<String, Message> stream) {
        return stream.filter((key, message) -> Translator.knows(message));
    }


    public static KStream<String, Message>[] branchSpecial(KStream<String, Message> stream) {
        return stream.branch(
                (key, message) -> message.getLatitude() >= -75,
                (key, message) -> message.getLatitude() < -75);
    }

    public static void logScottBase(KStream<String, Message> stream) {
        stream.foreach((key, message) -> logger.info(String.format("SB %s %s", key, message)));
    }

    public static KStream<String, Message> translate(KStream<String, Message> stream) {
        return stream.mapValues(message -> {
            String translated = Translator.translate(message.getType(), message.getContent());
            return message.copy().resetContent(List.of(translated));
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

                            return aggregation.copy().addContent(message.getContent());

                        },
                        Materialized.as("PT10S-Store"));
    }

    public static void topology(StreamsBuilder builder) {
        KStream<String, Message> stream = createStream(builder);
        KStream<String, Message> filtered = filterKnown(stream);
        KStream<String, Message>[] branched = branchSpecial(filtered);
        KStream<String, Message> translated = translate(branched[0]);
        logScottBase(branched[1]);
        correlate(translated);
    }

}
