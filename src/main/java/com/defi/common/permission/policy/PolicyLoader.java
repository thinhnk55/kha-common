package com.defi.common.permission.policy;

import org.casbin.jcasbin.main.Enforcer;

/**
 * Interface for loading policies into Casbin enforcer from various sources.
 *
 * <p>
 * This interface defines the contract for policy loading services that can
 * load policies from different sources such as databases, resource files,
 * or API endpoints.
 * </p>
 *
 * @author Defi Team
 * @since 1.0.0
 */
public interface PolicyLoader {

    /**
     * Loads policies into the Casbin enforcer from the configured source.
     *
     * @param enforcer the Casbin enforcer to load policies into
     * @throws RuntimeException if policy loading fails
     */
    void loadPolicies(Enforcer enforcer);
}