package cz.scholz.kafka.demo.shares.positionmanager;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.vertx.kafka.client.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionManager extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(PositionManager.class.getName());

    private final List<JsonObject> transactions = new ArrayList<>();

    private final PositionManagerConfig verticleConfig;
    private KafkaProducer<String, Integer> producer;
    private KafkaConsumer<String, Integer> consumer;

    public PositionManager(PositionManagerConfig verticleConfig) {
        log.info("Creating PositionManager");
        this.verticleConfig = verticleConfig;
    }

    /*
    Start the verticle
     */
    @Override
    public void start(Future<Void> start) {
        log.info("Starting PositionManager");

        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", verticleConfig.getBootstrapServers());
        config.put("acks", verticleConfig.getAcks());
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeerializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        config.put("enable.auto.commit", "false");
        config.put("auto.offset.reset", "earliest");
        config.put("group.id", verticleConfig.getGroupId());

        if (verticleConfig.getTrustStorePassword() != null && verticleConfig.getTrustStorePath() != null)   {
            log.info("Configuring truststore");
            config.put("security.protocol", "SSL");
            config.put("ssl.truststore.type", "PKCS12");
            config.put("ssl.truststore.password", verticleConfig.getTrustStorePassword());
            config.put("ssl.truststore.location", verticleConfig.getTrustStorePath());
        }

        if (verticleConfig.getKeyStorePassword() != null && verticleConfig.getKeyStorePath() != null)   {
            log.info("Configuring keystore");
            config.put("security.protocol", "SSL");
            config.put("ssl.keystore.type", "PKCS12");
            config.put("ssl.keystore.password", verticleConfig.getKeyStorePassword());
            config.put("ssl.keystore.location", verticleConfig.getKeyStorePath());
        }

        if (verticleConfig.getUsername() != null && verticleConfig.getPassword() != null)   {
            config.put("sasl.mechanism","SCRAM-SHA-512");
            config.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + verticleConfig.getUsername() + "\" password=\"" + verticleConfig.getPassword() + "\";");

            if (config.get("security.protocol") != null && config.get("security.protocol").equals("SSL"))  {
                config.put("security.protocol","SASL_SSL");
            } else {
                config.put("security.protocol","SASL_PLAINTEXT");
            }
        }

        producer = KafkaProducer.create(vertx, config, String.class, Integer.class);
        producer.exceptionHandler(res -> {
            log.error("Received producer exception", res);
        });

        consumer = KafkaConsumer.create(vertx, config, String.class, Integer.class);
        consumer.handler(res -> {
            log.info("Adding {} to transaction list", res.key(), res.value());

            JsonObject transaction = new JsonObject().put("code", res.key()).put("amount", res.value());
            transactions.add(transaction);
        });
        consumer.exceptionHandler(res -> {
            log.error("Received consumer exception", res);
        });
        consumer.subscribe(verticleConfig.getTopic(), res -> {
            if (res.succeeded()) {
                log.info("Subscribed to topic {}", verticleConfig.getTopic());
                start.complete();
            }
            else {
                log.error("Failed to subscribe to topic {}", verticleConfig.getTopic());
                start.fail("Failed to subscribe to topic " + verticleConfig.getTopic());
            }
        });

        setupWebServer();

        //start.complete();
    }

    private Future<RecordMetadata> sendMessage(String key, Integer amount) {
        Future<RecordMetadata> future = Future.future();

        KafkaProducerRecord<String, Integer> record = KafkaProducerRecord.create(verticleConfig.getTopic(), key, amount);
        producer.send(record, future.completer());

        return future;
    }

    private void setupWebServer()   {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/positionmanager/*").handler(BodyHandler.create());
        router.post("/positionmanager").handler(req -> {
            log.info("Received POST /positionmanager request: {}", req.getBodyAsString());

            JsonObject request = req.getBodyAsJson();
            String shareCode = request.getString("code", null);
            Object tempAmount = request.getValue("amount");
            Integer amount;

            if (tempAmount instanceof Integer)  {
                amount = (Integer) tempAmount;
            } else if (tempAmount instanceof String)   {
                amount = Integer.parseInt((String) tempAmount);
            } else {
                throw new RuntimeException("Failed to convert amount: " + tempAmount);
            }

            sendMessage(shareCode, amount).setHandler(res -> {
                if (res.succeeded())    {
                    log.info("Position in {} with amount of {} send to Kafka", shareCode, amount);

                    req.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(request.encodePrettily());
                } else {
                    log.error("Failed to send position to Kafka", res.cause());

                    req.response()
                            .setStatusCode(500)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(new JsonObject().put("error", "Failed to send position: " + res.cause().getMessage()).encodePrettily());
                }
            });
        });

        router.get("/positionmanager").handler(req -> {
            log.info("Received GET /positionmanager request");

            String output = new JsonArray(transactions).encodePrettily();

            HttpServerResponse response = req.response();
            response.putHeader("content-type", "application/json");
            response.setStatusCode(200);
            response.end(output);
        });

        router.route("/*").handler(StaticHandler.create());
        server.requestHandler(router).listen(8080);
    }

    /*
    Stop the verticle
     */
    @Override
    public void stop(Future<Void> stopFuture) {
        log.info("Stopping the PositionManager.");
        producer.close(res -> {
            stopFuture.complete();
        });
    }
}
