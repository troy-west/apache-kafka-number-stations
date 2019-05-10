package numbers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class Json {
    public static <T> T deserialize(String content, Class<T> expectedType) throws javax.xml.bind.JAXBException {
        JAXBContext jc = JAXBContext.newInstance(expectedType);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);

        StreamSource json = new StreamSource(new StringReader(content));

        return unmarshaller.unmarshal(json, expectedType).getValue();
    }

    public static String serialize(Message content) throws javax.xml.bind.JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Message.class);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
        marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

        StringWriter json = new StringWriter();

        marshaller.marshal(content, json);

        return json.toString();
    }
}
