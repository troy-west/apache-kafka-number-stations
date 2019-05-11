package numbers;

import junit.framework.TestCase;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerializerTest extends TestCase {

    public void testSerialize() {
        Message message = new Message();
        message.name = "test";

        Serializer ser = new JsonSerializer();
        byte[] bytes = ser.serialize("test-topic", message);
        assertEquals("{\"time\":0,\"name\":\"test\",\"content\":null,\"type\":null,\"longitude\":0,\"latitude\":0,\"number\":null,\"numbers\":null}", new String(bytes));
    }

}