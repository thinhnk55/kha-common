package com.defi.common.permission.entity;

/**
 * {@code Constant} is a class that contains constants related to access control and permission models.
 * These constants are used for storing names of topics, caches, and Casbin model configurations.
 *
 * <p>These constants help standardize and reuse fixed values throughout the permission system,
 * ensuring consistency and ease of maintenance across the project.</p>
 */
public class Constant {
    /**
     * Private constructor to prevent instantiation.
     */
    private Constant() {
        // Utility class
    }

    /**
     * The name of the topic related to permissions in the system.
     * This constant is used when referring to the permission topic in services related to
     * sending or receiving messages.
     */
    public static final String PERMISSION_TOPIC = "PERMISSION_TOPIC";

    /**
     * The name of the cache where permission policies are stored.
     * This constant is used when referring to the cache of permission policies in the system.
     */
    public static final String PERMISSION_POLICIES_CACHE = "PERMISSION_POLICIES_CACHE";

    /**
     * The Casbin model for access control in the system.
     * This is the structure of the Casbin model used for access control.
     *
     * <ul>
     *   <li><strong>request_definition (r)</strong>: Defines the request with the parameters sub (subject), dom (domain), obj (object), act (action).</li>
     *   <li><strong>policy_definition (p)</strong>: Defines the policy with the parameters sub (subject), dom (domain), obj (object), act (action).</li>
     *   <li><strong>role_definition (g)</strong>: Defines relationships between roles in the system.</li>
     *   <li><strong>policy_effect (e)</strong>: Defines the effect of the policy.</li>
     *   <li><strong>matchers (m)</strong>: Rules to check if a request matches a policy.</li>
     * </ul>
     */

    public static final String CASBIN_MODEL = """
            [request_definition]
            r = sub, dom, obj, act
                        
            [policy_definition]
            p = sub, dom, obj, act
                        
            [role_definition]
            g = _, _, _
                        
            [policy_effect]
            e = some(where (p.eft == allow))
                        
            [matchers]
            m = g(r.sub, p.sub, r.dom) && keyMatch(r.dom, p.dom) && keyMatch(r.obj, p.obj) && keyMatch(r.act, p.act)
            """;
}
