package numbers;

import junit.framework.TestCase;

public class JsonDeserializerTest extends TestCase {

    public void testDeserialize() {
        String jsonText = "{\"time\":1557125670789,\"name\":\"85\",\"content\":[\"eins\",\"null\",\"sechs\"],\"type\":\"GER\",\"longitude\":-92,\"latitude\":-30}";

        Message actual = new JsonDeserializer<>(Message.class).deserialize("test-topic", jsonText.getBytes());
        Message expected = new Message(1557125670789L, "GER", "85", -92, -30, new String[]{"eins", "null", "sechs"});

        assertEquals(actual, expected);
    }
}