# Run App

mvn exec:java -Dexec.mainClass="numbers.system"

# Run Tests

mvn test

# Run Producer

mvn exec:java -Dexec.mainClass="numbers.Producer"

# Building the web app

mvn package

# Running the web app

java -jar target/apache-kafka-java-number-stations-1.0-SNAPSHOT-jar-with-dependencies.jar