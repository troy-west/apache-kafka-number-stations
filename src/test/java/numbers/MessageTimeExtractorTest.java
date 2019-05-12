package numbers;

import junit.framework.TestCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

public class MessageTimeExtractorTest extends TestCase {

    public void testExtract() {
        Message actual = new Message(1557125670789L, "GER", "85", -92, -30, List.of("eins", "null", "sechs"));

        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("radio-logs", 1, 1, "85", actual);

        assertEquals(1557125670789L, new MessageTimeExtractor().extract(record, 0));
    }
}