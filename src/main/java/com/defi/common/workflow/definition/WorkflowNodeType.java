package com.defi.common.workflow.definition;

/**
 * Enumeration defining the types of nodes in a workflow.
 * <p>
 * This enum specifies the different categories of nodes that can exist in a
 * workflow definition. Each node type represents a different stage or action
 * in the workflow process, determining how the workflow engine should handle
 * the node during execution.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
public enum WorkflowNodeType {
    /**
     * Represents the starting point of a workflow.
     * This is the entry node where workflow execution begins.
     * Typically, only one start node should exist per workflow definition.
     */
    START,

    /**
     * Represents the ending point of a workflow.
     * This is the exit node where workflow execution terminates.
     * Multiple end nodes may exist for different completion scenarios.
     */
    END,

    /**
     * Represents a processing node that performs business logic.
     * This type of node typically involves user interaction, data processing,
     * or automated actions that move the workflow forward.
     */
    PROCESS,

    /**
     * Represents a decision point in the workflow.
     * This type of node evaluates conditions and determines the next
     * path to take based on the evaluation result.
     */
    CONDITION
}
