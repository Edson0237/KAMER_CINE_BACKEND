package com.kamercinetalents.manager.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Fournisseur JWT pour la génération et la validation des tokens d'authentification.
 *
 * <p>Centralise la logique cryptographique afin que les contrôleurs et filtres
 * de sécurité ne dépendent que d'une interface unique (principe SRP). La clé
 * secrète et la durée d'expiration sont externalisées dans la configuration
 * applicative ({@code app.jwt.*}).</p>
 *
 * <p>Le token embarque les claims suivants :
 * <ul>
 *   <li>{@code sub} : UUID de l'utilisateur</li>
 *   <li>{@code role} : code du rôle (ex. {@code N1_COMITE_CENTRAL})</li>
 *   <li>{@code niveau} : niveau hiérarchique (1 à 7)</li>
 *   <li>{@code territoire} : UUID du territoire de périmètre</li>
 *   <li>{@code perms} : liste des codes de permissions (RBAC dynamique)</li>
 * </ul>
 * Ces claims évitent un accès base à chaque requête pour vérifier les
 * autorisations, tout en restant stateless.</p>
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;
    private final long refreshExpirationMs;

    /**
     * Construit le fournisseur à partir des propriétés de configuration.
     *
     * @param secret              la clé secrète HMAC-SHA256 (au moins 256 bits)
     * @param expirationMs        la durée de validité du token d'accès en millisecondes
     * @param refreshExpirationMs la durée de validité du token de rafraîchissement
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Génère un token JWT d'accès signé pour un utilisateur.
     *
     * @param ctx le contexte de sécurité contenant l'identité, le rôle,
     *            le territoire et les permissions
     * @return le token JWT compact et signé
     */
    public String generateAccessToken(SecurityContext ctx) {
        Date now = new Date();
        return Jwts.builder()
                .subject(ctx.userId().toString())
                .claim("role", ctx.roleCode())
                .claim("niveau", ctx.niveauHierarchique())
                .claim("territoire", ctx.territoireId() != null ? ctx.territoireId().toString() : null)
                .claim("perms", List.copyOf(ctx.permissions()))
                .claim("type", "access")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Génère un token JWT de rafraîchissement (durée plus longue, sans permissions).
     *
     * @param userId l'identifiant de l'utilisateur
     * @return le token de rafraîchissement signé
     */
    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Valide l'intégrité et la non-expiration d'un token JWT.
     *
     * @param token le token à valider
     * @return {@code true} si le token est valide et non expiré
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrait les claims (payload) d'un token JWT validé.
     *
     * @param token le token à parser
     * @return les claims contenus dans le payload
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
