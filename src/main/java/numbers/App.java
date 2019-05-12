package numbers;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("uncaught exception on" + t.getName(), e));

        StreamsBuilder builder = new StreamsBuilder();
        Compute.topology(builder);

        KafkaStreams streams = new KafkaStreams(builder.build(), Compute.config);
        streams.start();

        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("numbers.http"));
        IFn start = Clojure.var("numbers.http", "start!");

        System.out.println("Start the number stations app!");
        start.invoke(8080, streams);
    }
}
