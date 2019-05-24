package numbers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonDeserializer<T> implements Deserializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);
    private ObjectMapper mapper = new ObjectMapper();
    private Class<T> type;

    public JsonDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public T deserialize(String s, byte[] bytes) {

        // TODO: Implement Me. Use the ObjectMapper to deserialize bytes into types
        // TODO: Question - what's the preferred action when a deserialization exception occurs, and why?

        return null;
    }

    @Override
    public void close() {
    }
}