FROM strimzi/kafka:0.18.0-kafka-2.5.0

USER root:root
COPY target/price-feed-connector.jar /opt/kafka/plugins/
USER 1001