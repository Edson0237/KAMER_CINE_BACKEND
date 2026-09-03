package com.kamercinetalents.manager.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI / Swagger UI.
 *
 * <p>Expose la documentation automatique de l'API sur
 * {@code /swagger-ui.html} avec les métadonnées du projet KAMER CINÉ
 * TALENTS MANAGER. Le schéma de sécurité JWT Bearer est déclaré
 * globalement afin que chaque endpoint puisse le référencer via
 * {@code @SecurityRequirement}.</p>
 */
@Configuration
public class OpenApiConfig {

    /** Nom logique du schéma de sécurité JWT utilisé dans les annotations OpenAPI. */
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Construit la spécification OpenAPI avec les métadonnées du projet
     * et le schéma d'authentification JWT Bearer.
     *
     * @return la configuration OpenAPI complète
     */
    @Bean
    public OpenAPI kctManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KAMER CINÉ TALENTS MANAGER API")
                        .version("1.0.0-SNAPSHOT")
                        .description("Plateforme nationale de pilotage du programme de formation "
                                + "aux métiers du cinéma — 360 communes du Cameroun.")
                        .contact(new Contact()
                                .name("Comité Central KAMER CINÉ TALENTS")
                                .email("contact@kamercinetalents.cm")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Authentification JWT Bearer. "
                                                + "Obtenir un token via POST /api/iam/auth/login.")));
    }
}
