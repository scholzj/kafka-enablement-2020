package cz.scholz.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.connect.mirror.RemoteClusterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class Consumer {
    private static int timeout = 60000;
    private static Logger LOG = LoggerFactory.getLogger(Consumer.class);

    public static void main(String[] args) throws InterruptedException, TimeoutException {
        /*
         * Configure the logger
         */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        /*
         * Consumer configuration
         */
        Map<String, Object> props = new HashMap();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "europe-kafka-bootstrap-kafka-europe.apps.myocp.com:443");
        //props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "us-kafka-bootstrap-kafka-us.apps.myocp.com:443");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-local-group");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "../cluster-europe.p12");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "JRpgii8AWmDh");
        //props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "../cluster-us.p12");
        //props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "kgNwE8fbXSF2");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS"); // Hostname verification

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        ConsumerRebalanceListener offsetHandler = new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets = getMirroredOffsets();

                for (TopicPartition partition : partitions) {
                    OffsetAndMetadata mirroredOffset = findHighestMirroredOffset(mirroredOffsets, partition);
                    OffsetAndMetadata localOffset = consumer.committed(Collections.singleton(partition)).get(partition);

                    if (mirroredOffset != null) {
                        if (localOffset != null)    {
                            if (mirroredOffset.offset() > localOffset.offset()) {
                                LOG.warn("Seeking to {} in {} (higher than local offset {})", mirroredOffset.offset(), partition, localOffset.offset());
                                consumer.seek(partition, mirroredOffset);
                            } else {
                                LOG.warn("Keeping local offset {} in {} (higher than mirrored offset {})", localOffset.offset(), partition, mirroredOffset.offset());
                            }
                        } else {
                            LOG.warn("Seeking to {} in {} (local offset does not exist)", mirroredOffset.offset(), partition);
                            consumer.seek(partition, mirroredOffset);
                        }
                    } else {
                        LOG.warn("Mirrored offset does not exist for partition {}", partition);
                    }
                }
            }

            public OffsetAndMetadata findHighestMirroredOffset(List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets, TopicPartition partition) {
                OffsetAndMetadata foundOffset = null;

                for (Map<TopicPartition, OffsetAndMetadata> offsets : mirroredOffsets)  {
                    if (offsets.containsKey(partition)) {
                        if (foundOffset == null)    {
                            foundOffset = offsets.get(partition);
                        } else  {
                            OffsetAndMetadata newOffset = offsets.get(partition);
                            if (foundOffset.offset() < newOffset.offset())   {
                                foundOffset = newOffset;
                            }
                        }
                    }
                }

                return foundOffset;
            }

            public List<Map<TopicPartition, OffsetAndMetadata>> getMirroredOffsets() {
                Set<String> clusters = null;
                try {
                    clusters = RemoteClusterUtils.upstreamClusters(props);
                } catch (InterruptedException e) {
                    LOG.error("Failed to get remote cluster", e);
                    return Collections.emptyList();
                } catch (TimeoutException e) {
                    LOG.error("Failed to get remote cluster", e);
                    return Collections.emptyList();
                }

                List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets = new ArrayList<>();

                for (String cluster : clusters) {
                    try {
                        mirroredOffsets.add(RemoteClusterUtils.translateOffsets(props, cluster, props.get(ConsumerConfig.GROUP_ID_CONFIG).toString(), Duration.ofMinutes(1)));
                    } catch (InterruptedException e) {
                        LOG.error("Failed to translate offsets", e);
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        LOG.error("Failed to translate offsets", e);
                    }
                }

                return mirroredOffsets;
            }
        };

        /*
         * Consume messages
         */
        consumer.subscribe(Pattern.compile(".*my-mm2-topic"), offsetHandler);

        int messageNo = 0;

        while (true)
        {
            ConsumerRecords<String, String> records = consumer.poll(timeout);

            if(records.isEmpty()) {
                LOG.info("No message in topic for {} seconds. Finishing ...", timeout/1000);
                break;
            }

            for (ConsumerRecord<String, String> record : records)
            {
                LOG.info("Received message no. {}: {} / {} (from topic {}, partition {}, offset {})", ++messageNo, record.key(), record.value(), record.topic(), record.partition(), record.offset());
            }

            consumer.commitSync();
        }

        consumer.close();

    }
}
