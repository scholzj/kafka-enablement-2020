# HTTP Bridge

The video of this lab can be found here: https://youtu.be/wjMlihYpW5A

## Setup

### Kafka cluster

* Deploy the cluster operator and Kafka cluster:

```sh
oc apply -f 01-operator/
oc apply -f 02-kafka.yaml
```

### Topics

* Create two topics `my-topic` and `my-topic2`:

```sh
oc apply -f 03-topics.yaml
```

## HTTP Bridge

* Check the `04-http-bridge.yaml` file.
Look at how it is configured, how it uses the Kafka user and the secrets for encryption and authentication.
* Deploy it:

```sh
oc apply -f 04-http-bridge.yaml
```

## Using the bridge

* To make the commands easier, store the route address of the bridge into an environment variable:

```sh
export BRIDGE="$(oc get routes my-bridge -o jsonpath='{.status.ingress[0].host}')"
```

### Sending messages

* Send messages using a simple `POST` call.
Notice the `content-type` header which is important!

```sh
curl -X POST $BRIDGE/topics/my-topic \
  -H 'content-type: application/vnd.kafka.json.v2+json' \
  -d '{"records":[{"key":"message-key","value":"message-value"}]}' \
  | jq
```

### Receiving messages

* First, we need to create a consumer:

```sh
curl -X POST $BRIDGE/consumers/my-group \
  -H 'content-type: application/vnd.kafka.v2+json' \
  -d '{
    "name": "consumer1",
    "format": "json",
    "auto.offset.reset": "earliest",
    "enable.auto.commit": false,
    "fetch.min.bytes": 512,
    "consumer.request.timeout.ms": 30000
  }' \
  | jq
```

* Then we need to subscribe the consumer to the topics it should receive from:

```sh
curl -X POST $BRIDGE/consumers/my-group/instances/consumer1/subscription \
  -H 'content-type: application/vnd.kafka.v2+json' \
  -d '{"topics": ["my-topic"]}' \
  | jq
```

* And then we can consume the messages (can be called repeatedly - e.g. in a loop):

```sh
curl -X GET $BRIDGE/consumers/my-group/instances/consumer1/records \
  -H 'accept: application/vnd.kafka.json.v2+json' \
  | jq
```

* Now after we process the messages, we can commit them:

```sh
curl -X POST $BRIDGE/consumers/my-group/instances/consumer1/offsets
```

* At the end we should close the consumer

```sh
curl -X DELETE $BRIDGE/consumers/my-group/instances/consumer1
```

### Sending messages to wrong topic

* Try to send the messages to `my-topic2` to see that the ACLs will not allow it.

```sh
curl -X POST $BRIDGE/topics/my-topic2 \
  -H 'content-type: application/vnd.kafka.json.v2+json' \
  -d '{"records":[{"key":"message-key","value":"message-value"}]}' \
  | jq
```