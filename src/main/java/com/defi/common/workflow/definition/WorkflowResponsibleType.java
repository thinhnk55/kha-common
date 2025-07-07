package com.defi.common.workflow.definition;

/**
 * Enumeration defining the types of responsible entities in a workflow.
 * <p>
 * This enum specifies the different categories of entities that can be assigned
 * responsibility for workflow tasks, actions, or approvals. It helps
 * distinguish
 * between individual users and groups when assigning workflow responsibilities.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
public enum WorkflowResponsibleType {
    /**
     * Represents an individual user as the responsible entity.
     * Used when a specific user is assigned to perform a workflow action,
     * such as reviewing, approving, or processing a task.
     */
    USER,

    /**
     * Represents a group of users as the responsible entity.
     * Used when a group or team is assigned to perform a workflow action,
     * allowing any member of the group to handle the responsibility.
     */
    GROUP
}
