package com.defi.common.util.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * {@code HikariClient} is a singleton utility class for managing database connections
 * using HikariCP – a high-performance JDBC connection pool.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Properties props = new Properties();
 * props.setProperty("jdbcUrl", "jdbc:postgresql://localhost:5432/mydb");
 * props.setProperty("username", "user");
 * props.setProperty("password", "pass");
 * HikariClient.getInstance().init(props);
 * Connection conn = HikariClient.getInstance().getConnection();
 * }</pre>
 */
public class HikariClient {

    private static HikariClient instance;
    private HikariDataSource dataSource;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private HikariClient() {
    }

    /**
     * Returns the singleton instance of {@code HikariClient}.
     *
     * @return the singleton instance
     */
    public static HikariClient getInstance() {
        if (instance == null) {
            instance = new HikariClient();
        }
        return instance;
    }

    /**
     * Initializes the connection pool with the given properties.
     * These should include JDBC settings such as {@code jdbcUrl}, {@code username}, and {@code password}.
     *
     * @param prop the properties for configuring the HikariCP pool
     */
    public void init(Properties prop) {
        HikariConfig config = new HikariConfig(prop);
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Retrieves a connection from the HikariCP pool.
     *
     * @return a valid {@link Connection}
     * @throws SQLException if acquiring the connection fails
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Closes the connection pool and releases resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Returns the internal {@link DataSource} managed by HikariCP.
     *
     * @return the current {@code DataSource}
     */
    public DataSource getDataSource() {
        return dataSource;
    }
}
