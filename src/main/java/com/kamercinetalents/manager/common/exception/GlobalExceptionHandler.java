package com.kamercinetalents.manager.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PerimeterAccessException.class)
    public ProblemDetail handlePerimeterAccess(PerimeterAccessException ex, HttpServletRequest req) {
        log.warn("Périmètre refusé: {} sur {}", ex.getMessage(), req.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle("Accès hors périmètre");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Argument invalide: {} sur {}", ex.getMessage(), req.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Requête invalide");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        log.warn("État illégal: {} sur {}", ex.getMessage(), req.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Opération impossible");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Données invalides");
        log.warn("Validation échouée: {} sur {}", detail, req.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Validation échouée");
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .toList());
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Accès refusé: {} sur {}", ex.getMessage(), req.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Accès refusé");
        pd.setTitle("Permission insuffisante");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue sur {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue. Veuillez réessayer ou contacter l'administrateur.");
        pd.setTitle("Erreur interne");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }
}
