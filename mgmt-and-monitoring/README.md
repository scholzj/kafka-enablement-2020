# Management and Monitoring

## CLI tools

### Setup

Setup and start the Zookeeper and Kafka cluster from the [Kafka Architecture demo](../kafka-architecture/).
This cluster will be used for the first part of this demo. 

### Topics

We already used a lot of commands.
You can also use the script to show only some topics in _troubles_:

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --under-replicated-partitions
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --unavailable-partitions
```

### Consumer groups

`kafka-consumer-groups.sh` lets you manage and monitor consumer groups:

```
./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --all-groups --list
```

or describe them:

```
./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --all-groups --describe
```

Check the offsets but also the consumer lag.

Reset the offset to 0:

```
/kafka-2.4.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-earliest --group replay-group --topic demo --execute
```

Reset the offset to last message:

```
./kafka-2.5.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-latest --group replay-group --topic demo --execute
```

Try other options as well:
* specific offset
* specific time

### Topic Reassignments

First we create a new topic which we will be later reassigning:

```
kafka-2.4.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic reassignment-topic --partitions 1 --replication-factor 1
```

Next, we generate reassignment file.
Check the `topics-to-move.json` file which tells the toolfor which topics should the file be generated.

```
./kafka-2.5.0/bin/kafka-reassign-partitions.sh --zookeeper localhost:2181 \
  --topics-to-move-json-file topics-to-move.json \
  --broker-list 0,1,2 \
  --generate
```

Next we can use the prepared file to trigger the reassignment.
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

The Admin APi client can be found in `admin-api`.
Go through it in IDE.

## Metrics

### Prometheus

First we need to install Prometheus.
If you run this demo in other namespace than `myproject`, you have to change the namespace in the `ClusterRoleBinding` inside `01-prometheus/prometheus.yaml`.
You can use the Prometheus operator or the files from this demo:

```
oc apply -f 01-prometheus/
```

### Grafana

Grafana is used to deploy the provided dashboards and visualize the data.
The provided YAML files deploy Grafana together with the dashbords:

```
oc apply -f 02-grafana/
```

The deployment automatically creates a route for Grafana.
The default username is `admin` and password `123456`.

### Streams

Next we need to deploy our operator and Kafka cluster with enabled metrics.

```
oc apply -f 03-streams-operator/
oc apply -f 04-kafka-with-metrics.yaml
```

Notice the metrics configuration in the Kafka CR.

### Application

Next we need to deploy an application to generate some traffic:

```
oc apply -f 05-application
```

### Monitoring

Show the dashboards and walk through them.

## Tracing

### Jaeger

Install the Jaeger operator from the Operator Hub.

After the Jaeger operator is running, create the following Jaeger resource either using the UI:

```yaml
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
  name: my-jaeger
spec:
  strategy: allInOne
  allInOne:
    image: jaegertracing/all-in-one:latest
    options:
      log-level: info
  storage:
    type: memory
    options:
      memory:
        max-traces: 100000
```

Or from command line:

```
oc apply -f 06-jaeger.yaml
```

### Application with tracing

Deploy a sample application with tracing:

```
oc apply -f 07-tracing-application.yaml
```

Check also their sources in the `tracing` directory.

### Check traces

Open the Jaeger UI and check the traces.

### Tracing in other components

Deploy Kafka Connect with tracing and FileSink connector.

```
oc apply -f 08-connect.yaml
```

Check how is tracing configured in the KafkaConnect CR.
Check how the traces changed.