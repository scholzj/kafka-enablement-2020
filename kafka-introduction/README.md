# Kafka introduction

## Start ZooKeeper

* First before we start Kafka, we have to start ZooKeeper:

```
./kafka-2.5.0/bin/zookeeper-server-start.sh ./kafka-2.5.0/config/zookeeper.properties
```

## Start Kafka

* Next we can start Kafka:

```
./kafka-2.5.0/bin/kafka-server-start.sh ./kafka-2.5.0/config/server.properties
```

## Create and list topics

* List the topics using ZooKeeper:

```
./kafka-2.5.0/bin/kafka-topics.sh --zookeeper localhost:2181 --list
```

* Or using Kafka:

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

* Create new topic which we will use

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic my-topic --partitions 3 --replication-factor 1
```

* List the topics again to see it was created.
* Describe the topic to see more details:

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
```

## Consuming and producing messages

* Start the console producer:

```
./kafka-2.5.0/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic my-topic
```

* Wait until it is ready (it should show `>`).
* Once ready, send some message by typing the message payload and pressing enter to send.
* Next we can consume the messages

```
./kafka-2.5.0/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my-topic
```

* But this will by default show only new messages. You can add `--from-beginning` to see the messages sent in the past:

```
./kafka-2.5.0/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my-topic --from-beginning
```

* You can also check the consumer groups:

```
./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --all-groups
```

## Kafka Connect

* Edit the file `./kafka-2.5.0/config/connect-file-sink.properties` and change the `topics` field to `my-topic` and add the two following fields:

```
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter
```

* Run Kafka Connect with the file sink connector:

```
./kafka-2.5.0/bin/connect-standalone.sh ./kafka-2.5.0/config/connect-standalone.properties ./kafka-2.5.0/config/connect-file-sink.properties
```

* Check the output file `test.sink.txt` to verify it got the messages:

```
cat test.sink.txt
```