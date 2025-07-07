package com.defi.common.workflow.definition;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Represents a responsible entity in a workflow system.
 * <p>
 * This class defines an entity (user or group) that is responsible for
 * performing
 * specific actions within a workflow. It contains information about the type of
 * responsible entity, its unique identifier, and display name.
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
 * @see WorkflowResponsibleType
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowResponsible {

    /**
     * The type of responsible entity (user or group).
     * Determines whether this responsibility is assigned to an individual
     * user or a group of users.
     */
    private WorkflowResponsibleType type;

    /**
     * The unique identifier of the responsible entity.
     * For users, this would typically be a user ID.
     * For groups, this would typically be a group ID.
     */
    private Long id;

    /**
     * The display name of the responsible entity.
     * Used for UI display purposes and human-readable identification
     * of the responsible party.
     */
    private String name;
}
