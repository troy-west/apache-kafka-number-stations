package numbers;

import java.util.Objects;

public class Message {

    private long time;
    private String name;
    private String type;
    private int longitude;
    private int latitude;
    private String[] content;

    public Message() {
    }

    public Message(long time, String type, String name) {
        this.setTime(time);
        this.setName(name);
        this.type = type;
        this.setLongitude(0);
        this.setLatitude(0);
    }

    public Message(long time, String type, String name, int longitude, int latitude) {
        this.setTime(time);
        this.setName(name);
        this.setType(type);
        this.setLongitude(longitude);
        this.setLatitude(latitude);
    }

    public Message(long time, String type, String name, int longitude, int latitude, String[] content) {
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
        return "Message:" + getTime() + ":" + type + ":" + getName() + ":" + getLongitude() + ":" + getLatitude() + ":" + getContent();
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

    public void setTime(long time) {
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

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }
}
