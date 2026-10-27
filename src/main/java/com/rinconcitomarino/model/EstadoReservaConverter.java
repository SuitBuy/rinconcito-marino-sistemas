package com.rinconcitomarino.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoReservaConverter implements AttributeConverter<EstadoReserva, String> {

    @Override
    public String convertToDatabaseColumn(EstadoReserva estadoReserva) {
        return estadoReserva == null ? null : estadoReserva.getEtiqueta();
    }

    @Override
    public EstadoReserva convertToEntityAttribute(String value) {
        return EstadoReserva.fromDatabaseValue(value);
    }
}
