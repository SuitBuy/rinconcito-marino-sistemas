package com.rinconcitomarino.model;

import java.util.Arrays;

public enum RolUsuario {
    ADMIN("admin", "Administrador"),
    EMPLEADO("empleado", "Empleado"),
    LECTOR("lector", "Solo lectura");

    private final String databaseValue;
    private final String etiqueta;

    RolUsuario(String databaseValue, String etiqueta) {
        this.databaseValue = databaseValue;
        this.etiqueta = etiqueta;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getAuthority() {
        return "ROLE_" + name();
    }

    public static RolUsuario fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            return EMPLEADO;
        }
        return Arrays.stream(values())
                .filter(rol -> rol.name().equalsIgnoreCase(value) || rol.databaseValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol desconocido: " + value));
    }
}
