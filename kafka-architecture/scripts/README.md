# Environment

## Prerequsites

Following software needs to be installed on your computer to use this environment:

* Java 8
* OpenSSL
* CFSSL

OpenSSL and CFSSL is needed only if you want to generate your own keys.

## SSL keys

SSL keys used by the installation can be found in `ssl/keys`. To generate new set of keys, run the `generate.sh` script. To delete the existing keys, run the `clean.sh` script. Both scripts can be found in `ssl`.

## Zookeeper

First you need to start Zookeeper. To start Zookeeper nodes, use the helper scripts `zookeeper-0.sh`, `zookeeper-1.sh` and `zookeeper-2.sh`. Zookeeper is using the Kafka binaries. It is secured using SASL authentication. Zookeeper data directory is stored in `tmp`. For more details on configuration and starting Zookeeper, have a look at the startup scripts or at the config files in `configs/zookeeper`.

## Kafka

Once Zookeeper is running you can start Kafka. Kafka can be staretd using the helper scritps `kafka-0.sh`, `kafka-1.sh` and `kafka-2.sh`. For more details about how to start Kafka and how to configure it see the startup scripts and the configuration files in `configs/kafka`. This Kafka installation has 4 interfaces: Plaintext one (INSECURE), TLS (ENCRYPTED), SASL based (AUTHENTICATED) and a separate interface for replication.

## Kafka Connect

Kafka Connect can be started after Kafka brokers are running. It connects to Kafka using SSL (ENCRYPTED interface). It can be staretd using the helper scritps `connect-0.sh`, `connect-1.sh` and `connect-2.sh`. For more details about how to start Kafka Connect and how to configure it see the startup scripts and the configuration files in `configs/connect`.