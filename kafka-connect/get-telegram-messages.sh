#!/bin/bash
set -v

kubectl run kafka-consumer -ti --image=strimzi/kafka:0.16.0-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic telegram-topic --from-beginning