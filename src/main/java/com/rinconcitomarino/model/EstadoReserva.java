package com.rinconcitomarino.model;

import java.util.Arrays;

public enum EstadoReserva {
    PENDIENTE("Pendiente", 1, "warning"),
    CONFIRMADO("Confirmado", 2, "success"),
    ATENDIDO("Atendido", 3, "primary"),
    NO_ASISTIO("No asistio", 4, "dark"),
    CANCELADO_CLIENTE("Cancelado por cliente", 5, "secondary"),
    RECHAZADO("Rechazado", 6, "danger"),
    EXPIRADO("Expirado", 7, "secondary");

    private final String etiqueta;
    private final int orden;
    private final String badgeClass;

    EstadoReserva(String etiqueta, int orden, String badgeClass) {
        this.etiqueta = etiqueta;
        this.orden = orden;
        this.badgeClass = badgeClass;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getOrden() {
        return orden;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public static EstadoReserva fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDIENTE;
        }
        return Arrays.stream(values())
                .filter(estado -> estado.name().equalsIgnoreCase(value) || estado.etiqueta.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de reserva desconocido: " + value));
    }
}
