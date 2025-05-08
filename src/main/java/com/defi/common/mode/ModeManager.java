package com.defi.common.mode;

import com.defi.common.util.file.FileUtil;
import com.defi.common.util.log.DebugLogger;
import lombok.Getter;

/**
 * Singleton class to manage application environment mode.
 * <p>
 * This class loads the mode from an environment variable (APP_MODE) or a provided default value,
 * and provides utilities for resolving configuration file paths based on that mode.
 * </p>
 *
 * <p>Example modes: local, dev, sandbox, product.</p>
 */
public class ModeManager {

    /**
     * Singleton instance of ModeManager.
     */
    @Getter
    private static final ModeManager instance = new ModeManager();

    /**
     * Current mode of the application (e.g., "local", "dev", etc.).
     */
    @Getter
    private String mode;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ModeManager() {}

    /**
     * Initializes the mode using the APP_MODE environment variable,
     * or falls back to the given default value if not set.
     *
     * @param defaultValue fallback mode if APP_MODE is not defined
     */
    public void init(String defaultValue) {
        this.mode = System.getenv().getOrDefault("APP_MODE", defaultValue);
        DebugLogger.logger.info("ModeManager init: APP_MODE = {}", this.mode);
    }

    /**
     * Reads the content of a config file under the current mode-specific folder.
     *
     * @param filePath relative path to the config file
     * @return file content as a string
     */
    public String getConfigContent(String filePath) {
        String realPath = getRealConfigFilePath(filePath);
        return FileUtil.readString(realPath);
    }

    /**
     * Resolves the full path to the config file based on the current mode.
     *
     * @param relativeFilePath relative path to the config file
     * @return resolved file path under the config folder
     */
    public String getRealConfigFilePath(String relativeFilePath) {
        return "config" + "/" + mode + "/" + relativeFilePath;
    }
}
