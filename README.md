# Apache Kafka Three Ways: Number Stations

Use in unison with the TW AK3W Workshop: https://kafka.troywest.com

# Background

We have captured a mysterious ~3hr broadcast of global Number Station data.

In raw form it is 1.5M messages in different languages.

Can we filter it, branch it, translate it, correlate it, and decode the hidden message?

# Initialize Kafka + Kakfa Tools

Using [troy-west/apache-kafka-cli-tools](https://github.com/troy-west/apache-kafka-cli-tools)

Start a 3-node Kafka Cluster and enter a shell with all kafka-tools scripts:
```sh
docker-compose down
docker-compose up -d
docker-compose -f docker-compose.tools.yml run kafka-tools
```

## Monitor

In a new terminal, view the running kafka logs:
```sh
docker-compose logs -f
```

Create a new Topic 'radio-logs' with 12 partitions and RF=3:

```sh
# ./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --create --topic radio-logs --partitions 12 --replication-factor 3
```

Confirm the new topic has been created:

```sh
# ./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --list

radio-logs
```

Describe the new topic:

```sh
# ./bin/kafka-topics.sh --bootstrap-server kafka-1:19092 --describe --topic radio-logs

Topic:radio-logs	PartitionCount:12	ReplicationFactor:3	Configs:
	Topic: radio-logs	Partition: 0	Leader: 3	Replicas: 3,2,1	Isr: 3,2,1
	Topic: radio-logs	Partition: 1	Leader: 1	Replicas: 1,3,2	Isr: 1,3,2
	Topic: radio-logs	Partition: 2	Leader: 2	Replicas: 2,1,3	Isr: 2,1,3
	Topic: radio-logs	Partition: 3	Leader: 3	Replicas: 3,1,2	Isr: 3,1,2
	Topic: radio-logs	Partition: 4	Leader: 1	Replicas: 1,2,3	Isr: 1,2,3
	Topic: radio-logs	Partition: 5	Leader: 2	Replicas: 2,3,1	Isr: 2,3,1
	Topic: radio-logs	Partition: 6	Leader: 3	Replicas: 3,2,1	Isr: 3,2,1
	Topic: radio-logs	Partition: 7	Leader: 1	Replicas: 1,3,2	Isr: 1,3,2
	Topic: radio-logs	Partition: 8	Leader: 2	Replicas: 2,1,3	Isr: 2,1,3
	Topic: radio-logs	Partition: 9	Leader: 3	Replicas: 3,1,2	Isr: 3,1,2
	Topic: radio-logs	Partition: 10	Leader: 1	Replicas: 1,2,3	Isr: 1,2,3
	Topic: radio-logs	Partition: 11	Leader: 2	Replicas: 2,3,1	Isr: 2,3,1
```

Take a look at the data in each partition (initially empty)

```
./bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list kafka-1:19092 --topic radio-logs --time -1

radio-logs:0:0
radio-logs:1:0
radio-logs:2:0
radio-logs:3:0
radio-logs:4:0
radio-logs:5:0
radio-logs:6:0
radio-logs:7:0
radio-logs:8:0
radio-logs:9:0
radio-logs:10:0
radio-logs:11:0
```

# Decoding the secret message, part by part.

At any time, run all the project tests with ```mvn test```

# 1. Sample the radio

Take a look at the sort of messages we are working with

mvn compile exec:java -Dexec.mainClass="numbers.SecretRadio"

# 2. Implement the JSON Serializer / Deserializer

Get numbers.JsonDeserializerTest and numbers.JsonSerializerTest working so that we can produce messages to radio-logs.

# 3. Implement Producer.main

Send each message returned by SecretRadio.listen() to the radio-logs topic

# 4. Complete the Compute toplogy by fixing every test in ComputeTest

Also remember to log/info each Scott Base message (there's no test for that)

# 5. When all the tests are passing, run the application against your local Kafka Cluster

mvn clean compile exec:java -Dexec.mainClass="numbers.App"

Navigate to localhost:8080 to inspect the decoded message!

While the logs are being computed you can check on progress by looking at the offsets of the consumer group

```
./bin/kafka-consumer-groups.sh --bootstrap-server kafka-1:19092 --group compute-radio-logs --describe

TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                                                                          HOST            CLIENT-ID
radio-logs      11         27541           103504          75963           compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      4          27670           129498          101828          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      10         27447           97830           70383           compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      6          18714           135357          116643          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      3          18729           144000          125271          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      8          27688           117879          90191           compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      0          18735           152640          133905          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      7          21479           143891          122412          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      1          18390           89205           70815           compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      2          27770           158125          130355          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      9          18719           155520          136801          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
radio-logs      5          27747           129372          101625          compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer-c21b4661-18fb-422b-981c-38b0615950c1 /172.27.0.1     compute-radio-logs-5a520771-acfe-49d4-bf44-8976ff8d23cf-StreamThread-1-consumer
```
As you progress through this project you can always reset the consumer offets like so:

```
./bin/kafka-consumer-groups.sh --bootstrap-server kafka-1:19092 --group compute-radio-logs --reset-offsets --to-earliest --execute --topic radio-logs
```

# Parallelising our Compute

What happens when you run more than one application (say on ports 8081, 8082, 8083), and why?

# Build the app JAR

mvn clean compile package

# Run multiple versions of the app at once on different ports

java -jar target/apache-kafka-java-number-stations-1.0-SNAPSHOT-jar-with-dependencies.jar 8080 &
java -jar target/apache-kafka-java-number-stations-1.0-SNAPSHOT-jar-with-dependencies.jar 8081 &

It's a case for interactive queries!
