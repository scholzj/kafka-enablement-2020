/*
 * Copyright 2018, Jakub Scholz
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package cz.scholz.kafka.demo.shares.pricefeedconnector;

import cz.scholz.kafka.demo.shares.pricefeedconnector.Price;
import cz.scholz.kafka.demo.shares.pricefeedconnector.PriceFeedConnector;
import cz.scholz.kafka.demo.shares.pricefeedconnector.ShareMasterData;
import io.vertx.core.Vertx;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PriceFeedTask extends SourceTask {
    private static final Logger log = LoggerFactory.getLogger(PriceFeedTask.class);

    private final Random random = new Random();
    private Vertx vertx;
    private String topic;
    private Queue<Price> priceQueue = new ConcurrentLinkedQueue();
    private List<ShareMasterData> masterData;


    @Override
    public String version() {
        return new PriceFeedConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting connector task {}");

        vertx = Vertx.vertx();
        topic = props.get(PriceFeedConnector.TOPIC_CONFIG);
        masterData = PriceFeedConnector.masterData;

        vertx.setPeriodic(1000, res -> {
            generatePricingInfo();
        });
    }

    private void generatePricingInfo() {
        masterData.stream().forEach(share -> {
            float change = ((100f - random.nextInt(200)) / 100f) * (share.getMaxChange() / 100f);
            float newPrice = Math.round(share.getPrice() * (1f + change) * 100f) / 100f;
            share.setPrice(newPrice);
            priceQueue.add(new Price(share.getCode(), newPrice));
        });
    }

    @Override
    public List<SourceRecord> poll() {
        List<SourceRecord> records = new ArrayList<>();

        Price price = priceQueue.poll();
        while (price != null)   {
            Map<String, Object> sourcePartition = Collections.singletonMap("filename", "pricefeed");
            Map<String, Object> sourceOffset = Collections.singletonMap("position", price.getShare());

            SourceRecord record = new SourceRecord(sourcePartition, sourceOffset, topic, Schema.STRING_SCHEMA, price.getShare(), Schema.FLOAT64_SCHEMA, price.getPrice());
            records.add(record);
        }

        return records;
    }

    @Override
    public void stop() {
        vertx.close();
    }
}
