package numbers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.function.Consumer;

public class TopologyTest extends TestCase {

    private static ObjectMapper mapper = new ObjectMapper();

    private static String inputTopic = "radio-logs";
    private static ConsumerRecordFactory<String, JsonNode> recordFactory =
            new ConsumerRecordFactory<>(inputTopic, new StringSerializer(), new JsonSerializer());

    private static JsonNode deserializeJson(String json) {
        return new JsonDeserializer().deserialize("", json.getBytes());
    }

    public <T> T jaxDeserializeJson(String content, Class<T> expectedType) throws javax.xml.bind.JAXBException {
        JAXBContext jc = JAXBContext.newInstance(expectedType);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);

        StreamSource json = new StreamSource(new StringReader(content));

        return unmarshaller.unmarshal(json, expectedType).getValue();
    }

    public <T> String jaxSerializeJson(Object content, Class<T> expectedType) throws javax.xml.bind.JAXBException {
        JAXBContext jc = JAXBContext.newInstance(expectedType);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
        marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

        StringWriter json = new StringWriter();

        marshaller.marshal(content, json);

        return json.toString();
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

    private static ArrayNode getWindowValues(KeyValueIterator iterator) {
        ArrayNode windowValues = mapper.createArrayNode();
        iterator.forEachRemaining(new Consumer<KeyValue>() {
            @Override
            public void accept(KeyValue o) {
                windowValues.add((ArrayNode) o.value);
            }
        });
        return windowValues;
    }

    @XmlRootElement
    public static class Root {
        public int a;
    }

    public void testJaxbSerialize() throws javax.xml.bind.JAXBException {
        Root root = new Root();
        root.a = 1;

        System.out.println(jaxSerializeJson(root, Root.class));
    }

    public void testJaxbDeserialize() throws javax.xml.bind.JAXBException {
        Root root = jaxDeserializeJson("{\"a\": 1}", Root.class);

        System.out.println(root.a);
    }

    public void testFilterRecognized() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> stream = Topology.createStream(builder);

        String outputTopic = "output";
        stream = Topology.filterRecognized(stream);
        stream.to(outputTopic);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        JsonNode expected1 = deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": [\"two\", \"five\", \"one\"]}");
        JsonNode notExpected = deserializeJson("{\"time\": 20, \"name\": \"X-unknown\"}");
        JsonNode expected2 = deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": [\"eins\", \"null\", \"null\"]}");

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

        driver.pipeInput(createRecord(deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": [\"two\", \"five\", \"one\"]}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": [\"eins\", \"null\", \"null\"]}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 50, \"type\": \"MOR\", \"name\": \"M-test-morse\", \"value\": [\".----\", \"..---\", \"-----\"]}")));

        assertEquals(
                deserializeJson("{\"time\": 10, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 251}"),
                readOutput(driver, outputTopic));
        assertEquals(
                deserializeJson("{\"time\": 30, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 100}"),
                readOutput(driver, outputTopic));
        assertEquals(
                deserializeJson("{\"time\": 50, \"type\": \"MOR\", \"name\": \"M-test-morse\", \"value\": 120}"),
                readOutput(driver, outputTopic));
        assertNull(readOutput(driver, outputTopic));

        driver.close();
    }

    public void testCorrelate() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> stream = Topology.createStream(builder);

        String storeName = "PT10S-Store";
        Topology.correlate(stream);

        TopologyTestDriver driver = new TopologyTestDriver(builder.build(), Topology.config);

        // First Window
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 10010, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 1}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 11000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 2}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 12000, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 3}")));

        // Second Window
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 22000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 4}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 20000, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 5}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 21000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 6}")));
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 21000, \"type\": \"GER\", \"name\": \"G-test-german\", \"value\": 7}")));

        // Third Window
        driver.pipeInput(createRecord(deserializeJson("{\"time\": 30000, \"type\": \"ENG\", \"name\": \"E-test-english\", \"value\": 8}")));

        // Fetch all the keys for all time
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetchAll(Long.MIN_VALUE, Long.MAX_VALUE)),
                deserializeJson("[[{\"time\":10010,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":1},{\"time\":11000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":2}]," +
                                "[{\"time\":22000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":4},{\"time\":21000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":6}]," +
                                "[{\"time\":30000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":8}]," +
                                "[{\"time\":12000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":3}]," +
                                "[{\"time\":20000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":5},{\"time\":21000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":7}]]"));

        // Fetch by the English keys for all time
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", Long.MIN_VALUE, Long.MAX_VALUE)),
                deserializeJson("[[{\"time\":10010,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":1},{\"time\":11000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":2}]," +
                                "[{\"time\":22000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":4},{\"time\":21000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":6}]," +
                                "[{\"time\":30000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":8}]]"));

        // Fetch by the German keys for all time
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("G-test-german", Long.MIN_VALUE, Long.MAX_VALUE)),
                deserializeJson("[[{\"time\":12000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":3}]," +
                                "[{\"time\":20000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":5},{\"time\":21000,\"type\":\"GER\",\"name\":\"G-test-german\",\"value\":7}]]"));

        // Fetch by the English key for a single window
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", 10000, 20000 - 1)),
                deserializeJson("[[{\"time\":10010,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":1},{\"time\":11000,\"type\":\"ENG\",\"name\":\"E-test-english\",\"value\":2}]]"));

        // Fetch from empty windows
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", 0, 10000 - 1)),
                deserializeJson("[]"));
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("G-test-english", 0, 10000 - 1)),
                deserializeJson("[]"));
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("G-test-english", 30000, 40000)),
                deserializeJson("[]"));
        assertEquals(getWindowValues(driver.getWindowStore(storeName).fetch("E-test-english", 40000, 50000)),
                deserializeJson("[]"));

        driver.close();
    }
}
