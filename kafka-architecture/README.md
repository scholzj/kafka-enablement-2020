# Kafka Architecture

## Setup

### SSL

First we need to generate the certificates.
For that you need to have the following things installed:
* OpenSSL
* CFSSL
* Java (keytool)

Generate the certificates by running:

```
./ssl/generate.sh
```

### Start ZooKeeper cluster

First before we start Kafka, we have to start ZooKeeper cluster.
We will use 3 node cluster.
Start the 3 Zookeeper nodes by running these 3 scripts in different terminals:

```
./scripts/zookeeper-0.sh
./scripts/zookeeper-1.sh
./scripts/zookeeper-2.sh
```

### Start Kafka cluster

We will use 3 node Kafka cluster
Start the 3 Kafka nodes by running these 3 scripts in different terminals:

```
./scripts/kafka-0.sh
./scripts/kafka-1.sh
./scripts/kafka-2.sh
```

### Start Kafka Connect cluster

The introduction demo showed standalone Kafka Connect.
Now we are going to use distributed Kafka Connect with 3 worker nodes.
Start the 3 Kafka nodes by running these 3 scripts in different terminals:

```
./scripts/connect-0.sh
./scripts/connect-1.sh
./scripts/connect-2.sh
```

## Configs

### Zookeeper configuration

* Show Zookeeper config files
* Explain the `myid` file which needs to be created
* Show the ensemble configuration

### Kafka configuration

* Show Kafka configuration files
* Show `broker.id`
* Show listeners, advertised listeners, protocols
* Show Zookeeper config
* Show journal files
* Show other mgmt tools for reassigning topics etc.

## Zookeeper

### Show what Kafka does in Zookeeper

* Explain that ZK is integrated into the Kafka distribution file
* Start the ZK client

```
bin/zookeeper-shell.sh localhost:2181
```

* Show some paths in ZK

```
ls /
get /controller
ls /brokers
ls /brokers/ids
get /brokers/ids/0
ls /brokers/topics
```

* Show netcat dump with connected brokers

```
echo dump | nc localhost 2181
```

* Kill one of the brokers and show how it disappers

```
echo dump | nc localhost 2181
```

## Basics

### Create topic

```
bin/kafka-topics.sh --zookeeper --bootstrap-server localhost:9092 --create --topic demo --partitions 3 --replication-factor 3
```

### Check the created topic

```
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list --topic demo
bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic demo
```

Notice the distribution of leaders and the ISR replicas. Explain also the RackID feature.

### Send some messages

```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic demo
```

### Consume messages

* Read from the whole topic

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo --from-beginning
```

* Notice how the messages are out of order. And check how nicely ordered they are in a single partition.

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo --partition 0 --from-beginning
```

* Show reading from a particular offset

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo --partition 0 --offset 2
```

## Replication

### Broker crash

* Kill one of the brokers
* Show again the topic description with the leaders which changed and new ISR

```
bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic demo
```

### Consume messages

* Show that the messages are still in the topic!

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo --from-beginning
```

### Send some messages

```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic demo
```

### Start the broker again

* Leadership didn't changed, but all replicas are again ISR


## Consumer Groups


### Create a new topic to get rid of the old messages

```
bin/kafka-topics.sh --zookeeper --bootstrap-server localhost:9092 --delete --topic demo
bin/kafka-topics.sh --zookeeper --bootstrap-server localhost:9092 --create --topic demo --partitions 3 --replication-factor 3
```

### Setup consumers

* Open 3 consumers using the same group `group-1`

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo --from-beginning --property print.key=true --property key.separator=":" --group group-1
```

* Open consumer using a different group `group-2`

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo --from-beginning  --property print.key=true --property key.separator=":" --group group-2
```

### Send messages

* Send some messages with keys

```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic demo --property "parse.key=true" --property "key.separator=:"
```

### Rebalancing consumer group

* Kill one of the consumers started before
* Send some messages with the same key as was used before for this consumer
* Show that one fo the other consumers got the partition assigned and will receive it




































## Kafka Connect

* Show Kafka connect configuration files
* Show the plugin path and explain how it works
* Show basics of the rest interface

```
curl -s http://localhost:8083/ | jq
curl -s http://localhost:8083/connectors | jq
curl -s http://localhost:8083/connector-plugins | jq
```




