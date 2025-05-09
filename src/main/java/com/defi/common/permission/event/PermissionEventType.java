package com.defi.common.permission.event;

/**
 * {@code PermissionEventType} is an enum representing the types of events that can occur
 * related to permissions in the system. These events could include actions such as
 * adding, removing, or reloading permissions.
 *
 * <p>This enum is used to identify and handle different permission-related events,
 * allowing for appropriate actions to be taken when these events occur in the system.</p>
 */
public enum PermissionEventType {

    /**
     * Event type representing the reloading of permissions in the system.
     * This event is typically used to trigger a reload of permission data or rules.
     */
    RELOAD_PERMISSION("reload_permission"),

    /**
     * Event type representing the addition of a new permission.
     * This event is triggered when a new permission is created and needs to be processed.
     */
    ADD_PERMISSION("add_permission"),

    /**
     * Event type representing the removal of an existing permission.
     * This event is triggered when a permission is deleted and needs to be processed.
     */
    REMOVE_PERMISSION("remove_permission");

    private final String name;

    /**
     * Constructor to assign the name of each permission event type.
     *
     * @param name The name of the permission event type (e.g., "reload_permission").
     */
    PermissionEventType(String name) {
        this.name = name;
    }

    /**
     * Retrieves the {@code PermissionEventType} based on the event name.
     *
     * @param name The name of the event type.
     * @return The corresponding {@code PermissionEventType}, or {@code null} if no match is found.
     */
    public static PermissionEventType forName(String name) {
        for (PermissionEventType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the name of the permission event type.
     *
     * @return The name of the event type.
     */
    public String getName() {
        return name;
    }
}
