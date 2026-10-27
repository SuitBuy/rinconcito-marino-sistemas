package com.rinconcitomarino.dto;

import com.rinconcitomarino.model.EstadoReserva;
import jakarta.validation.constraints.NotNull;

public class EstadoUpdateRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoReserva estado;

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }
}
