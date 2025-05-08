package com.defi.common.util.redis;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.IOException;

/**
 * {@code Redisson} is a lightweight singleton wrapper around the Redisson client for Redis.
 * It provides basic lifecycle management, including initialization from a YAML configuration,
 * client access, and graceful shutdown.
 *
 * <p>This is useful for centralizing Redis configuration and access across an application.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Redisson redis = Redisson.getInstance();
 * redis.init("redis-config.yml");
 * RedissonClient client = redis.getClient();
 * }</pre>
 */
public class Redisson {

    private static volatile Redisson instance;
    private RedissonClient redisson;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private Redisson() {
    }

    /**
     * Returns the singleton instance of {@code Redisson}.
     *
     * @return the singleton instance
     */
    public static Redisson getInstance() {
        if (instance == null) {
            instance = new Redisson();
        }
        return instance;
    }

    /**
     * Initializes the Redisson client using the given YAML configuration file.
     * Must be called before accessing {@link #getClient()}.
     *
     * @param yamlConfig the path to the YAML configuration file
     * @throws IOException if the file cannot be read or parsed
     */
    public void init(String yamlConfig) throws IOException {
        Config config = Config.fromYAML(yamlConfig);
        redisson = org.redisson.Redisson.create(config);
    }

    /**
     * Returns the underlying {@link RedissonClient} instance.
     *
     * @return the Redisson client
     */
    public RedissonClient getClient() {
        return redisson;
    }

    /**
     * Shuts down the Redisson client and releases resources.
     * This also resets the singleton instance for future reinitialization.
     */
    public void shutdown() {
        if (redisson != null) {
            redisson.shutdown();
            redisson = null;
            instance = null;
        }
    }
}
