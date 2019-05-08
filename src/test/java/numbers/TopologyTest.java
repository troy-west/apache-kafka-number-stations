package numbers;

import com.fasterxml.jackson.databind.JsonNode;
import junit.framework.TestCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

public class TopologyTest extends TestCase {

    private static String inputTopic = "radio-logs";
    private static ConsumerRecordFactory<String, JsonNode> recordFactory =
            new ConsumerRecordFactory<>(inputTopic, new StringSerializer(), new JsonSerializer());

    private static JsonNode deserializeJson(String json) {
        return new JsonDeserializer().deserialize("", json.getBytes());
    }

    private static ConsumerRecord<byte[], byte[]> createRecord(JsonNode value) {
        return recordFactory.create(inputTopic, value.get("name").asText(), value);
    }

    private static JsonNode readOutput(TopologyTestDriver driver, String topic) {
        ProducerRecord<String, JsonNode> output = driver.readOutput(topic, new StringDeserializer(), new JsonDeserializer());
        if (output != null) {
            return output.value();
        } else {
            return null;
        }
    }

    public void testFilterRecognized() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.filterRecognized(stream);
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        JsonNode expected1 = deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"numbers\": [\"three\", \"two\", \"one\"]}");
        JsonNode notExpected = deserializeJson("{\"time\": 20, \"name\": \"X-unknown\"}");
        JsonNode expected2 = deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"numbers\": [\"eins\", \"null\", \"null\"]}");

        driver.pipeInput(createRecord(expected1));
        driver.pipeInput(createRecord(notExpected));
        driver.pipeInput(createRecord(expected2));

        assertEquals(expected1, readOutput(driver, outputTopic));
        assertEquals(expected2, readOutput(driver, outputTopic));
        assertNull(readOutput(driver, outputTopic));

        driver.close();
    }

    public void testTranslate() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.translate(stream);
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        driver.pipeInput(createRecord(deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"numbers\": [\"three\", \"two\", \"one\"]}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"numbers\": [\"eins\", \"null\", \"null\"]}")));

        assertEquals(
                deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"number\": 321}"),
                readOutput(driver, outputTopic));
        assertEquals(
                deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"number\": 100}"),
                readOutput(driver, outputTopic));
        assertNull(readOutput(driver, outputTopic));

        driver.close();
    }

    public void testCorrelate() {
    }
}