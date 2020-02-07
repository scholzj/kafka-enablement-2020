FROM registry.access.redhat.com/amq7/amq-streams-kafka-connect:1.1.0-kafka-2.1.1

USER root:root
COPY target/price-feed-connector.jar /opt/kafka/plugins/
USER 185:0