# Management and Monitoring

This lab shows how we can monitor Kafka on OCP using Prometheus, Grafana.
It also shows how we can trace messages using Jaeger and Open Tracing.

## Metrics

### Prometheus

First we need to install Prometheus.

* Install Prometheus Operator from OCP Operator Hub
* Deploy the Prometheus server

```
oc apply -f 01-prometheus/
```

_(If you run this demo in other namespace than `myproject`, you have to change the namespace inside `01-prometheus/prometheus-server.yaml`.)_

### Grafana

Grafana is used to deploy the provided dashboards and visualize the data.
The provided YAML files deploy Grafana together with the dashbords:

* Install the Grafana Operator

```
oc apply -f 02-grafana/grafana-operator.yaml
```

_(If you run this demo in other namespace than `myproject`, you have to change the namespace inside `01-prometheus/prometheus-server.yaml`.)_

* Install the Grafana server and dashboards

```
oc apply -f 02-grafana/grafana-server.yaml
```

The deployment automatically creates a route for Grafana.
The default username is `admin` and password `123456`.

### Streams

Next we need to deploy our operator and Kafka cluster with enabled metrics.

```
oc apply -f 03-operator/
oc apply -f 04-kafka-with-metrics.yaml
```

Notice the metrics configuration in the Kafka CR.

### Application

Next we need to deploy an application to generate some traffic:

```
oc apply -f 05-application.yaml
```

### Monitoring

* Open Grafana using the route
* Go through the dashboards

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

* Deploy a sample application with tracing:

```
oc apply -f 07-tracing-application.yaml
```

* Check also their sources in the `tracing` directory.

### Check traces

* Open the Jaeger UI and check the traces.

### Tracing in other components

* Deploy Kafka Connect with tracing and FileSink connector.

```
oc apply -f 08-connect.yaml
```

* Check how is tracing configured in the KafkaConnect CR.
* Check how the traces changed.