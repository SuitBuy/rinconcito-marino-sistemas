package com.rinconcitomarino.dto;

import java.math.BigDecimal;

public record ReservaStats(
        long pendientes,
        long reservasHoy,
        long confirmadasHoy,
        long atendidas,
        long noAsistio,
        long canceladasCliente,
        long rechazadas,
        long expiradas,
        long personasHoy,
        long reservasMes,
        BigDecimal gananciaReservas,
        long opiniones,
        String horarioMasReservado
) {
}
