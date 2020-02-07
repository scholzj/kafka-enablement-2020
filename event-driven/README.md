# Event Driven Architecture

This demo contains simple financial application based on events and stream processing.
It uses Kafka and runs in OCP using Strimzi / AMQ Streams.

## Setup

### Build

Use the `make` command to build the project sources from `00-components/`.
`make all` should build it and push it to the container registry.
Environment variables `DOCKER_REGISTRY`, `DOCKER_ORG`, `DOCKER_REPOSITORY`, and `DOCKER_TAG` can be used to override the registry used for the build, the organization which will be used and the tag which will be used.

```
make all
```

You can also use `make docuker_build` to only build everything but not push it anywhere.

## AMQ Streams

This demo expects to be run in the namespace `myproject`.

```
oc new-project myproject
```

* Deploy cluster operator

```
oc apply -f 01-operator/
```

* Deploy Kafka and Kafka Connect cluster

```
oc apply -f 02-kafka/
```

## Applications

* Deploy the applications using:

```
oc apply -f 03-applications/
```