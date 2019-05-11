package numbers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Message {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);

    private long time;
    private String name;
    private String type;
    private int longitude = 0;
    private int latitude = 0;
    private String[] content;

    public Message() {
    }

    public Message(Long time, String type, String name) {
        this.setTime(time);
        this.setName(name);
        this.type = type;
        this.setLongitude(0);
        this.setLatitude(0);
    }

    public Message(long time, String type, String name, Integer longitude, Integer latitude) {
        this.setTime(time);
        this.setName(name);
        this.setType(type);
        this.setLongitude(longitude);
        this.setLatitude(latitude);
    }

    public Message(long time, String type, String name, Integer longitude, Integer latitude, String[] content) {
        this.setTime(time);
        this.setName(name);
        this.setType(type);
        this.setLongitude(longitude);
        this.setLatitude(latitude);
        this.setContent(content);
    }

    public Message copy() {
        return new Message(this.getTime(), getType(), this.getName(), this.getLongitude(), this.getLatitude());
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


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(Integer longitude) {
        if (longitude != null) this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(Integer latitude) {
        if (latitude != null) this.latitude = latitude;
    }
}
