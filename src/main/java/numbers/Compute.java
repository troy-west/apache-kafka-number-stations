package numbers;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        // TODO: Implement me. Create a new stream with a MessageTimeExtractor for timestamps
        return null;
    }

    public static KStream<String, Message> filterKnown(KStream<String, Message> stream) {
        // TODO: Implement me. Filter only messages where Translator.knows(message)
        return null;
    }


    public static KStream<String, Message>[] branchSpecial(KStream<String, Message> stream) {
        // TODO: Implement me. Split the stream in two. All messages above -75 latitude, and those below (Scott Base)
        return null;
    }

    public static void logScottBase(KStream<String, Message> stream) {
        // TODO: Implement me. Log/info each Scott Base Message
    }

    public static KStream<String, Message> translate(KStream<String, Message> stream) {
        // TODO: Implement me. Translate content from text to numeric
        return null;
    }

    public static KTable<Windowed<String>, Message> correlate(KStream<String, Message> stream) {
        // TODO: Implement me. Group messages by key, then window by 10s tumbling time windows, then aggregate each window into a single message with a content tuple of three numbers
        // TODO: finally, make sure the k-table is materialized to "PT10S-Store"
        return null;
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
