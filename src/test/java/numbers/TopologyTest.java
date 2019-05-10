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

    public void testFilterRecognized() throws javax.xml.bind.JAXBException {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Message> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.filterRecognized(stream);
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        try {
            Message expected1 = new Message();
            expected1.time = 10;
            expected1.type = "ENG";
            expected1.name = "E-test-english";
            expected1.content = new String[] {"two", "five", "one"};

            Message notExpected = new Message();
            notExpected.time = 20;
            notExpected.name = "X-unknown";
            notExpected.number = null;

            Message expected2 = new Message();
            expected2.time = 30;
            expected2.type = "GER";
            expected2.name = "G-test-german";
            expected2.number = 100;

            driver.pipeInput(createRecord(expected1));
            driver.pipeInput(createRecord(notExpected));
            driver.pipeInput(createRecord(expected2));

            assertEquals(Json.serialize(expected1), Json.serialize(readOutput(driver, outputTopic)));
            assertEquals(Json.serialize(expected2), Json.serialize(readOutput(driver, outputTopic)));
            assertNull(readOutput(driver, outputTopic));
        } finally {
            driver.close();
        }
    }

    // public void testTranslate() {
    //     StreamsBuilder builder = new StreamsBuilder();
    //     KStream<String, JsonNode> stream = Topology.createStream(builder);

    //     String outputTopic = "output";
    //     stream = Topology.translate(stream);
    //     stream.to(outputTopic);

    //     TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": [\"two\", \"five\", \"one\"]}")));
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": [\"eins\", \"null\", \"null\"]}")));
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 50, \"type\": \"MOR\", \"name\": \"M-test-morse\", \"value\": [\".----\", \"..---\", \"-----\"]}")));

    //     assertEquals(
    //             deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 251}"),
    //             readOutput(driver, outputTopic));
    //     assertEquals(
    //             deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 100}"),
    //             readOutput(driver, outputTopic));
    //     assertEquals(
    //             deserializeJson("{\"time\": 50, \"type\": \"MOR\", \"name\": \"M-test-morse\", \"value\": 120}"),
    //             readOutput(driver, outputTopic));
    //     assertNull(readOutput(driver, outputTopic));

    //     driver.close();
    // }

    // public void testCorrelate() {
    //     StreamsBuilder builder = new StreamsBuilder();
    //     KStream<String, JsonNode> stream = Topology.createStream(builder);

    //     String storeName = "PT10S-Store";
    //     Topology.correlate(stream);

    //     TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

    //     // First Window
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 10010, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 1}")));
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 11000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 2}")));
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 12000, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 3}")));

    //     // Second Window
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 22000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 4}")));
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 20000, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 5}")));
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 21000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 6}")));
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 21000, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 7}")));

    //     // Third Window
    //     driver.pipeInput(createRecord(deserializeJson("{\"time\": 30000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 8}")));

    //     // Fetch all the keys for all time
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetchAll(Long.MIN_VALUE, Long.MAX_VALUE)),
    //             deserializeJson("[[{\"time\":10010,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":1},{\"time\":11000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":2}]," +
    //                             "[{\"time\":22000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":4},{\"time\":21000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":6}]," +
    //                             "[{\"time\":30000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":8}]," +
    //                             "[{\"time\":12000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":3}]," +
    //                             "[{\"time\":20000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":5},{\"time\":21000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":7}]]"));

    //     // Fetch by the English keys for all time
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", Long.MIN_VALUE, Long.MAX_VALUE)),
    //             deserializeJson("[[{\"time\":10010,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":1},{\"time\":11000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":2}]," +
    //                             "[{\"time\":22000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":4},{\"time\":21000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":6}]," +
    //                             "[{\"time\":30000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":8}]]"));

    //     // Fetch by the German keys for all time
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("G-test-german", Long.MIN_VALUE, Long.MAX_VALUE)),
    //             deserializeJson("[[{\"time\":12000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":3}]," +
    //                             "[{\"time\":20000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":5},{\"time\":21000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":7}]]"));

    //     // Fetch by the English key for a single window
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", 10000, 20000 - 1)),
    //             deserializeJson("[[{\"time\":10010,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":1},{\"time\":11000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":2}]]"));

    //     // Fetch from empty windows
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", 0, 10000 - 1)),
    //             deserializeJson("[]"));
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("G-test-english", 0, 10000 - 1)),
    //             deserializeJson("[]"));
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("G-test-english", 30000, 40000)),
    //             deserializeJson("[]"));
    //     assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", 40000, 50000)),
    //             deserializeJson("[]"));

    //     driver.close();
    // }
}
