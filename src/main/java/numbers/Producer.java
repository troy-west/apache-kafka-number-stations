package numbers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class Producer {

    public static void main(String[] args) {

        System.out.println("Produce to radio-logs topic!");

        Properties config = new Properties();
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // TODO: Implement me. Send each message from SecretRadio.listen() to local kafka (config above)

        System.out.println("Finished producing to radio-logs topic!");
    }
}
