package com.defi.common.permission.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code Permission} represents a permission entity in the system.
 * It stores details about a permission including the subject, domain, resource,
 * action, and any additional notes related to the permission.
 *
 * <p>This class is used to define access permissions for various subjects
 * within different domains, which can be used to enforce security policies.</p>
 */
@Data
@Builder
@AllArgsConstructor
public class Permission {

    /**
     * Unique identifier for the permission.
     * This field represents the primary key for the permission entity.
     */
    private Integer id;

    /**
     * The subject of the permission (e.g., user, system).
     * This defines who or what the permission is granted to.
     */
    private String subject;

    /**
     * The domain where the permission applies (e.g., organization, project).
     * This field helps define the scope of the permission.
     */
    private String domain;

    /**
     * The resource that the permission applies to (e.g., file, service).
     * This defines which object or entity the permission is related to.
     */
    private String resource;

    /**
     * The action allowed by the permission (e.g., read, write, delete).
     * This defines what the subject can do to the resource.
     */
    private String action;

    /**
     * Optional additional notes about the permission.
     * This field can be used to store any additional information or context
     * regarding the permission.
     */
    private String note;

    /**
     * Default no-args constructor required for deserialization frameworks like Jackson.
     */
    public Permission() {
        // Required for frameworks
    }
}
