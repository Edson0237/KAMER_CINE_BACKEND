package com.kamercinetalents.manager.admin.domain;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.core.JacksonException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

/**
 * Convertisseur JPA pour sérialiser/désérialiser la colonne JSONB {@code details}
 * de la table {@code audit_log}.
 *
 * <p>Utilise Jackson pour transformer un {@code Map<String, Object>} en texte
 * JSON et inversement, de façon transparente pour l'entité.</p>
 */
@Converter
public class AuditDetailsConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JacksonException e) {
            return "{}";
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return mapper.readValue(dbData, new TypeReference<>() {});
        } catch (JacksonException e) {
            return Map.of();
        }
    }
}
