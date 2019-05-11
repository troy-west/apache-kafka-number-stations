package numbers;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class MessageSerde implements Serde {

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer serializer() {
        return new JsonSerializer();
    }

    @Override
    public Deserializer deserializer() {
        return new JsonDeserializer<>(Message.class);
    }
}
