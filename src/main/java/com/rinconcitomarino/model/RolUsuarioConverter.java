package com.rinconcitomarino.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolUsuarioConverter implements AttributeConverter<RolUsuario, String> {

    @Override
    public String convertToDatabaseColumn(RolUsuario rolUsuario) {
        return rolUsuario == null ? null : rolUsuario.getDatabaseValue();
    }

    @Override
    public RolUsuario convertToEntityAttribute(String value) {
        return RolUsuario.fromDatabaseValue(value);
    }
}
