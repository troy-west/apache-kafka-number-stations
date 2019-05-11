package numbers;

import java.util.Objects;

public class Message {
    public long time;
    public String name;
    public String[] content;
    public String type;
    public int longitude;
    public int latitude;
    public Integer number;
    public int[] numbers;

    public Message() {
    }

    public Message(long time, String type, String name) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.longitude = 0;
        this.latitude = 0;
    }

    public Message(long time, String type, String name, int longitude, int latitude, String[] content) {
        this.time = time;
        this.name = name;
        this.type = type;
        this.longitude = longitude;
        this.latitude = latitude;
        this.content = content;
    }

    public boolean equals(Object o) {
        Message message = (Message) o;

        return Objects.deepEquals(message.content, this.content) &&
                Objects.deepEquals(message.numbers, this.numbers) &&
                Objects.equals(message.time, this.time) &&
                Objects.equals(message.name, this.name) &&
                Objects.equals(message.type, this.type) &&
                Objects.equals(message.longitude, this.longitude) &&
                Objects.equals(message.latitude, this.latitude) &&
                Objects.equals(message.number, this.number);
    }

}
