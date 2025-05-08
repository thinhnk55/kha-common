package com.defi.common.vertx;

import com.defi.common.util.log.DebugLogger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.*;

/**
 * Singleton wrapper around the Vert.x server.
 * <p>
 * This class initializes and starts the Vert.x environment using configurable deployment options.
 * It provides a central access point for managing the Vert.x lifecycle within the application.
 */
public class VertxServer {

    /**
     * Singleton instance of the VertxServer.
     */
    private static VertxServer instance;

    /**
     * The underlying Vert.x instance.
     */
    public Vertx vertx;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the Vert.x server with default configurations.
     */
    private VertxServer() {
        // Private constructor
    }

    /**
     * Retrieves the singleton instance of {@code VertxServer}.
     * If not already created, a new instance is initialized.
     *
     * @return the singleton instance of VertxServer
     */
    public static VertxServer getInstance() {
        if (instance == null) {
            instance = new VertxServer();
        }
        return instance;
    }

    /**
     * Starts the Vert.x application by deploying the specified verticle class.
     * <p>
     * This method:
     * <ul>
     *   <li>Initializes configuration via {@code VertxConfig}</li>
     *   <li>Creates a Vert.x instance with custom options</li>
     *   <li>Deploys the verticle using worker threading model</li>
     * </ul>
     *
     * @param config        the application configuration as a JSON node
     * @param verticleClass the verticle class to deploy
     * @return a {@code Future<String>} that completes with the deployment ID or fails with an error
     */
    public Future<String> start(ObjectNode config, Class<?> verticleClass) {
        VertxConfig.instance().init(config);

        // Configure Vert.x options (e.g., blocked thread detection)
        VertxOptions vxOptions = new VertxOptions()
                .setBlockedThreadCheckInterval(30000); // 30 seconds

        // Initialize Vert.x instance
        vertx = Vertx.vertx(vxOptions);

        // Determine optimal thread pool and instance size based on CPU cores
        int procs = Runtime.getRuntime().availableProcessors();
        DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER)
                .setWorkerPoolSize(procs * 2)
                .setInstances(procs * 2);

        // Deploy the verticle and log the result
        return vertx.deployVerticle(verticleClass.getName(), deploymentOptions)
                .onSuccess(id -> DebugLogger.logger.info("Your Vert.x application is started! ID = {}", id))
                .onFailure(err -> DebugLogger.logger.info("Unable to start your application", err));
    }
}
