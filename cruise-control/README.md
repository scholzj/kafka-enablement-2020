# Cruise Control

## Cluster operator

* Install the Strimzi or AMQ Streams. 
You can use the Operator Hub or the attached YAML files.

```
oc apply -f 01-operator/
```

## Kafka cluster

* Deploy the Kafka cluster with just 1 Kafka node

```
oc apply -f 02-kafka.yaml
```

* Have a look at the `cruiseControl` section in the Kafka custom resource.
Notice also the Cruise Control deployment created by the operator.

## Deploy Kafka application

* Install an application to generate traffic

```
oc apply -f 03-application
```

* Let the application run for some time to generate some load.

## Scale the cluster

* Scale the Kafka broker to 3 nodes
  * Edit the Kafka CR with `kubectl edit kafka my-cluster` and set `.spec.kafka.replicas` to 3
  * Wait until the new nodes are started and ready
* Check the existing topics and notice how they are all on node 0, since they were created when we had only 1 broker

```
kubectl exec -ti my-cluster-kafka-0 -c kafka -- bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
```

## Deploy Cruise Control

* Edit the Kafka custom resource and enable Cruise Control
* You can use `oc edit kafka my-cluster` and add following section to the custom resource:

```yaml
  cruiseControl:
    brokerCapacity:
      disk: 100Gi
      cpuUtilization: 100
      inboundNetwork: 10000KB/s
      outboundNetwork: 10000KB/s
```

* Or you can just use the `04-kafka.yaml` file.

```
oc apply -f 04-kafka.yaml
```

## Rebalance the cluster

* Let's get a rebalance proposal from Cruise Control. Check the `KafkaRebalance` resource in [05-rebalance.yaml](./05-rebalance.yaml) before applying it and look at the goals.
  
```
kubectl apply -f 04-rebalance.yaml
```

* Wait until the proposal status is `ProposalReady`
* Check the status of the `KafkaProposal` resource
  * Notice the information about the replicas to be moved etc.
* Approve the rebalance

```
kubectl annotate kafkarebalance my-rebalance strimzi.io/rebalance=approve
```

* Wait until the proposal status is `Ready` which means that the rebalancing is finished
* Check the new topic distribution after the rebalance

```
kubectl exec -ti my-cluster-kafka-0 -c kafka -- bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
```
