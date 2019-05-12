package numbers;

import junit.framework.TestCase;

import java.util.List;

public class JsonDeserializerTest extends TestCase {

    public void testDeserialize() {
        String jsonText = "{\"time\":1557125670789,\"name\":\"85\",\"content\":[\"eins\",\"null\",\"sechs\"],\"type\":\"GER\",\"longitude\":-92,\"latitude\":-30}";

        Message actual = new JsonDeserializer<>(Message.class).deserialize("test-topic", jsonText.getBytes());
        Message expected = new Message(1557125670789L, "GER", "85", -92, -30, List.of("eins", "null", "sechs"));

        assertEquals(actual, expected);
    }
}