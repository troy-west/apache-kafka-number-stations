package numbers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);

    private long time;
    private String name;
    private String type;
    private int longitude = 0;
    private int latitude = 0;
    private List<String> content;

    public Message() {
    }

    public Message(Long time, String type, String name) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.longitude = 0;
        this.latitude = 0;
    }

    public Message(long time, String type, String name, Integer longitude, Integer latitude, List<String> content) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.longitude = longitude;
        this.latitude = latitude;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public List<String> getContent() {
        return content;
    }

    public int getLongitude() {
        return longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public Message copy() {
        return new Message(this.getTime(), this.getType(), this.getName(), this.getLongitude(), this.getLatitude(), new ArrayList<>(this.content));
    }

    public Message resetContent(List<String> newContent) {
        this.content = newContent;
        return this;
    }

    public Message addContent(List<String> newContent) {
        this.content.addAll(newContent);
        return this;
    }

    public boolean equals(Object o) {
        Message message = (Message) o;

        return Objects.deepEquals(message.getContent(), this.getContent()) &&
                Objects.equals(message.getTime(), this.getTime()) &&
                Objects.equals(message.getName(), this.getName()) &&
                Objects.equals(message.getType(), this.getType()) &&
                Objects.equals(message.getLongitude(), this.getLongitude()) &&
                Objects.equals(message.getLatitude(), this.getLatitude());
    }

    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing record", e);
            return "";
        }
    }
}

