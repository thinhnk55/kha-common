package com.defi.common.token;

import com.defi.common.token.entity.ClaimField;
import com.defi.common.token.entity.SubjectType;
import com.defi.common.token.entity.Token;
import com.defi.common.token.entity.TokenType;
import com.defi.common.token.helper.RSAKeyUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.*;

import lombok.Getter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@code TokenManager} is a singleton class responsible for issuing, signing, validating,
 * and parsing JWT access tokens using RSA public/private key pairs via the Nimbus JOSE + JWT library.
 *
 * <p>Tokens are generated from a {@link Token} domain object and signed with RS256.</p>
 *
 * <p>This utility supports:</p>
 * <ul>
 *     <li>Loading RSA keys from PEM strings</li>
 *     <li>Creating signed JWTs for sessions</li>
 *     <li>Refreshing expired tokens</li>
 *     <li>Validating and parsing existing tokens</li>
 * </ul>
 */
public class TokenManager {

    /**
     * Singleton instance of the TokenManager.
     */
    @Getter
    private static final TokenManager instance = new TokenManager();

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    private RSASSASigner signer;
    private RSASSAVerifier verifier;

    /**
     * Private constructor for singleton pattern.
     */
    private TokenManager() {
    }

    /**
     * Initializes the token manager with public and private keys in PEM format.
     *
     * @param publicKeyPem  the PEM string of the public key
     * @param privateKeyPem the PEM string of the private key
     * @param paraphrase    the private key passphrase
     * @throws Exception if the keys cannot be loaded or are invalid
     */
    public void init(String publicKeyPem, String privateKeyPem, String paraphrase) throws Exception {
        this.publicKey = RSAKeyUtil.readRSAPublicKeyFromPEM(publicKeyPem);
        this.privateKey = RSAKeyUtil.readRSAPrivateKeyFromPEM(privateKeyPem, paraphrase);
        this.signer = new RSASSASigner(privateKey);
        this.verifier = new RSASSAVerifier(publicKey);
    }

    /**
     * Generates a signed JWT access token with the provided session and subject details.
     *
     * @param sessionId    the session UUID
     * @param type         the token type (e.g., access, refresh)
     * @param subjectID    the ID of the subject (usually user ID)
     * @param subjectName  the display name or username of the subject
     * @param roles        list of role IDs
     * @param groups       list of group IDs
     * @param timeToLive   token TTL in seconds
     * @return a signed JWT string
     */
    public String generateToken(UUID sessionId, TokenType type,
                                UUID subjectID, String subjectName, List<Integer> roles,
                                List<Integer> groups, long timeToLive) {
        long issuedAt = Instant.now().getEpochSecond();
        Token token = Token.builder()
                .sessionId(sessionId)
                .tokenType(type)
                .subjectId(subjectID)
                .subjectName(subjectName)
                .subjectType(SubjectType.USER)
                .roles(roles)
                .groups(groups)
                .iat(issuedAt)
                .exp(issuedAt + timeToLive)
                .build();
        return signToken(token);
    }

    /**
     * Refreshes an existing token by creating a new one with updated issue and expiration times.
     *
     * @param token       the original token object
     * @param timeToLive  time-to-live in seconds for the new token
     * @return a new signed JWT string
     */
    public String refreshToken(Token token, int timeToLive) {
        long issuedAt = Instant.now().getEpochSecond();
        Token newToken = Token.builder()
                .tokenType(TokenType.ACCESS_TOKEN)
                .sessionId(token.getSessionId())
                .subjectId(token.getSubjectId())
                .subjectName(token.getSubjectName())
                .subjectType(token.getSubjectType())
                .roles(token.getRoles())
                .groups(token.getGroups())
                .iat(issuedAt)
                .exp(issuedAt + timeToLive)
                .build();
        return signToken(newToken);
    }

    /**
     * Signs a {@link Token} object as a JWT using RS256 algorithm.
     *
     * @param payload the token payload
     * @return a signed JWT string
     */
    private String signToken(Token payload) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(payload.getSubjectId().toString())
                    .issueTime(new Date(payload.getIat() * 1000))
                    .expirationTime(new Date(payload.getExp() * 1000))
                    .claim(ClaimField.ID.getName(), payload.getSessionId().toString())
                    .claim(ClaimField.TYPE.getName(), payload.getTokenType().getName())
                    .claim(ClaimField.SUBJECT_NAME.getName(), payload.getSubjectName())
                    .claim(ClaimField.SUBJECT_TYPE.getName(), payload.getSubjectName())
                    .claim(ClaimField.ROLES.getName(), payload.getRoles())
                    .claim(ClaimField.GROUPS.getName(), payload.getGroups())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Token signing failed", e);
        }
    }

    /**
     * Validates the signature and expiration of the provided JWT.
     *
     * @param signedJWT the parsed JWT object
     * @return {@code true} if the token is valid and not expired, otherwise {@code false}
     */
    public boolean validateToken(SignedJWT signedJWT) {
        try {
            boolean signatureValid = signedJWT.verify(verifier);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            long expiresAt = claims.getExpirationTime().toInstant().getEpochSecond();
            long now = Instant.now().getEpochSecond();
            boolean notExpired = now < expiresAt;
            return signatureValid && notExpired;
        } catch (JOSEException | ParseException e) {
            return false;
        }
    }

    /**
     * Parses a JWT string and returns the {@link Token} object if valid.
     *
     * @param token the JWT string
     * @return a valid {@link Token} object, or {@code null} if invalid
     */
    public Token parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!validateToken(signedJWT)) {
                return null;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            List<Object> roles = claims.getListClaim(ClaimField.ROLES.getName());
            List<Object> groups = claims.getListClaim(ClaimField.GROUPS.getName());

            long issuedAt = claims.getIssueTime().toInstant().getEpochSecond();
            long expiresAt = claims.getExpirationTime().toInstant().getEpochSecond();

            return Token.builder()
                    .sessionId(UUID.fromString((String) claims.getClaim(ClaimField.ID.getName())))
                    .tokenType(TokenType.forName((String) claims.getClaim(ClaimField.TYPE.getName())))
                    .subjectId(UUID.fromString(claims.getSubject()))
                    .subjectName((String) claims.getClaim(ClaimField.SUBJECT_NAME.getName()))
                    .subjectType(SubjectType.forName((String) claims.getClaim(ClaimField.SUBJECT_TYPE.getName())))
                    .roles(roles.stream()
                            .filter(Integer.class::isInstance)
                            .map(Integer.class::cast)
                            .collect(Collectors.toList()))
                    .groups(groups.stream()
                            .filter(Integer.class::isInstance)
                            .map(Integer.class::cast)
                            .collect(Collectors.toList()))
                    .iat(issuedAt)
                    .exp(expiresAt)
                    .build();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse token", e);
        }
    }
}

