package numbers;

import junit.framework.TestCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

import java.util.List;

public class ComputeTest extends TestCase {

    private static ConsumerRecordFactory<String, Object> recordFactory =
            new ConsumerRecordFactory<>("radio-logs", new StringSerializer(), new JsonSerializer<>());

    private static ConsumerRecord<byte[], byte[]> createRecord(Message message) {
        return recordFactory.create("radio-logs", message.getName(), message);
    }

    private static Message readOutput(TopologyTestDriver driver) {
        ProducerRecord<String, Message> output = driver.readOutput("output", new StringDeserializer(), new JsonDeserializer<>(Message.class));
        if (output != null) {
            return output.value();
        } else {
            return null;
        }
    }

    List<Message> testMessages = List.of(
            new Message(1557125670789L, "GER", "085", -92, -30, List.of("eins", "null", "sechs")),
            new Message(1557125670790L, "UXX", "XRAY"),
            new Message(1557125670794L, "MOR", "425", 77, 25, List.of(".....", "----.")),
            new Message(1557125670795L, "UXX", "XRAY"),
            new Message(1557125670799L, "ENG", "NZ1", 166, -78, List.of("two")),
            new Message(1557125670807L, "ENG", "159", -55, -18, List.of("three", "five")),
            new Message(1557125670812L, "ENG", "426", 78, 26, List.of("six", "three")),
            new Message(1557125670814L, "GER", "085", -92, -30, List.of("drei", "neun")),
            new Message(1557125670819L, "MOR", "425", 77, 25, List.of(".----")),
            new Message(1557125670824L, "ENG", "NZ1", 166, -78, List.of("two")),
            new Message(1557125670827L, "ENG", "324", 27, 9, List.of("two", "nine")),
            new Message(1557125670829L, "GER", "460", 95, 31, List.of("fünf", "sieben")),
            new Message(1557125670831L, "GER", "355", 42, 14, List.of("sieben")),
            new Message(1557125670832L, "ENG", "159", -55, -18, List.of("three", "five")),
            new Message(1557125670837L, "ENG", "426", 78, 26, List.of("one")),
            new Message(1557125670839L, "GER", "085", -92, -30, List.of("fünf", "fünf")),
            new Message(1557125670840L, "GER", "505", 117, 39, List.of("eins", "null", "vier")),
            new Message(1557125670841L, "GER", "487", 108, 36, List.of("eins", "null", "neun")),
            new Message(1557125670842L, "MOR", "020", -125, -41, List.of("...--")),
            new Message(1557125670843L, "GER", "199", -35, -11, List.of("eins", "vier"))
    );

    public void sendMessage(TopologyTestDriver driver, Message message) {
        driver.pipeInput(createRecord(message));
    }

    public void sendMessages(TopologyTestDriver driver, List<Message> messages) {
        for (Message m : messages) {
            sendMessage(driver, m);
        }
    }

    public void testStreamTimestampExtraction() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Compute.createStream(builder);
        stream.to("output");

        try (TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Compute.config)) {

            sendMessage(driver, new Message(1557125670789L, "GER", "085", -92, -30, List.of("eins", "null", "sechs")));

            ProducerRecord<String, Message> output = driver.readOutput("output", new StringDeserializer(), new JsonDeserializer<>(Message.class));

            assertEquals((Long) 1557125670789L, output.timestamp());
        }
    }

    public void testFilterKnown() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Compute.createStream(builder);
        KStream<String, Message> filtered = Compute.filterKnown(stream);
        filtered.to("output");

        try (TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Compute.config)) {

            sendMessages(driver, testMessages);

            List<Message> expected = List.of(
                    new Message(1557125670789L, "GER", "085", -92, -30, List.of("eins", "null", "sechs")),
                    new Message(1557125670794L, "MOR", "425", 77, 25, List.of(".....", "----.")),
                    new Message(1557125670799L, "ENG", "NZ1", 166, -78, List.of("two")),
                    new Message(1557125670807L, "ENG", "159", -55, -18, List.of("three", "five")),
                    new Message(1557125670812L, "ENG", "426", 78, 26, List.of("six", "three"))
            );

            for (Message m : expected) {
                assertEquals(m, readOutput(driver));
            }
        }
    }

    public void testBranchRestOfWorld() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Compute.createStream(builder);
        KStream<String, Message> filtered = Compute.filterKnown(stream);
        KStream[] branched = Compute.branchSpecial(filtered);

        branched[0].to("output");

        try (TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Compute.config)) {

            sendMessages(driver, testMessages);

            List<Message> expected = List.of(
                    new Message(1557125670789L, "GER", "085", -92, -30, List.of("eins", "null", "sechs")),
                    new Message(1557125670794L, "MOR", "425", 77, 25, List.of(".....", "----.")),
                    new Message(1557125670807L, "ENG", "159", -55, -18, List.of("three", "five")),
                    new Message(1557125670812L, "ENG", "426", 78, 26, List.of("six", "three"))
            );

            for (Message m : expected) {
                assertEquals(m, readOutput(driver));
            }
        }
    }

    public void testBranchScottBase() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Compute.createStream(builder);
        KStream<String, Message> filtered = Compute.filterKnown(stream);
        KStream[] branched = Compute.branchSpecial(filtered);

        branched[1].to("output");

        try (TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Compute.config)) {

            sendMessages(driver, testMessages);

            List<Message> expected = List.of(
                    new Message(1557125670799L, "ENG", "NZ1", 166, -78, List.of("two"))
            );

            for (Message m : expected) {
                assertEquals(m, readOutput(driver));
            }
        }
    }

    public void testTranslate() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Compute.createStream(builder);

        stream = Compute.translate(Compute.filterKnown(stream));
        stream.to("output");

        try (TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Compute.config)) {
            sendMessages(driver, testMessages);

            List<Message> expected = List.of(
                    new Message(1557125670789L, "GER", "085", -92, -30, List.of("106")),
                    new Message(1557125670794L, "MOR", "425", 77, 25, List.of("59")),
                    new Message(1557125670799L, "ENG", "NZ1", 166, -78, List.of("2")),
                    new Message(1557125670807L, "ENG", "159", -55, -18, List.of("35")),
                    new Message(1557125670812L, "ENG", "426", 78, 26, List.of("63"))
            );

            for (Message m : expected) {
                assertEquals(m, readOutput(driver));
            }
        }
    }

    public void testCorrelate() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Compute.createStream(builder);

        Compute.correlate(Compute.translate(Compute.filterKnown(stream)));

        try (TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Compute.config)) {
            sendMessages(driver, testMessages);

            try (KeyValueIterator iterator = driver.getWindowStore("PT10S-Store").fetch("085", (1557125670789L - 25000L), (1557125670789L + 100000L))) {

                Message expected = new Message(1557125670789L, "GER", "085", -92, -30, List.of("106", "39", "55"));
                Message actual = (Message) ((KeyValue) iterator.next()).value;

                assert (!iterator.hasNext());
                assertEquals(expected, actual);
            }

            // Fetch from empty windows
            try (KeyValueIterator iterator2 = driver.getWindowStore("PT10S-Store").fetch("Unknown-Key", 0L, 10000L)) {
                assert (!iterator2.hasNext());
            }
        }
    }
}
