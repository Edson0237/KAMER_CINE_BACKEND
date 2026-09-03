package com.kamercinetalents.manager.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Filtre HTTP qui intercepte chaque requête entrante, extrait le token JWT
 * de l'en-tête {@code Authorization: Bearer <token>}, le valide, et peuple
 * le {@link SecurityContextHolder} avec un {@link SecurityContext} contenant
 * l'identité, le rôle, le niveau hiérarchique, le territoire de périmètre
 * et les permissions de l'utilisateur.
 *
 * <p>Ce filtre est le point d'entrée unique de l'authentification : aucun
 * contrôleur ne manipule directement le JWT. Le périmètre territorial est
 * vérifié ultérieurement par {@link TerritoireAccessInterceptor} au niveau
 * des services métier.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Construit le filtre avec le fournisseur JWT injecté.
     *
     * @param jwtTokenProvider le fournisseur de tokens JWT
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Claims claims = jwtTokenProvider.getClaims(token);
            String tokenType = claims.get("type", String.class);

            // N'accepter que les tokens d'accès pour l'authentification
            if ("access".equals(tokenType)) {
                UUID userId = UUID.fromString(claims.getSubject());
                String roleCode = claims.get("role", String.class);
                int niveau = claims.get("niveau", Integer.class);
                String territoireStr = claims.get("territoire", String.class);
                UUID territoireId = territoireStr != null ? UUID.fromString(territoireStr) : null;

                Set<String> permissions = new HashSet<>();
                List<String> permsList = claims.get("perms", List.class);
                if (permsList != null) {
                    permissions.addAll(permsList);
                }

                SecurityContext ctx = new SecurityContext(
                        userId, roleCode, niveau, territoireId, permissions);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(ctx, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT de l'en-tête Authorization.
     *
     * @param request la requête HTTP entrante
     * @return le token sans le préfixe "Bearer ", ou {@code null} si absent
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
