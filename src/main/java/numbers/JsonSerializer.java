package numbers;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonSerializer implements Serializer<Message> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, Message data) {
        if (data == null)
            return null;

        try {
            return Json.serialize(data).getBytes();
        } catch (Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    @Override
    public void close() {

    }
}
