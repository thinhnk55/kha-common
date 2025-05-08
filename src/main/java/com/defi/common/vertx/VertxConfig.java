package com.defi.common.vertx;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Singleton class that holds configuration values for the Vert.x server.
 * <p>
 * This configuration is typically initialized from a JSON-based config (e.g., {@code config.json})
 * and can be accessed globally within the application.
 */
public class VertxConfig {

    /**
     * Singleton instance.
     */
    private static VertxConfig ins = null;

    /**
     * The HTTP port the server should bind to.
     */
    public int httpPort;

    /**
     * Returns the singleton instance of {@code VertxConfig}.
     * If it has not been initialized yet, a new instance is created.
     *
     * @return the {@code VertxConfig} instance
     */
    public static VertxConfig instance() {
        if (ins == null) {
            ins = new VertxConfig();
        }
        return ins;
    }

    /**
     * Private constructor to enforce singleton pattern.
     */
    private VertxConfig() {
        // Prevent direct instantiation
    }

    /**
     * Initializes the configuration using a JSON object.
     * Currently supports reading the HTTP port from the {@code http_port} key.
     *
     * @param config a JSON object containing configuration values
     */
    public void init(ObjectNode config) {
        this.httpPort = config.get("http_port").asInt();
    }
}
