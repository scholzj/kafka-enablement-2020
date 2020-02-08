# Management and Monitoring

## CLI tools

### Setup

Setup and start the Zookeeper and Kafka cluster from the [Kafka Architecture demo](../kafka-architecture/).
This cluster will be used for the first part of this demo. 

### Consumer groups

`kafka-consumer-groups.sh` lets you manage and monitor consumer groups:

```
./kafka-2.4.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --all-groups --list
```

or describe them:

```
./kafka-2.4.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --all-groups --describe
```

Check the offsets but also the consumer lag.

Reset the offset to 0:

```
/kafka-2.4.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-earliest --group replay-group --topic demo --execute
```

Reset the offset to last message:

```
./kafka-2.4.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --reset-offsets --to-latest --group replay-group --topic demo --execute
```

Try other options as well:
* specific offset
* specific time

## Admin API

TODO

## Metrics

### Prometheus

First we need to install Prometheus.
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
And create the following Jaeger resource either using the UI:

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