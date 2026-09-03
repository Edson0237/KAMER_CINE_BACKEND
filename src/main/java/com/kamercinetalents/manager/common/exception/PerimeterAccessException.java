package com.kamercinetalents.manager.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée lorsqu'un utilisateur tente d'accéder à un territoire
 * hors de son périmètre hiérarchique.
 *
 * <p>Retourne un HTTP 403 (Forbidden) au client. Cette exception est
 * levée par {@link com.kamercinetalents.manager.common.service.TerritoireAccessService}
 * et garantit que le contrôle de périmètre est appliqué à chaque endpoint.</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PerimeterAccessException extends RuntimeException {

    /**
     * Construit l'exception avec un message explicatif.
     *
     * @param message le message décrivant la violation de périmètre
     */
    public PerimeterAccessException(String message) {
        super(message);
    }
}
