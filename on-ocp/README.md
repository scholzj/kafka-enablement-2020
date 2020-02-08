# Kafka on OCP

## Requirements

This demo expects you to have an OCP cluster with OCP 3.11 or higher.
Normally it is executed against OCP 4 running on AWS.
It expects to be executed against the namespace `myproject`

```
oc new-project myproject
```

## Deploy the operator from YAML files

In case you are running in different namespace, you will have to change the namespace of the service account in all `ClusterRoleBinding` and `RoleBinding` resources.
You can do that on Linux using:

```
sed -i 's/namespace: .*/namespace: my-namespace/' 01-operator/*RoleBinding*.yaml
```

Or on MacOS using:

```
sed -i '' 's/namespace: .*/namespace: my-namespace/' 01-operator/*RoleBinding*.yaml
```

## Deploy first Kafka cluster

Go through the file `02-kafka.yaml` and look at the different parts.
Notice for example how it configures Kafka and ZooKeeper, how it configures storage, or how it configures the User and Topic operators.

```
oc apply -f 02-kafka.yaml
```

After the cluster is deployed, check the status in the custom resources.

```
oc edit kafka my-cluster
```

## Updating cluster

You can try to update or scale the cluster.
Edit the Kafka resource and add following options `auto.create.topics.enable: "false"` to `spec.kafka.config`.

```
oc edit kafka my-cluster
```

## Using the Topic operator

Check the `03-topic.yaml` file.
Create topic using the KafkaTopic resource:

```
oc apply -f 03-topic.yaml
```

We can check that the topic exists:

```
oc run kafka-topics -ti --image=strimzi/kafka:0.16.2-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-topics.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --describe
```

We can also try to create the topic in Kafka:

```
oc run kafka-topics -ti --image=strimzi/kafka:0.16.2-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-topics.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --create --topic my-topic2 --partitions 12 --replication-factor 3
```

And check the created custom resource:

```
oc get kt my-topic2 -o yaml
```

## Using the User operator

TODO: Qoutes

Check the user resource in `04-user.yaml`.
Use it to create user:

```
oc apply -f 04-user.yaml
```

And check the secrets it created with the TLs certificate:

```
oc get secret my-user -o yaml
```

As well as the ACL rights

```
oc run kafka-acls -ti --image=strimzi/kafka:0.16.2-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-acls.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --list
```

## Deploying application

Check the file `06-application.yaml` for an example how to combine all these things.
Deploy the application with the full authentication, authorization and topic management and check how it sends and receives messages:

```
oc apply -f 06-application.yaml
```

## Expose Kafka from the outside

Edit the Kafka resource:

```
oc edit kafka my-cluster
```

And add external listener using OpenShift Routes:

```yaml
listeners:
  # ...
  external:
    type: route
    authentication:
      type: tls
```

Watch the rolling update and check how the services and routes were created.
Get the cluster address from the status of the Kafka resource.

Create the external user:

```
oc apply -f 07-external-user.yaml
```

Get the broker public key and user key:

```
oc extract secret/my-cluster-cluster-ca-cert --keys=ca.p12 --to=- > cluster.p12
oc extract secret/external-user --keys=user.p12 --to=- > user.p12
```

And get their passwords as well:

```
oc extract secret/my-cluster-cluster-ca-cert --keys=ca.password --to=-
oc extract secret/external-user --keys=user.password --to=-
```

Use the external client from directory`08-external-client` to connect to the cluster and consume messages.

_If time, do the same with load balancers._