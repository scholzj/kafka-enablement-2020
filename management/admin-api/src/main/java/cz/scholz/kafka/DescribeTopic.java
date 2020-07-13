package cz.scholz.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DescribeTopic {
    private static Logger LOG = LoggerFactory.getLogger(DescribeTopic.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /*
         * Configure the logger
         */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        /*
         * Configure the consumer
         */
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        /*
         * Create the Admin API client
         */
        AdminClient admin = AdminClient.create(props);

        /*
         * Get the topic description
         */
        DescribeTopicsResult result = admin.describeTopics(Arrays.asList(new String[]{"reassignment-topic"}));
        Map<String, TopicDescription> topics = result.all().get();

        /*
         * Print the topic details
         */
        for (TopicDescription topic : topics.values())
        {
            LOG.info("Topic name {}", topic.name());
            LOG.info("    Internal: {}", topic.isInternal());
            LOG.info("    Partitions:");

            for (TopicPartitionInfo partition : topic.partitions())
            {
                LOG.info("     - number: {}", partition.partition());
                LOG.info("            leader: {}", partition.leader().id());
                LOG.info("            replicas: {}", partition.replicas().stream().map(node -> node.id()).collect(Collectors.toList()));
                LOG.info("            isr: {}", partition.isr().stream().map(node -> node.id()).collect(Collectors.toList()));
            }
        }

    }
}
