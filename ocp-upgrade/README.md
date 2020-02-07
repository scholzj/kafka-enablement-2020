# Kafka upgrades on OCP

## Requirements

This demo expects you to have an OCP cluster with OCP 3.11 or higher.
Normally it is executed against OCP 4 running on AWS.
It expects to be executed against the namespace `myproject`

```
oc new-project myproject
```

## Deploy the old operator from YAML files

First, deploy the old operator. 
In this case Strimzi 0.16.2.

```
oc apply -f 01-old-operator/
```

## Deploy Kafka 2.3.1

Next deploy Kafka 2.3.1 using the old operator version.

```
oc apply -f 02-kafka-2.3.0.yaml
```

## Upgrade the operator

Upgrade the operator by installing the new operator:

```
oc apply -f 03-new-operator/
```

Rolling update will occur.
This is needed to make the pods use the same container images as the operator does.
This does not change Kafka version.

## Upgrade Kafka

Edit the Kafka CR and change the version field to `2.4.0.
Watch the rolling updates happen multiple times.

After the upgrade is finished, update the clients and the `log.message.format.version` option.
