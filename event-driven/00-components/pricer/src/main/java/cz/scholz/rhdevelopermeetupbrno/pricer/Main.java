package cz.scholz.rhdevelopermeetupbrno.pricer;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

public class Main {
    public static void main(final String[] args) {
        PricerConfig config = PricerConfig.fromEnv();
        Properties kafkaStreamsConfig = PricerConfig.getKafkaStreamsProperties(config);

        StreamsBuilder builder = new StreamsBuilder();
        KTable<String, Integer> positions = builder
                .stream(config.getTradesTopic(), Consumed.with(Serdes.String(), Serdes.Integer()))
                .groupByKey().aggregate(
                    () -> 0,
                    (aggKey, newValue, aggValue) -> aggValue + newValue,
                    Materialized.with(Serdes.String(), Serdes.Integer()));
        KTable<String, Float> prices = builder
                .table(config.getPricesTopic(), Consumed.with(Serdes.String(), Serdes.Float()));

        positions.leftJoin(prices, (leftValue, rightValue) -> calculatePrice(leftValue, rightValue))
                .toStream()
                .to(config.getPortfolioTopic(), Produced.with(Serdes.String(), Serdes.Float()));

        KafkaStreams streams = new KafkaStreams(builder.build(), kafkaStreamsConfig);
        streams.start();
    }

    private static Float calculatePrice(Integer amount, Float price)   {
        if (amount != null && price != null)    {
            return amount * price;
        } else {
            return 0f;
        }
    }
}
