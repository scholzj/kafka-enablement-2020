package cz.scholz.kafka.demo.shares.positiongenerator;

import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String args[]) {
        List<ShareMasterData> masterData = loadMasterData();

        try {
            Vertx vertx = Vertx.vertx();
            vertx.deployVerticle(new PositionGenerator(PositionGeneratorConfig.fromEnv(), masterData), res -> {
                if (res.failed()) {
                    log.error("Failed to start the verticle", res.cause());
                    System.exit(1);
                }
            });
        } catch (IllegalArgumentException e) {
            log.error("Unable to parse arguments", e);
            System.exit(1);
        } catch (Exception e) {
            log.error("Error starting PositionGenerator", e);
            System.exit(1);
        }
    }

    private static List<ShareMasterData> loadMasterData()  {
        List<ShareMasterData> masterData = new ArrayList<>(10);

        masterData.add(new ShareMasterData("RHT", 183.25f, 2));
        masterData.add(new ShareMasterData("AMZN", 1837.28f, 2));
        masterData.add(new ShareMasterData("VMW", 185.79f, 2));
        masterData.add(new ShareMasterData("TWTR", 34.72f, 2));
        masterData.add(new ShareMasterData("GOOG", 1207.15f, 2));
        masterData.add(new ShareMasterData("IBM", 143.28f, 2));
        masterData.add(new ShareMasterData("TSLA", 274.96f, 2));
        masterData.add(new ShareMasterData("MSFT", 119.89f, 2));
        masterData.add(new ShareMasterData("ORCL", 53.93f, 2));
        masterData.add(new ShareMasterData("FB", 175.72f, 2));

        return masterData;
    }
}
