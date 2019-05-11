package numbers;

import junit.framework.TestCase;

public class JsonSerializerTest extends TestCase {

    public void testSerialize() {
        Message message = new Message(1557125670789L, "GER", "85", -92, -30, new String[]{"eins", "null", "sechs"});

        String actual = new String(new JsonSerializer().serialize("test-topic", message));
        String expected = "{\"time\":1557125670789,\"name\":\"85\",\"type\":\"GER\",\"longitude\":-92,\"latitude\":-30,\"content\":[\"eins\",\"null\",\"sechs\"]}";

        assertEquals(expected, actual);
    }
}