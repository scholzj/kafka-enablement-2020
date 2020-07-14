# Mirroring

This lab is using Mirror Maker between two Kafka cluster on the same OCP cluster, but in different namespace.
A video recording of this demo can be found here: https://youtu.be/rda8yrd_-mE

## Namespaces

* Create two namespaces / projects `kafka-europe` and `kafka-us` to start with:

```sh
oc new-project kafka-us
oc new-project kafka-europe
```

* And keep `myproject` as default - that is where the operator will run:

```sh
oc project myproject
```

## Cluster operator

* On OCP 4, you can simply install Strimzi or AMQ Streams form the Operator Hub and configure it to watch all namespaces.

\- or -

* On OCP 3.11, you will have to do it manually. 
We will use the Cluster operator to manage multiple namespaces.
Change the watched namespaces in `01-operator/050-Deployment-stirmzi-cluster-operator`:

```yaml
        - name: STRIMZI_NAMESPACE
          value: kafka-europe,kafka-us
```

* Then install the operator:

```sh
oc apply -f 01-operator/
```

* And create the additional role bindings for the second namespace:

```sh
oc apply -f 01-operator/020-RoleBinding-strimzi-cluster-operator.yaml -n kafka-europe
oc apply -f 01-operator/031-RoleBinding-strimzi-cluster-operator-entity-operator-delegation.yaml -n kafka-europe
oc apply -f 01-operator/032-RoleBinding-strimzi-cluster-operator-topic-operator-delegation.yaml -n kafka-europe
oc apply -f 01-operator/020-RoleBinding-strimzi-cluster-operator.yaml -n kafka-us
oc apply -f 01-operator/031-RoleBinding-strimzi-cluster-operator-entity-operator-delegation.yaml -n kafka-us
oc apply -f 01-operator/032-RoleBinding-strimzi-cluster-operator-topic-operator-delegation.yaml -n kafka-us
```

## Kafka clusters

* Deploy the Europe and US Kafka clusters.
Europe cluster will run in `kafka-europe` and US cluster in `kafka-us`.

```sh
oc apply -f 02-kafka-europe.yaml -n kafka-europe
oc apply -f 03-kafka-us.yaml -n kafka-us
```

## Mirror Maker 1

### Application

* Each cluster will have its own applications sending and receiving messages.
Notice how they create the topics for both regions and how they produce only into their own.

```sh
oc apply -f 04-application-europe.yaml -n kafka-europe
oc apply -f 05-application-us.yaml -n kafka-us
```

### Mirroring

* Next, we can deploy Mirror Maker and see how it mirrors the messages.
We deploy it in the way that it produces locally and consumes remotely to minimize duplicates.

```sh
oc apply -f 06-mirror-maker-1-europe.yaml -n kafka-europe
oc apply -f 07-mirror-maker-1-us.yaml -n kafka-us
```

* Wait until it starts mirroring and notice that it is using the same topic as was on the original cluster.

## Mirror Maker 2

* Before you start, delete the Mirror Maker 1 deployment:

```sh
oc delete kmm my-mirror-maker-1 -n kafka-europe
oc delete kmm my-mirror-maker-1 -n kafka-us
```

### Application

* Each cluster will have its own applications sending and receiving messages.
Notice how they both use just the topic `my-topic` without any prefixes.

```sh
oc apply -f 08-application-europe.yaml -n kafka-europe
oc apply -f 09-application-us.yaml -n kafka-us
```

### Mirroring

* Next, we can deploy Mirror Maker and see how it mirrors the messages.
We deploy it in the way that it produces locally and consumes remotely to minimize duplicates.

```sh
oc apply -f 10-mirror-maker-2-europe.yaml -n kafka-europe
oc apply -f 11-mirror-maker-2-us.yaml -n kafka-us
```

* Wait until it starts mirroring and notice that it is automatically changing the topic names.

## Offset recovery

* Download the public key of the European cluster:

```sh
oc extract -n kafka-europe secret/europe-cluster-ca-cert --keys=ca.p12 --to=- > cluster-europe.p12
```

* And get the password for the store:

```sh
oc extract -n kafka-europe secret/europe-cluster-ca-cert --keys=ca.password --to=-
```

* Set these in the configuration of the application in `./offset-recovery`(./offset-recovery).
And run the application.
It should connect and start consuming the messages.
Stop the application and remember the last offsets and timestamps of the messages from the European and US cluster you got.

* Download the public key of the US cluster:

```sh
oc extract -n kafka-us secret/us-cluster-ca-cert --keys=ca.p12 --to=- > cluster-us.p12
```

* And get the password for the store:

```sh
oc extract -n kafka-us secret/us-cluster-ca-cert --keys=ca.password --to=-
```

## Challenges

* Secret management for authentication, encryption etc.