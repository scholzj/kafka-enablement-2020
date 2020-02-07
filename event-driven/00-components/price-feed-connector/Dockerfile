FROM strimzi/kafka:0.17.0-rc1-kafka-2.4.0

USER root:root
COPY target/price-feed-connector.jar /opt/kafka/plugins/
USER 1001