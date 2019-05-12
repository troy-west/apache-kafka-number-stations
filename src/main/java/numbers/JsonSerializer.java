package numbers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonSerializer<T> implements Serializer<T> {

    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public byte[] serialize(String topic, T o) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();

        // TODO: Implement me. Use the object mapper to turn types into bytes

        return retVal;
    }

    @Override
    public void close() {
    }
}
