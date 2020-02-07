package cz.scholz.rhdevelopermeetupbrno.portfolioviewer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortfolioViewer extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(PortfolioViewer.class.getName());

    private final Map<String, Float> portfolio = new HashMap<>();

    private final PortfolioViewerConfig verticleConfig;
    private KafkaConsumer<String, Float> consumer;

    public PortfolioViewer(PortfolioViewerConfig verticleConfig) {
        log.info("Creating PortfolioViewer");
        this.verticleConfig = verticleConfig;
    }

    /*
    Start the verticle
     */
    @Override
    public void start(Future<Void> start) {
        log.info("Starting PortfolioViewer");

        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", verticleConfig.getBootstrapServers());
        config.put("acks", verticleConfig.getAcks());
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeerializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.FloatDeserializer");
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

        consumer = KafkaConsumer.create(vertx, config, String.class, Float.class);
        consumer.handler(res -> {
            log.info("Adding {} to portfolio list", res.key(), res.value());
            portfolio.put(res.key(), res.value());
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
    }

    private void setupWebServer()   {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/portfolioviewer/*").handler(BodyHandler.create());

        router.get("/portfolioviewer").handler(req -> {
            log.info("Received GET /portfolioviewer request");

            String output = new JsonArray(portfolio.entrySet().stream().map(entry -> new JsonObject().put("code", entry.getKey()).put("value", String.format("%.2f", entry.getValue()))).collect(Collectors.toList())).encodePrettily();

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
        log.info("Stopping the PortfolioViewer.");
        consumer.close(res -> {
            stopFuture.complete();
        });
    }
}
