package com.kamercinetalents.manager.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour l'authentification (login).
 *
 * @param email    l'adresse email de l'utilisateur
 * @param password le mot de passe en clair
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
