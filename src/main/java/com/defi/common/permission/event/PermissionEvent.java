package com.defi.common.permission.event;

import com.defi.common.permission.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code PermissionEvent} represents an event related to a permission in the system.
 * It contains information about the type of event and the associated permission data.
 *
 * <p>This class is used to represent changes or actions related to permissions, such as
 * creation, modification, or deletion of permissions, and to trigger or capture
 * events related to permission changes in the system.</p>
 */
@Data
@Builder
@AllArgsConstructor
public class PermissionEvent {

    /**
     * The type of the permission event (e.g., CREATE, UPDATE, DELETE).
     * This field defines the event's nature and can be used to handle the event accordingly.
     */
    private PermissionEventType type;

    /**
     * The permission data associated with the event.
     * This field stores the actual permission object that is being created, updated, or deleted.
     */
    private Permission data;
    /**
     * Default no-args constructor required for deserialization frameworks like Jackson.
     */
    public PermissionEvent() {
        // Required for frameworks
    }
}
