package com.kamercinetalents.manager.iam.controller;

import com.kamercinetalents.manager.iam.dto.*;
import com.kamercinetalents.manager.iam.service.AuthService;
import com.kamercinetalents.manager.iam.service.PasswordResetService;
import com.kamercinetalents.manager.iam.service.TwoFactorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST d'authentification — endpoints publics de login,
 * refresh et logout.
 *
 * <p>Ces endpoints sont les seuls endpoints publics (sans authentification
 * JWT requise) — le login et le refresh sont accessibles sans token.
 * Le logout nécessite un token valide pour identifier l'utilisateur.</p>
 */
@RestController
@RequestMapping("/api/iam/auth")
@Tag(name = "IAM — Authentification", description = "Login, 2FA, refresh token, forgot password, logout")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final TwoFactorService twoFactorService;

    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService,
                          TwoFactorService twoFactorService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.twoFactorService = twoFactorService;
    }

    /**
     * Authentifie un utilisateur et retourne les tokens JWT.
     *
     * @param request les credentials (email + password)
     * @return 200 OK avec les tokens et les infos utilisateur
     */
    @PostMapping("/login")
    @Operation(
            summary = "Authentifier un utilisateur",
            description = "Vérifie les credentials. Si la 2FA est activée, retourne twoFactorRequired=true " +
                    "sans tokens. Sinon, émet un token d'accès JWT et un token de rafraîchissement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie ou 2FA requise",
                    content = @Content(
                            schema = @Schema(oneOf = {AuthResponse.class, Login2FAResponse.class})
                    )),
            @ApiResponse(responseCode = "400", description = "Credentials invalides")
    })
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-2fa")
    @Operation(
            summary = "Vérifier le code 2FA",
            description = "Valide le code à 6 chiffres et émet les tokens JWT si correct."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code valide — tokens émis",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Code invalide ou expiré")
    })
    public ResponseEntity<AuthResponse> verify2FA(@Valid @RequestBody Verify2FARequest request) {
        return ResponseEntity.ok(twoFactorService.verify(request.userId(), request.code()));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Demander une réinitialisation de mot de passe",
            description = "Génère un code OTP à 6 chiffres et l'envoie par email (loggé en V1)."
    )
    @ApiResponse(responseCode = "200", description = "Code envoyé si l'email existe")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Réinitialiser le mot de passe",
            description = "Vérifie le code OTP et définit le nouveau mot de passe."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mot de passe réinitialisé"),
            @ApiResponse(responseCode = "400", description = "Code invalide ou expiré")
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.email(), request.code(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Rafraîchit le token d'accès à partir d'un token de rafraîchissement.
     *
     * @param request le token de rafraîchissement
     * @return 200 OK avec les nouveaux tokens
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Rafraîchir le token d'accès",
            description = "Émet un nouveau token d'accès à partir d'un token de rafraîchissement " +
                    "valide. Les permissions sont rechargées depuis la base."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nouveaux tokens émis",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token de rafraîchissement invalide ou expiré")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * Déconnecte l'utilisateur courant.
     *
     * <p>Journalise la déconnexion dans audit_log. Le token JWT étant
     * stateless, l'invalidation effective se fait côté client.</p>
     *
     * @return 200 OK
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Déconnecter l'utilisateur courant",
            description = "Journalise la déconnexion dans audit_log. " +
                    "L'invalidation du token se fait côté client (stateless JWT)."
    )
    @ApiResponse(responseCode = "200", description = "Déconnexion enregistrée")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }
}
