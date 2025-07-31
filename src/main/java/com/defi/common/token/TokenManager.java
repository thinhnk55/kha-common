package com.defi.common.token;

import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;
import com.defi.common.token.helper.RSAKeyUtil;
import com.defi.common.token.service.TokenIssuerService;
import com.defi.common.token.service.TokenVerifierService;
import com.defi.common.token.service.impl.TokenIssuerServiceImpl;
import com.defi.common.token.service.impl.TokenVerifierServiceImpl;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.defi.common.util.log.ErrorLogger;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import lombok.extern.slf4j.Slf4j;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

/**
 * Singleton manager for JWT token operations including issuing and
 * verification.
 * 
 * <p>
 * This class provides centralized management of JWT token operations using RSA
 * key pairs for signing and verification. It supports both token issuing and
 * token parsing/verification capabilities.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * TokenManager manager = TokenManager.getInstance();
 * manager.init(publicKeyPEM, privateKeyPEM, passphrase);
 * 
 * // Parse and verify token
 * Token token = manager.parseToken(jwtString);
 * 
 * // Issue new token
 * String jwt = manager.issueToken(token);
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Slf4j
public class TokenManager {

    private static TokenManager instance;

    private TokenVerifierService verifier;
    private TokenIssuerService issuer;
    private volatile boolean initialized = false;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private TokenManager() {
    }

    /**
     * Initializes the TokenManager with RSA key pair for JWT operations.
     * 
     * <p>
     * This method must be called before using any token operations.
     * Subsequent calls will be ignored to prevent re-initialization.
     * </p>
     * 
     * @param publicKey  RSA public key in PEM format for token verification
     * @param privateKey RSA private key in PEM format for token signing
     * @param paraphrase passphrase for the private key (can be null if not
     *                   encrypted)
     * @throws Exception                if key parsing or service initialization
     *                                  fails
     * @throws IllegalArgumentException if required parameters are null or empty
     */
    public synchronized void init(String publicKey, String privateKey, String paraphrase) throws Exception {
        if (initialized) {
            log.warn("TokenManager is already initialized - ignoring subsequent init call");
            return;
        }

        // Validate input parameters
        if (publicKey == null || publicKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Public key cannot be null or empty");
        }
        if (privateKey == null || privateKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Private key cannot be null or empty");
        }

        try {
            log.info("Initializing TokenManager with RSA key pair");

            // Initialize verifier with public key
            RSAPublicKey rsaPublicKey = RSAKeyUtil.readRSAPublicKeyFromPEM(publicKey);
            RSASSAVerifier rsassaVerifier = new RSASSAVerifier(rsaPublicKey);
            verifier = new TokenVerifierServiceImpl(rsassaVerifier);

            // Initialize issuer with private key
            RSAPrivateKey rsaPrivateKey = RSAKeyUtil.readRSAPrivateKeyFromPEM(privateKey, paraphrase);
            RSASSASigner rsassaSigner = new RSASSASigner(rsaPrivateKey);
            issuer = new TokenIssuerServiceImpl(rsassaSigner);

            initialized = true;
            log.info("TokenManager initialized successfully");

        } catch (Exception e) {
            // Cleanup on failure
            verifier = null;
            issuer = null;
            throw e;
        }
    }

    /**
     * Gets the singleton instance of TokenManager.
     * 
     * @return the TokenManager instance
     */
    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    /**
     * Parses and verifies a JWT token string.
     * 
     * @param jwtToken the JWT token string to parse
     * @return the parsed Token object
     * @throws IllegalStateException    if TokenManager is not initialized
     * @throws IllegalArgumentException if jwtToken is null or empty
     * @throws RuntimeException         if token parsing fails
     */
    public Token parseToken(String jwtToken) {
        if (!initialized || verifier == null) {
            throw new IllegalStateException("TokenManager is not initialized. Call init() first.");
        }

        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token cannot be null or empty");
        }

        try {
            return verifier.parseToken(jwtToken);
        } catch (Exception e) {
            ErrorLogger.create("Failed to parse JWT token", e)
                    .putContext("jwtToken", jwtToken)
                    .log();
            throw new RuntimeException("Token parsing failed", e);
        }
    }

    /**
     * Generates a new JWT token with the specified parameters.
     * 
     * @param sessionId   unique session identifier
     * @param type        the type of token to generate
     * @param subjectID   unique identifier of the subject
     * @param subjectName display name of the subject
     * @param roles       list of role identifiers
     * @param groups      list of group identifiers
     * @param permissions list of permissions identifiers
     * @param timeToLive  token lifetime in seconds
     * @return the generated JWT token string
     * @throws IllegalStateException if TokenManager is not initialized
     * @throws RuntimeException      if token generation fails
     */
    public String generateToken(String sessionId, TokenType type,
            String subjectID, String subjectName,
                                List<String> roles,
                                List<String> groups,
                                List<String> permissions,
                                long timeToLive) {
        if (!initialized || issuer == null) {
            throw new IllegalStateException("TokenManager is not initialized. Call init() first.");
        }

        try {
            return issuer.generateToken(sessionId, type, subjectID, subjectName, roles, groups, permissions, timeToLive);
        } catch (Exception e) {
            ErrorLogger.create("Failed to generate JWT token", e)
                    .putContext("sessionId", sessionId)
                    .putContext("type", type)
                    .putContext("subjectID", subjectID)
                    .putContext("subjectName", subjectName)
                    .putContext("roles", roles)
                    .putContext("groups", groups)
                    .putContext("permissions", permissions)
                    .putContext("timeToLive", timeToLive)
                    .log();
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * Refreshes an existing token with new expiration time.
     * 
     * @param token      the original token to refresh
     * @param timeToLive new token lifetime in seconds
     * @return the refreshed JWT token string
     * @throws IllegalStateException    if TokenManager is not initialized
     * @throws IllegalArgumentException if token is null
     * @throws RuntimeException         if token refresh fails
     */
    public String refreshToken(Token token, int timeToLive) {
        if (!initialized || issuer == null) {
            throw new IllegalStateException("TokenManager is not initialized. Call init() first.");
        }

        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }

        try {
            return issuer.refreshToken(token, timeToLive);
        } catch (Exception e) {
            ErrorLogger.create("Failed to refresh JWT token", e)
                    .putContext("token", token.toString())
                    .putContext("newTimeToLive", timeToLive)
                    .log();
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * Checks if the TokenManager has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the token verifier service.
     * 
     * @return the TokenVerifierService instance or null if not initialized
     */
    public TokenVerifierService getVerifier() {
        return verifier;
    }

    /**
     * Gets the token issuer service.
     * 
     * @return the TokenIssuerService instance or null if not initialized
     */
    public TokenIssuerService getIssuer() {
        return issuer;
    }
}
