package numbers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonSerializer implements Serializer<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);

    @Override
    public void configure(Map map, boolean b) {
    }

    @Override
    public byte[] serialize(String topic, Object o) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(o);
        } catch (Exception e) {
            logger.error("Error serializing record", e);
        }
        return retVal;
    }

    @Override
    public void close() {
    }
}
