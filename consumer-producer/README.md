# Kafka Architecture

## Setup

Setup and start the Zookeeper and Kafka cluster from the [Kafka Architecture demo](../kafka-architecture/).
This cluster will be used during this demo. 

## Topic

Create new topic for out test messages:

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic weather-report --partitions 1 --replication-factor 3
```

## Consumer

Start a console consumer to see the sent messages:

```
./kafka-2.5.0/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic weather-report --from-beginning --property print.key=true --property key.separator=":     "
```

## Consumer and Producer APIs

Open the attached Java project in IDE and go through the files.
You can run them against the brokers.
There are several consumer and producer pair showing different options:

* Super simple basic consumer producer
* Acks and manual commits
* SSL and SSL Authentication
* SASL
* Serialization and deserialization
