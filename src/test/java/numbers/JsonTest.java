package numbers;

import junit.framework.TestCase;

public class JsonTest extends TestCase {

    public void testJsonSerialize() throws javax.xml.bind.JAXBException {
        Message message = new Message();
        message.name = "test";

        assertEquals("{\"time\":0,\"name\":\"test\",\"long\":0,\"lat\":0}", Json.serialize(message));
    }

    public void testJsonSerialize2() throws javax.xml.bind.JAXBException {
        Message message = new Message();
        message.time = 10L;
        message.type = "ENG";
        message.name = "E-test-english";
        message.content = new String[] { "two", "five", "one" };

        Json.serialize(message);
    }

    public void testJsonDeserialize() throws javax.xml.bind.JAXBException {
        Message root = Json.deserialize("{\"name\": \"123\", \"long\": 22}", Message.class);
        assertEquals("123", root.name);
        assertEquals(22, root.longitude);
    }
}
