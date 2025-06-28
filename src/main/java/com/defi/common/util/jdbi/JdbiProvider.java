package com.defi.common.util.jdbi;

import com.defi.common.util.json.JsonUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.postgresql.util.PGobject;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Provider for JDBI instance with custom JSON support.
 * 
 * <p>
 * This class manages a singleton JDBI instance configured with custom
 * mappers and argument factories for handling JSON data types with PostgreSQL.
 * It automatically registers ObjectNode mappers for seamless JSON column
 * reading and writing.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Initialize once at application startup
 * JdbiProvider.getInstance().init(dataSource);
 * 
 * // Use JDBI instance anywhere in application
 * Jdbi jdbi = JdbiProvider.getInstance().getJdbi();
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.5
 */
public class JdbiProvider {

    @Getter
    private static final JdbiProvider instance = new JdbiProvider();

    private Jdbi jdbi;

    /**
     * Private constructor for singleton pattern.
     */
    private JdbiProvider() {
        // Singleton constructor
    }

    /**
     * Initializes the JDBI instance with custom JSON mappers.
     * 
     * <p>
     * This method configures JDBI with:
     * </p>
     * <ul>
     * <li>Column mapper for reading PostgreSQL JSON columns as ObjectNode</li>
     * <li>Argument factory for storing ObjectNode as JSON strings</li>
     * </ul>
     * 
     * @param dataSource the database connection source
     */
    public void init(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource);

        // Required to read JSON columns as ObjectNode
        jdbi.registerColumnMapper(ObjectNode.class, (rs, col, ctx) -> {
            PGobject pgObj = (PGobject) rs.getObject(col);
            if (pgObj != null) {
                return JsonUtil.toJsonObject(pgObj.getValue());
            }
            return null;
        });

        // Required to store ObjectNode as JSON string
        jdbi.registerArgument((ArgumentFactory) (type, value, config) -> {
            if (value instanceof ObjectNode) {
                return Optional.of((pos, stmt, ctx) -> {
                    stmt.setString(pos, value.toString());
                });
            }
            return Optional.empty();
        });
    }

    /**
     * Gets the configured JDBI instance.
     * 
     * @return the JDBI instance, or null if not initialized
     */
    public Jdbi getJdbi() {
        return jdbi;
    }
}
