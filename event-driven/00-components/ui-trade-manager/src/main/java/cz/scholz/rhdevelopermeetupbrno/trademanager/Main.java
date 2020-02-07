package cz.scholz.rhdevelopermeetupbrno.trademanager;

import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String args[]) {
        try {
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new TradeManager(TradeManagerConfig.fromEnv()), res -> {
                if (res.failed()) {
                    log.error("Failed to start the verticle", res.cause());
                    System.exit(1);
                }
            });
        } catch (IllegalArgumentException e) {
            log.error("Unable to parse arguments", e);
            System.exit(1);
        } catch (Exception e) {
            log.error("Error starting TradeManager", e);
            System.exit(1);
        }
    }
}
