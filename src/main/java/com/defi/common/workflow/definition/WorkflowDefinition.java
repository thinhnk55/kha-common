package com.defi.common.workflow.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a complete workflow definition.
 * <p>
 * This class defines the structure and configuration of a workflow, including
 * all nodes, their relationships, and the responsible parties for workflow
 * execution and supervision. It serves as the blueprint for workflow execution.
 * </p>
 * <p>
 * This class uses Lombok annotations to generate constructors and accessor
 * methods:
 * </p>
 * <ul>
 * <li>{@code @NoArgsConstructor} - Creates a default constructor for frameworks
 * and serialization</li>
 * <li>{@code @AllArgsConstructor} - Creates a constructor with all fields as
 * parameters</li>
 * <li>{@code @Builder} - Provides a fluent builder pattern for object
 * creation</li>
 * </ul>
 * 
 * @author Defi Team
 * @since 1.0.0
 * @see WorkflowNode
 * @see WorkflowResponsible
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDefinition {

    /**
     * Map of all nodes in this workflow definition.
     * The key is the node code, and the value is the corresponding
     * WorkflowNode object. This collection defines all possible
     * states and transitions in the workflow.
     */
    private Map<String, WorkflowNode> nodes;

    /**
     * The code of the starting node in this workflow.
     * This node represents the entry point where workflow execution begins.
     * Must correspond to a node code in the nodes map.
     */
    private String startNode;

    /**
     * The code of the ending node in this workflow.
     * This node represents the exit point where workflow execution terminates.
     * Must correspond to a node code in the nodes map.
     */
    private String endNode;

    /**
     * The primary responsible entity for this workflow.
     * This entity is typically responsible for the overall execution
     * and completion of the workflow process.
     */
    private WorkflowResponsible worker;

    /**
     * The supervisory entity for this workflow.
     * This entity has oversight responsibilities and may be notified
     * of workflow events or have override capabilities.
     */
    private WorkflowResponsible supervisor;

    /**
     * Limit time in milliseconds to process this workflow node.
     */
    private WorkflowLimitTime limitTime;
}
