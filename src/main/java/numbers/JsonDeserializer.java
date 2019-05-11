package numbers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonDeserializer<T> implements Deserializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);
    private Class<T> type;

    public JsonDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        T obj = null;
        try {
            obj = mapper.readValue(bytes, type);
        } catch (Exception e) {
            logger.error("Error deserializing record", e);
        }
        return obj;
    }

    @Override
    public void close() {
    }
}