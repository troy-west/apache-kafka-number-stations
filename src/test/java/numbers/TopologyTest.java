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

public class TopologyTest extends TestCase {

    private static ConsumerRecordFactory<String, Object> recordFactory =
            new ConsumerRecordFactory<>("radio-logs", new StringSerializer(), new JsonSerializer());

    private static ConsumerRecord<byte[], byte[]> createRecord(Message message) {
        return recordFactory.create("radio-logs", message.name, message);
    }

    private static Message readOutput(TopologyTestDriver driver, String topic) {
        ProducerRecord<String, Message> output = driver.readOutput(topic, new StringDeserializer(), new JsonDeserializer<>(Message.class));
        if (output != null) {
            return output.value();
        } else {
            return null;
        }
    }

    Message[] testMessages = new Message[] {
            new Message(1557125670789L, "GER", "85", -92, -30, new String[] { "eins", "null", "sechs" }),
            new Message(1557125670790L, "UXX", "XRAY"),
            new Message(1557125670794L, "MOR", "425", 77, 25, new String[] { ".....", "----." }),
            new Message(1557125670795L, "UXX", "XRAY"),
            new Message(1557125670799L, "ENG", "NZ1", 166, -78, new String[] { "two" }),
            new Message(1557125670807L, "ENG", "159", -55, -18, new String[] { "three", "five" }),
            new Message(1557125670812L, "ENG", "426", 78, 26, new String[] { "six", "three" }),
            new Message(1557125670814L, "GER", "85", -92, -30, new String[] { "drei", "neun" }),
            new Message(1557125670819L, "MOR", "425", 77, 25, new String[] { ".----" }),
            new Message(1557125670824L, "ENG", "NZ1", 166, -78, new String[] { "two" }),
            new Message(1557125670827L, "ENG", "324", 27, 9, new String[] { "two", "nine" }),
            new Message(1557125670829L, "GER", "460", 95, 31, new String[] {  "fünf", "sieben"  }),
            new Message(1557125670831L, "GER", "355", 42, 14, new String[] {  "sieben"  }),
            new Message(1557125670832L, "ENG", "159", -55, -18, new String[] {  "three", "five"   }),
            new Message(1557125670837L, "ENG", "426", 78, 26, new String[] { "one"  }),
            new Message(1557125670839L, "GER", "85", -92, -30, new String[] { "fünf", "fünf" }),
            new Message(1557125670840L, "GER", "505", 117, 39, new String[] {  "eins", "null", "vier"  }),
            new Message(1557125670841L, "GER", "487", 108, 36, new String[] { "eins", "null", "neun"  }),
            new Message(1557125670842L, "MOR", "20", -125, -41, new String[] { "...--"  }),
            new Message(1557125670843L, "GER", "199", -35, -11, new String[] { "eins", "vier"})};

    public void sendMessage(TopologyTestDriver driver, Message message) {
        driver.pipeInput(createRecord(message));
    }

    public void sendMessages(TopologyTestDriver driver, Message[] messages) {
        for (Message m: messages) {
            sendMessage(driver, m);
        }
    }

    public void testFilterRecognized() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.filterRecognized(stream);
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        try {
            sendMessages(driver, testMessages);

            Message[] expectedMessages = new Message[] {
                    new Message(1557125670789L, "GER", "85", -92, -30, new String[] { "eins", "null", "sechs" }),
                    new Message(1557125670794L, "MOR", "425", 77, 25, new String[] { ".....", "----." }),
                    new Message(1557125670799L, "ENG", "NZ1", 166, -78, new String[] { "two" }),
                    new Message(1557125670807L, "ENG", "159", -55, -18, new String[] { "three", "five" }),
                    new Message(1557125670812L, "ENG", "426", 78, 26, new String[] { "six", "three" })};

            for(Message m: expectedMessages) {
                assertEquals(m, readOutput(driver, outputTopic));
            }
        } finally {
            driver.close();
        }
    }

    public void testTranslate(){
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.translate(Topology.filterRecognized(stream));
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        try {
            sendMessages(driver, testMessages);

            Message[] expectedMessages = new Message[] {
                new Message() { { time = 1557125670789L; type = "GER"; name = "85"; longitude = -92; latitude = -30; numbers = new int[] { 106 }; } },
                new Message() { { time = 1557125670794L; type = "MOR"; name = "425"; longitude = 77; latitude = 25; numbers = new int[] { 59 }; } },
                new Message() { { time = 1557125670799L; type = "ENG"; name = "NZ1"; longitude = 166; latitude = -78; numbers = new int[] { 2 }; } },
                new Message() { { time = 1557125670807L; type = "ENG"; name = "159"; longitude = -55; latitude = -18; numbers = new int[] { 35 }; } },
                new Message() { { time = 1557125670812L; type = "ENG"; name = "426"; longitude = 78; latitude = 26; numbers = new int[] { 63 }; } }
            };

            for(Message m: expectedMessages) {
                assertEquals(m, readOutput(driver, outputTopic));
            }
        } finally {
            driver.close();
        }
    }

    public void testCorrelate() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Topology.createStream(builder);

        String storeName = "PT10S-Store";
        Topology.correlate(Topology.translate(Topology.filterRecognized(stream)));

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        try {
            sendMessages(driver, testMessages);

            // Slice data

            KeyValueIterator iterator = driver.getWindowStore("PT10S-Store").fetch("85", (1557125670789L - 25000L), (1557125670789L + 100000L));

            try {
                Message expected = new Message() { { time = 1557125670789L; type = "GER"; name = "85"; longitude = -92; latitude = -30; numbers = new int[] { 106, 39, 55 }; } };
                Message message = (Message)((KeyValue)iterator.next()).value;
                assert(!iterator.hasNext());

                assertEquals(expected, message);
            } finally {
                iterator.close();
            }

            // Fetch from empty windows

            KeyValueIterator iterator2 = driver.getWindowStore("PT10S-Store").fetch("Unknown-Key", 0L, 10000L);

            try {
                assert(!iterator2.hasNext());
            } finally {
                iterator2.close();
            }
        } finally {
            driver.close();
        }
    }
}
