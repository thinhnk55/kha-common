package com.defi.common.permission;

import com.defi.common.permission.entity.Constant;
import com.defi.common.permission.entity.Permission;
import com.defi.common.permission.event.PermissionEvent;
import com.defi.common.permission.event.PermissionEventType;
import com.defi.common.util.json.JsonUtil;
import com.defi.common.util.redis.Redisson;
import lombok.Getter;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.redisson.api.RList;
import org.redisson.api.RTopic;

import java.util.List;

/**
 * {@code PermissionChecker} is a class that manages permission checking and policy enforcement
 * using the Casbin library for access control.
 * <p>
 * This class loads permission policies from Redis, subscribes to permission change events (such as add, remove, or reload),
 * and checks if a specific permission is allowed based on the Casbin model.
 * </p>
 */
public class PermissionChecker {
    /**
     * Singleton instance of {@code PermissionChecker}.
     */
    @Getter
    private static final PermissionChecker instance = new PermissionChecker();

    /**
     * Enforcer for Casbin access control model.
     */
    @Getter
    private Enforcer enforcer;

    /**
     * Redis topic for listening to permission events (add, remove, reload).
     */
    private RTopic topic;

    /**
     * Redis list for storing permission policies in cache.
     */
    private RList<String> list;

    /**
     * Private constructor to prevent direct instantiation.
     * Use {@code PermissionChecker.getInstance()} to get the singleton instance.
     */
    private PermissionChecker() {}

    /**
     * Initializes the PermissionChecker, loading the Casbin model,
     * setting up Redis topic and list, and subscribing to permission events.
     * It also reloads the permission policies from Redis.
     */
    public void init() {
        // Load the Casbin model from the Constant configuration
        Model model = new Model();
        model.loadModelFromText(Constant.CASBIN_MODEL);
        this.enforcer = new Enforcer(model);

        // Initialize Redis topic and list for permission policies
        this.topic = Redisson.getInstance().getClient().getTopic(Constant.PERMISSION_TOPIC);
        this.list = Redisson.getInstance().getClient().getList(Constant.PERMISSION_POLICIES_CACHE);

        // Reload permission policies
        reloadPermission();

        // Subscribe to permission change events
        subscribeToEvent();
    }

    /**
     * Reloads permission policies from the Redis cache.
     * Clears the current Casbin policy and adds policies from the cache.
     */
    public void reloadPermission() {
        List<String> cachedPolicy = list.readAll();
        if (cachedPolicy != null && !cachedPolicy.isEmpty()) {
            enforcer.clearPolicy();  // Clear current policies
            // Add each policy from the cache
            for (String rule : cachedPolicy) {
                String[] parts = rule.split(",");
                if (parts.length >= 4) {
                    enforcer.addPolicy(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
                }
            }
        }
    }

    /**
     * Subscribes to the Redis topic to listen for permission-related events.
     * Based on the event type (add, remove, reload), the corresponding method (add, remove, reloadPermission) is called.
     */
    public void subscribeToEvent() {
        topic.addListener(String.class, (channel, msg) -> {
            PermissionEvent event = JsonUtil.fromJsonString(msg, PermissionEvent.class);
            if (event.getType() == PermissionEventType.ADD_PERMISSION) {
                add(event.getData());
            }
            if (event.getType() == PermissionEventType.REMOVE_PERMISSION) {
                remove(event.getData());
            }
            if (event.getType() == PermissionEventType.RELOAD_PERMISSION) {
                reloadPermission();
            }
        });
    }

    /**
     * Checks if a given permission is allowed based on the Casbin model.
     *
     * @param permission The permission to check.
     * @return {@code true} if the permission is allowed, {@code false} otherwise.
     */
    public boolean isAllowed(Permission permission) {
        return enforcer.enforce(
                permission.getSubject(),
                permission.getDomain(),
                permission.getResource(),
                permission.getAction()
        );
    }

    /**
     * Adds a new permission to the Casbin policy.
     *
     * @param permission The permission to add.
     */
    public void add(Permission permission) {
        enforcer.addPolicy(
                permission.getSubject(),
                permission.getDomain(),
                permission.getResource(),
                permission.getAction()
        );
    }

    /**
     * Removes a permission from the Casbin policy.
     *
     * @param permission The permission to remove.
     */
    public void remove(Permission permission) {
        enforcer.removePolicy(
                permission.getSubject(),
                permission.getDomain(),
                permission.getResource(),
                permission.getAction()
        );
    }
}
