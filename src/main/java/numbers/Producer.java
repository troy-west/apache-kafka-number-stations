package numbers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class Producer {

    public static void main( String[] args )
    {
        Properties config = new Properties();
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        KafkaProducer<String, Message> producer = new KafkaProducer<>(config, new StringSerializer(), new JsonSerializer<>());

        System.out.println( "Produce radio-logs!" );
        for (Message message: JavaRadio.listen()) {
            producer.send(new ProducerRecord<>("radio-logs", message.getName(), message));
        }
        System.out.println( "Finished producing radio-logs!");
    }
}
