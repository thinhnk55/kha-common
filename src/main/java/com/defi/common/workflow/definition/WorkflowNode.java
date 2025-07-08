package com.defi.common.workflow.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a node in a workflow definition.
 * <p>
 * This class defines a single step or stage within a workflow. Each node
 * contains
 * information about its type, the responsible party, and the possible
 * transitions
 * to other nodes based on different actions that can be performed.
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
 * @see WorkflowNodeType
 * @see WorkflowAction
 * @see WorkflowResponsible
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowNode {

    /**
     * The unique identifier code for this workflow node.
     * Used to reference this node within the workflow definition
     * and for routing between nodes.
     */
    private String code;

    /**
     * The human-readable name of this workflow node.
     * Used for display purposes in user interfaces and
     * workflow documentation.
     */
    private String name;

    /**
     * The type of this workflow node.
     * Determines the behavior and processing logic for this node
     * during workflow execution.
     */
    private WorkflowNodeType type;

    /**
     * Map of possible transitions from this node to other nodes.
     * The key represents the action that triggers the transition,
     * and the value represents the target node code to transition to.
     * This defines the flow control logic for the workflow.
     */
    private Map<WorkflowAction, String> transition;

    /**
     * The entity responsible for handling this workflow node.
     * Specifies who should perform the actions or make decisions
     * at this stage of the workflow.
     */
    private WorkflowResponsible worker;

    /**
     * Limit time in milliseconds to process this workflow node.
     */
    private Long limitTime;
}
