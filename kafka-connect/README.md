# Kafka Architecture

This lab shows how to use Kafka Connect on RHEL and on OCP.

## On RHEL

### Start Zookeeper

* Open a new terminal and start Zookeeper

```
./kafka-2.5.0/bin/zookeeper-server-start.sh kafka-2.5.0/config/zookeeper.properties
```

### Start Kafka broker

* Open a new terminal and start Kafka broker

```
./kafka-2.5.0/bin/kafka-server-start.sh kafka-2.5.0/config/server.properties
```

* Create a topic which we will use in this demo:

```
./kafka-2.5.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic demo --partitions 3 --replication-factor 1
```

### Start Kafka Connect

* The introduction demo showed standalone Kafka Connect.
Now we are going to use distributed Kafka Connect with 3 worker nodes.
* Edit the distributed Connect configuration file (`kafka-2.5.0/config/connect-distributed.properties`) and set the plugin path to point to our folder with Camel Kafka connectors (`./docker-image/connectors`)

```
plugin.path=./docker-image/connectors/
```

* Open a new terminal and start Kafka Connect:

```
./kafka-2.5.0/bin/connect-distributed.sh kafka-2.5.0/config/connect-distributed.properties
```

### Kafka Connect

* Play the Kafka Connect REST interface and check that it found the Camel Kafka Connectors

```
curl -s http://localhost:8083/ | jq
curl -s http://localhost:8083/connectors | jq
curl -s http://localhost:8083/connector-plugins | jq
```

### Deploy CamelSink plugin

* Go to [https://webhook.site/](https://webhook.site/) get a new webhook URL.
* Copy the URL and paster it in `http-sink-connector.json` into `camel.sink.path.httpUri` option.
And create the connector:

```
curl -X POST -H "Content-Type: application/json" -d @http-sink-connector.json http://localhost:8083/connectors | jq
```

* Check the connector status:

```
curl -s http://localhost:8083/connectors | jq
curl -s http://localhost:8083/connectors/http-sink | jq
curl -s http://localhost:8083/connectors/http-sink/status | jq
curl -s http://localhost:8083/connectors/http-sink/config | jq
curl -s http://localhost:8083/connectors/http-sink/tasks/ | jq
curl -s http://localhost:8083/connectors/http-sink/tasks/0/status | jq
```

### Send some messages

* Send some messages to the my-topic topic

```
./kafka-2.5.0/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic demo
```

### Pausing and resuming the connector

* Try to pause or resume the connector

```
curl -X PUT http://localhost:8083/connectors/http-sink/pause | jq
curl -X PUT http://localhost:8083/connectors/http-sink/resume | jq
```

### Restarting connector or task

* Try to restart a connector or its task

```
curl -X POST http://localhost:8083/connectors/http-sink/restart | jq
curl -X POST http://localhost:8083/connectors/http-sink/tasks/0/restart | jq
```

### Delete the connector

* Delete the connector

```
curl -X DELETE http://localhost:8083/connectors/http-sink | jq
```

## On OCP

### Prerequisites

Before we start, we should install the prerequisites which will be used later:
* Secret with AWS credentials (not part of this repository because they are secret ;-))
* Secret with Telegram credentials (not part of this repository because they are secret ;-))
* Strimzi cluster operator

```bash
oc apply -f 00-aws-credentials.yaml
oc apply -f 00-telegram-credentials.yaml
oc apply -f 01-operator
```

### Secrets with credentials

The AWS properties file is expected to look something like this:

```properties
aws.access-key=XXXX
aws.secret-key=xxxx
aws.region=US_EAST_1
```

The properties file for Telegram should look like this:

```properties
token=123:xxx
```

The resulting Kubernetes secrets should look like this:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: aws-credentials
type: Opaque
data:
  aws-credentials.properties: >-
    <Base64 of the properties file>
```

and 

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: telegram-credentials
type: Opaque
data:
  telegram-credentials.properties: <Base64 of the properties file>
```

### Kafka cluster

* Deploy Kafka 2.5.0 cluster

```bash
oc apply -f 02-kafka.yaml
```

## Deploy Kafka Connect

* Deploy Kafka Connect cluster.
Notice the custom image it uses which has the Camel Kafka connectors.

```bash
oc apply -f 03-kafka-connect.yaml
```

* Also notice how thes ecrets with the AWS and Telegram credentials mounted into the Connect cluster:

```yaml
  externalConfiguration:
    volumes:
      - name: telegram-credentials
        secret:
          secretName: telegram-credentials
      - name: aws-credentials
        secret:
          secretName: aws-credentials
```

* And how the configuration provider is configured:

```yaml
  config:
    config.providers: file
    config.providers.file.class: org.apache.kafka.common.config.provider.FileConfigProvider
```

* The Kafka Connect YAML also creates topics and users used by the Kafka Connect.
One the Kafka Connect is deployed, you can check the available connectors in the `KafkaConnect` custom resource status.
You should see the Camel connectors.

## Deploy the Telegram connector

* The Telegram source connector can be deployed using the `KafkaConnector` custom resource:

```bash
oc apply -f 04-telegram-connector.yaml
```

* In the YAML, notice how the API token is mounted from a secret.
* Once the connector is deployed, you can check the status to see if it is running, you can go to the Telegram app and talk with the bot `@Jakubot`.
* Run receiver on a Kafka topic `telegram-topic` to see the messages sent to the bot.

```bash
kubectl run kafka-consumer -ti --image=strimzi/kafka:0.18.0-kafka-2.5.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic telegram-topic --from-beginning
```

or use

```bash
./get-telegram-messages.sh
```

## Deploy the Amazon AWS SQS connector

* Once you have the messages in a Kafka topic, you can process them.
Or you can use a sink connector to send them somewhere else.
In our case, we will take the messages from Telegram and send them to Amazon AWS SQS queue using the Camel Kafka sink connector.
* You can create the connector again using YAML:

```bash
oc apply -f 05-sqs-connector.yaml
```

* Once the connector is running, you can send some more Telegram messages to `@Jakubot`.
You can see them again received in Kafka, but also forwarded to the AWS SQS queue.

## Converting the data formats

* The key / value convertor is important to correctly convert the data which are received or sent.
We can demonstrate it on an AWS S3 example.
* First we will deploy a source connector which will read files from Amazon AWS S3 storage.
We will use StringConverters to get the text files we are going to upload as Strings.

```bash
oc apply -f 06-s3-connector-wrong-converter.yaml
```

* Next upload a text file into S3 bucket and check how it was sent by the S3 connector.

```bash
kubectl run kafka-consumer -ti --image=strimzi/kafka:0.18.0-kafka-2.5.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic s3-topic --from-beginning
```

or use

```bash
./get-s3-messages.sh
```

* We can see that it is just the Java Object.
* Now we can reconfigure the connector to use the `S3ObjectConverter`.

```bash
oc apply -f 07-s3-connector-correct-converter.yaml
```

* Now we can upload another file to S3 and check the message in Kafka which should not contain the actual test.

```bash
kubectl run kafka-consumer -ti --image=strimzi/kafka:0.16.0-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic s3-topic --from-beginning
```

or use

```bash
./get-s3-messages.sh
```

