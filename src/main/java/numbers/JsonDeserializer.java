package numbers;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import java.nio.charset.StandardCharsets;

import java.util.Map;

public class JsonDeserializer implements Deserializer<Message> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public Message deserialize(String s, byte[] bytes) {
        if (bytes == null)
            return null;

        try {
            return Json.deserialize(new String(bytes, StandardCharsets.UTF_8), Message.class);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public void close() {

    }
}
