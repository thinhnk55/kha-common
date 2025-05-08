package com.defi.common.mode;

/**
 * Enumeration representing the operational mode of the application.
 * <p>
 * The application can run in different environments (modes) such as development, sandbox,
 * or production. This enum helps clearly define and restrict possible values for runtime
 * configuration, allowing the system to behave differently based on its deployment context.
 * </p>
 */
public enum Mode {

    /** Local development environment, typically used on a developer’s machine. */
    LOCAL("local"),

    /** Shared development or staging environment with limited external integration. */
    DEVELOPMENT("dev"),

    /** Isolated sandbox environment for testing production-like behavior. */
    SANDBOX("sandbox"),

    /** Live production environment serving real users. */
    PRODUCTION("product");

    private final String name;

    /**
     * Constructs a new {@code Mode} with the given name.
     *
     * @param name the string identifier associated with the mode
     */
    Mode(String name) {
        this.name = name;
    }

    /**
     * Gets the string identifier for the current environment mode.
     *
     * @return the name of the mode (e.g., "dev", "product")
     */
    public String getName() {
        return name;
    }
}
