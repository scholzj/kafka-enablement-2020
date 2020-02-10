# Mirroring

This demo is using Mirror Maker between two Kafka cluster on the same OCP cluster, but in different namespace.

## Namespaces

Create two namespaces / projects `myproject` and `myproject2` to start with:

```
oc new-project myproject
oc new-project myproject2
```

And keep `myproject` as default - that is where the operator will run:

```
oc project myproject
```

## Cluster operator

We will use the Cluster operator to manage multiple namespaces.
You can check how we changed its configuration in `01-operator/050-Deployment-stirmzi-cluster-operator`:

```yaml
        - name: STRIMZI_NAMESPACE
          value: myproject,myproject2
```

Install the operator first:

```
oc apply -f 01-operator/
```

And create the additional role bindings for the second namespace:

```
oc apply -f 01-operator/020-RoleBinding-strimzi-cluster-operator.yaml -n myproject2
oc apply -f 01-operator/031-RoleBinding-strimzi-cluster-operator-entity-operator-delegation.yaml -n myproject2
oc apply -f 01-operator/032-RoleBinding-strimzi-cluster-operator-topic-operator-delegation.yaml -n myproject2
```

## Kafka clusters

Deploy the Europe and US Kafka clusters.
Europe cluster will run in `myproject` and US cluster in `myproject2`.

```
oc apply -f 02-kafka-europe.yaml -n myproject
oc apply -f 03-kafka-us.yaml -n myproject2
```

## Mirror Maker 1

### Application

Each cluster will have its own applications sending and receiving messages.
Notice how they create the topics for both regions and how they produce only into their own.

```
oc apply -f 04-application-europe.yaml -n myproject
oc apply -f 05-application-us.yaml -n myproject2
```

### Mirroring

Next, we can deploy Mirror Maker and see how it mirrors the messages.
We deploy it in the way that it produces locally and consumes remotely to minimize duplicates.

```
oc apply -f 06-mirror-maker-1-europe.yaml -n myproject
oc apply -f 07-mirror-maker-1-us.yaml -n myproject2
```

Wait until it starts mirroring and notice that it is using the same topic as was on the original cluster.

## Mirror Maker 2

Before you start, delete the Mirror Maker 1 deployment:

```
oc delete kmm my-mirror-maker-1 -n myproject
oc delete kmm my-mirror-maker-1 -n myproject2
```

### Application

Each cluster will have its own applications sending and receiving messages.
Notice how they both use just the topic `my-topic` without any prefixes.

```
oc apply -f 08-application-europe.yaml -n myproject
oc apply -f 09-application-us.yaml -n myproject2
```

### Mirroring

Next, we can deploy Mirror Maker and see how it mirrors the messages.
We deploy it in the way that it produces locally and consumes remotely to minimize duplicates.

```
oc apply -f 10-mirror-maker-2-europe.yaml -n myproject
oc apply -f 11-mirror-maker-2-us.yaml -n myproject2
```

Wait until it starts mirroring and notice that it is automatically changing the topic names.

## Challenges

Secret management for authentication, encryption etc.