package numbers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message {
    public long time;
    public String name;
    public String[] content;
    public String type;
    @XmlElement(name="long", nillable=true)
    public int longitude;
    public int lat;
    public Integer number;
    public int numbers[];
}
