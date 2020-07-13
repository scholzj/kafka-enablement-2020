# Kafka upgrades on OCP

## Requirements

This lab expects you to have an OCP cluster with OCP 3.11 or higher.
Normally it is executed against OCP 4 running on AWS.
It expects to be executed against the namespace `myproject`

```
oc new-project myproject
```

## Deploy the old operator from YAML files

* First, deploy the old operator. 
In this case Strimzi 0.17.0 (=> AMQ Streams 1.4).

```
oc apply -f 01-old-operator/
```

## Deploy Kafka 2.4.0

* Next deploy Kafka 2.4.0 using the old operator version.

```
oc apply -f 02-kafka-2.4.0.yaml
```

## Upgrade the operator

* Upgrade the operator by installing the new operator Strimzi 0.18.0 (=> AMQ Streams 1.5):

```
oc apply -f 03-new-operator/
```

* Rolling update will occur (carefull, Zookeeper will roll twice because of the TLS sidecar removal)
This is needed to make the pods use the same container images as the operator does.
This does not change Kafka version.

## Upgrade Kafka

* Edit the Kafka CR and change the version field to `2.5.0.
* Watch the rolling updates happen multiple times.
* After the upgrade is finished, you can upgrade the clients and afterwards set the `log.message.format.version` option to `2.5`.
