package com.defi.common.workflow.definition;

/**
 * Enumeration defining the possible actions that can be performed in a
 * workflow.
 * <p>
 * This enum specifies the different types of actions that can trigger
 * transitions
 * between workflow nodes. These actions represent the decisions or operations
 * that users or systems can perform to advance the workflow.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
public enum WorkflowAction {
    /**
     * Represents a simple progression to the next step in the workflow.
     * This action typically indicates successful completion of a task
     * and moves the workflow forward to the next node.
     */
    NEXT,

    /**
     * Represents an approval action in the workflow.
     * This action indicates that a request, document, or process
     * has been approved and can proceed to the next stage.
     */
    APPROVE,

    /**
     * Represents a rejection action in the workflow.
     * This action indicates that a request, document, or process
     * has been rejected and may require revision or termination.
     */
    REJECT
}
