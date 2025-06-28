package com.defi.common.minio;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider for MinIO client singleton instance.
 * 
 * <p>
 * This class manages the MinIO client lifecycle and provides thread-safe
 * access to a configured MinIO client instance. The client must be initialized
 * once before use.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * // Initialize once at application startup
 * MinioClientProvider.init("http://localhost:9000", "accessKey", "secretKey");
 * 
 * // Get client instance anywhere in application
 * MinioClient client = MinioClientProvider.getInstance();
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.5
 */
@Slf4j
public class MinioClientProvider {

    @Getter
    private static MinioClient instance;

    /**
     * Private constructor to prevent instantiation.
     */
    private MinioClientProvider() {
        // Utility class
    }

    /**
     * Initializes the MinIO client with connection credentials.
     * 
     * <p>
     * This method must be called once at application startup before using
     * the MinIO client. Subsequent calls will throw an exception.
     * </p>
     * 
     * @param endpoint  the MinIO server endpoint URL
     * @param accessKey the access key for authentication
     * @param secretKey the secret key for authentication
     * @throws IllegalStateException if the client is already initialized
     */
    public static synchronized void init(String endpoint, String accessKey, String secretKey) {
        if (instance != null)
            throw new IllegalStateException("MinioClient is already initialized!");
        instance = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        log.info("MinioClient initialized with endpoint: {}", endpoint);
    }
}