# Run App

mvn clean compile exec:java -Dexec.mainClass="numbers.system"

# Run Tests

mvn test

# Run Producer

mvn compile exec:java -Dexec.mainClass="numbers.radio"

# Building the web app

mvn clean compile package

# Running the web app

java -jar target/apache-kafka-java-number-stations-1.0-SNAPSHOT-jar-with-dependencies.jar 8080 &
java -jar target/apache-kafka-java-number-stations-1.0-SNAPSHOT-jar-with-dependencies.jar 8081 &