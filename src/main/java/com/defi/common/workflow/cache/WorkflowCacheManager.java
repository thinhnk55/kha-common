package com.defi.common.workflow.cache;

import com.defi.common.mode.ModeManager;
import com.defi.common.util.jdbi.JdbiProvider;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.workflow.constant.WorkflowConstant;
import com.defi.common.workflow.definition.WorkflowDefinition;
import com.defi.common.workflow.event.WorkflowEventManager;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.flywaydb.core.internal.util.JsonUtils;
import org.postgresql.util.PGobject;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for caching and managing workflow definitions.
 * 
 * <p>
 * This class provides a centralized cache for workflow definitions, supporting
 * both
 * initial loading from database and dynamic reloading of individual workflows.
 * It integrates with the event system to automatically reload workflows when
 * update notifications are received.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Singleton pattern for global access</li>
 * <li>Database-driven workflow definition loading</li>
 * <li>Individual workflow reloading capability</li>
 * <li>Integration with workflow event system</li>
 * <li>JSON-based configuration management</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * // Initialize the cache manager
 * WorkflowCacheManager cacheManager = WorkflowCacheManager.getInstance();
 * cacheManager.init();
 * 
 * // Get a workflow definition
 * WorkflowDefinition definition = cacheManager.getWorkflowDefinitionMap().get("WORKFLOW_CODE");
 * 
 * // Reload a specific workflow
 * cacheManager.reloadWorkflow("WORKFLOW_CODE");
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see WorkflowDefinition
 * @see WorkflowEventManager
 */
public class WorkflowCacheManager {

    /**
     * Singleton instance of the WorkflowCacheManager.
     */
    private static final WorkflowCacheManager instance = new WorkflowCacheManager();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private WorkflowCacheManager() {
    }

    /**
     * Gets the singleton instance of WorkflowCacheManager.
     * 
     * @return the singleton instance
     */
    public static WorkflowCacheManager getInstance() {
        return instance;
    }

    /**
     * Configuration object loaded from the workflow configuration file.
     * Contains source type, database queries, and other configuration settings.
     */
    @Getter
    private ObjectNode config;

    /**
     * Map of workflow definitions cached in memory.
     * The key is the workflow code, and the value is the corresponding
     * WorkflowDefinition.
     */
    @Getter
    private Map<String, WorkflowDefinition> workflowDefinitionMap;

    /**
     * Initializes the workflow cache manager.
     * 
     * <p>
     * This method performs the following operations:
     * </p>
     * <ol>
     * <li>Loads configuration from the workflow config file</li>
     * <li>Reloads all workflow definitions from the database</li>
     * <li>Starts listening for workflow update events</li>
     * </ol>
     * 
     * <p>
     * The configuration file should be located at the path specified by
     * {@link WorkflowConstant#WORKFLOW_CONFIG_FILE}.
     * </p>
     * 
     * @throws RuntimeException if configuration loading or database operations fail
     */
    public void init() {
        String data = ModeManager.getInstance().getConfigContent(
                WorkflowConstant.WORKFLOW_CONFIG_FILE);
        config = JsonUtils.parseJson(data, ObjectNode.class);
        reloadAllWorkflow();
        WorkflowEventManager.getInstance().init();
        WorkflowEventManager.getInstance().startListening();
    }

    /**
     * Reloads all workflow definitions from the database.
     * 
     * <p>
     * This method queries the database for all workflow definitions and updates
     * the in-memory cache. The query is obtained from the configuration object.
     * </p>
     * 
     * <p>
     * The method expects the database query to return rows with the following
     * columns:
     * </p>
     * <ul>
     * <li><code>code</code> - The workflow code/identifier</li>
     * <li><code>config</code> - JSON string containing the workflow definition</li>
     * </ul>
     * 
     * @throws RuntimeException if database operations fail
     */
    public void reloadAllWorkflow() {
        String sourceType = config.get("source_type").asText();
        if (sourceType.equals("database")) {
            String query = config.get("source_config").get("query_all").asText();
            workflowDefinitionMap = JdbiProvider.getInstance().getJdbi().withHandle(handle -> {
                Map<String, WorkflowDefinition> resultMap = new HashMap<>();
                handle.createQuery(query)
                        .mapToMap()
                        .list()
                        .forEach(row -> {
                            String code = (String) row.get("code");
                            PGobject object = (PGobject) row.get("config");
                            if (code != null && object != null) {
                                WorkflowDefinition definition = JsonUtil.fromJson(object.getValue(), WorkflowDefinition.class);
                                if (definition != null) {
                                    resultMap.put(code, definition);
                                }
                            }
                        });

                return resultMap;
            });
        }
    }

    /**
     * Reloads a specific workflow definition from the database.
     * 
     * <p>
     * This method queries the database for a specific workflow definition by its
     * code
     * and updates the corresponding entry in the in-memory cache.
     * </p>
     * 
     * <p>
     * The method uses a parameterized query that expects a <code>:code</code>
     * parameter
     * to be bound with the workflow code.
     * </p>
     * 
     * @param code the workflow code to reload
     * @throws RuntimeException if database operations fail
     */
    public void reloadWorkflow(String code) {
        String query = config.get("source_config").get("query_by_code").asText();
        JdbiProvider.getInstance().getJdbi().withHandle(handle -> {
            String configJson = handle.createQuery(query)
                    .bind("code", code)
                    .mapTo(String.class)
                    .findFirst()
                    .orElse(null);

            if (configJson != null) {
                WorkflowDefinition definition = JsonUtil.fromJson(configJson, WorkflowDefinition.class);
                if (definition != null) {
                    workflowDefinitionMap.put(code, definition);
                }
            }
            return null;
        });
    }

    /**
     * Gets a workflow definition by its code.
     * 
     * @param code the workflow code to get
     * @return the workflow definition
     */
    public WorkflowDefinition getWorkflowDefinition(String code) {
        return workflowDefinitionMap.get(code);
    }
}
