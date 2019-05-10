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
import org.apache.kafka.streams.state.WindowStore;

import java.util.function.Consumer;

public class TopologyTest extends TestCase {

    private static String inputTopic = "radio-logs";
    private static ConsumerRecordFactory<String, Message> recordFactory =
            new ConsumerRecordFactory<>(inputTopic, new StringSerializer(), new JsonSerializer());

    private static ConsumerRecord<byte[], byte[]> createRecord(Message message) {
        return recordFactory.create(inputTopic, message.name, message);
    }

    private static Message readOutput(TopologyTestDriver driver, String topic) {
        ProducerRecord<String, Message> output = driver.readOutput(topic, new StringDeserializer(), new JsonDeserializer());
        if (output != null) {
            return output.value();
        } else {
            return null;
        }
    }

    // private static Message getWindowValues(KeyValueIterator iterator) {
    //     List<int> values = new ArrayList<int>();

    //     return new Message() {
    //         contentDecoded = new int[] {};
    //     }
    //     ArrayNode windowValues = mapper.createArrayNode();
    //     iterator.forEachRemaining(new Consumer<KeyValue>() {
    //         @Override
    //         public void accept(KeyValue o) {
    //             values.add(o.value);
    //         }
    //     });
    //     return windowValues;
    // }

    Message[] testMessages = new Message[] {
        new Message() { { time = 1557125670789L; type = "GER"; name = "85"; longitude = -92; lat = -30; content = new String[] { "eins", "null", "sechs" }; } },
        new Message() { { time = 1557125670790L; type = "UXX"; name = "XRAY"; } },
        new Message() { { time = 1557125670794L; type = "MOR"; name = "425"; longitude = 77; lat = 25; content = new String[] { ".....", "----." }; } },
        new Message() { { time = 1557125670795L; type = "UXX"; name = "XRAY"; } },
        new Message() { { time = 1557125670799L; type = "ENG"; name = "NZ1"; longitude = 166; lat = -78; content = new String[] { "two" }; } },
        new Message() { { time = 1557125670807L; type = "ENG"; name = "159"; longitude = -55; lat = -18; content = new String[] { "three", "five" }; } },
        new Message() { { time = 1557125670812L; type = "ENG"; name = "426"; longitude = 78; lat = 26; content = new String[] { "six", "three" }; } },
        new Message() { { time = 1557125670814L; type = "GER"; name = "85"; longitude = -92; lat = -30; content = new String[] { "drei", "neun" }; } },
        new Message() { { time = 1557125670819L; type = "MOR"; name = "425"; longitude = 77; lat = 25; content = new String[] { ".----" }; } },
        new Message() { { time = 1557125670824L; type = "ENG"; name = "NZ1"; longitude = 166; lat = -78; content = new String[] { "two" }; } },
        new Message() { { time = 1557125670827L; type = "ENG"; name = "324"; longitude = 27; lat = 9; content = new String[] { "two", "nine" }; } },
        new Message() { { time = 1557125670829L; type = "GER"; name = "460"; longitude = 95; lat = 31; content = new String[] { "fünf", "sieben" }; } },
        new Message() { { time = 1557125670831L; type = "GER"; name = "355"; longitude = 42; lat = 14; content = new String[] { "sieben" }; } },
        new Message() { { time = 1557125670832L; type = "ENG"; name = "159"; longitude = -55; lat = -18; content = new String[] { "three", "five" }; } },
        new Message() { { time = 1557125670837L; type = "ENG"; name = "426"; longitude = 78; lat = 26; content = new String[] { "one" }; } },
        new Message() { { time = 1557125670839L; type = "GER"; name = "85"; longitude = -92; lat = -30; content = new String[] { "fünf", "fünf" }; } },
        new Message() { { time = 1557125670840L; type = "GER"; name = "505"; longitude = 117; lat = 39; content = new String[] { "eins", "null", "vier" }; } },
        new Message() { { time = 1557125670841L; type = "GER"; name = "487"; longitude = 108; lat = 36; content = new String[] { "eins", "null", "neun" }; } },
        new Message() { { time = 1557125670842L; type = "MOR"; name = "20"; longitude = -125; lat = -41; content = new String[] { "...--" }; } },
        new Message() { { time = 1557125670843L; type = "GER"; name = "199"; longitude = -35; lat = -11; content = new String[] { "eins", "vier" }; } }
    };

    public void sendMessage(TopologyTestDriver driver, Message message) {
        driver.pipeInput(createRecord(message));
    }

    public void sendMessages(TopologyTestDriver driver, Message[] messages) {
        for (Message m: messages) {
            sendMessage(driver, m);
        }
    }

    public void assertEqualsJson(Message a, Message b) throws javax.xml.bind.JAXBException {
        assertEquals(Json.serialize(a), Json.serialize(b));
    }

    public void testFilterRecognized() throws javax.xml.bind.JAXBException {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.filterRecognized(stream);
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        try {
            sendMessages(driver, testMessages);

            Message[] expectedMessages = new Message[] {
                new Message() { { time = 1557125670789L; type = "GER"; name = "85"; longitude = -92; lat = -30; content = new String[] { "eins", "null", "sechs" }; } },
                new Message() { { time = 1557125670794L; type = "MOR"; name = "425"; longitude = 77; lat = 25; content = new String[] { ".....", "----." }; } },
                new Message() { { time = 1557125670799L; type = "ENG"; name = "NZ1"; longitude = 166; lat = -78; content = new String[] { "two" }; } },
                new Message() { { time = 1557125670807L; type = "ENG"; name = "159"; longitude = -55; lat = -18; content = new String[] { "three", "five" }; } },
                new Message() { { time = 1557125670812L; type = "ENG"; name = "426"; longitude = 78; lat = 26; content = new String[] { "six", "three" }; } }
            };

            for(Message m: expectedMessages) {
                System.out.println(Json.serialize(m));
                assertEquals(m, readOutput(driver, outputTopic));
            }
        } finally {
            driver.close();
        }
    }

    public void testTranslate() throws javax.xml.bind.JAXBException {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.translate(Topology.filterRecognized(stream));
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        try {
            sendMessages(driver, testMessages);

            Message[] expectedMessages = new Message[] {
                new Message() { { time = 1557125670789L; type = "GER"; name = "85"; longitude = -92; lat = -30; numbers = new int[] { 106 }; } },
                new Message() { { time = 1557125670794L; type = "MOR"; name = "425"; longitude = 77; lat = 25; numbers = new int[] { 59 }; } },
                new Message() { { time = 1557125670799L; type = "ENG"; name = "NZ1"; longitude = 166; lat = -78; numbers = new int[] { 2 }; } },
                new Message() { { time = 1557125670807L; type = "ENG"; name = "159"; longitude = -55; lat = -18; numbers = new int[] { 35 }; } },
                new Message() { { time = 1557125670812L; type = "ENG"; name = "426"; longitude = 78; lat = 26; numbers = new int[] { 63 }; } }
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

            KeyValueIterator iterator = driver.getWindowStore("PT10S-Store").fetch("85", (1557125670789L - 25000L), (1557125670789L + 100000L));

            try {
                Message expected = new Message() { { time = 1557125670789L; type = "GER"; name = "85"; longitude = -92; lat = -30; numbers = new int[] { 106, 39, 55 }; } };
                Message message = (Message)((KeyValue)iterator.next()).value;
                assert(!iterator.hasNext());

                assertEquals(expected, message);
            } finally {
                iterator.close();
            }


        } finally {
            driver.close();
        }
    }
}
