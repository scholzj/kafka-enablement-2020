# Management

## CLI tools

### Setup

Setup and start the Zookeeper and Kafka cluster from the [Kafka Architecture demo](../kafka-architecture/).
This cluster will be used for this lab. 

### Topics

* We already used a lot of commands.
You can also use the script to show only some topics in _troubles_:

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --under-replicated-partitions
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --unavailable-partitions
```

### Consumer groups

* `kafka-consumer-groups.sh` lets you manage and monitor consumer groups:

```
./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --all-groups --list
```

* or describe them:

```
./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --all-groups --describe
```

* Check the offsets but also the consumer lag.
* Reset the offset to 0:

```
/./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-earliest --group replay-group --topic demo --execute
```

* Reset the offset to last message:

```
./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-latest --group replay-group --topic demo --execute
```

* Try other options as well:
    * specific offset
    * specific time

### Topic Reassignments

* First, create a new topic which will be later used for reassigning:

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic reassignment-topic --partitions 1 --replication-factor 1
```

* Next, we generate reassignment file.
* Check the `topics-to-move.json` file which tells the toolfor which topics should the file be generated.

```
./kafka-2.5.0/bin/kafka-reassign-partitions.sh --zookeeper localhost:2181 \
  --topics-to-move-json-file topics-to-move.json \
  --broker-list 0,1,2 \
  --generate
```

* Next we can use the prepared file to trigger the reassignment.
You can throttle the reassignment process to avoid affecting other workloads.
This command needs to write into Zookeeper.
So it needs to authenticate first.

```
export KAFKA_OPTS="-Djava.security.auth.login.config=../kafka-architecture/configs/kafka/jaas.config"
./kafka-2.5.0/bin/kafka-reassign-partitions.sh --zookeeper localhost:2181 \
  --reassignment-json-file reassign-partition.json \
  --throttle 5000000 \
  --execute
```

## Admin API

* The Admin API client can be found in `admin-api`.
* Open it in your IDE and run it
    * Check the code
    * Check the other methods the Admin API has
