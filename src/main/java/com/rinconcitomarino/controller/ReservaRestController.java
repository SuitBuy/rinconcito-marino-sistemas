package com.rinconcitomarino.controller;

import com.rinconcitomarino.dto.EstadoUpdateRequest;
import com.rinconcitomarino.model.Reserva;
import com.rinconcitomarino.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
public class ReservaRestController {

    private final ReservaService reservaService;

    public ReservaRestController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody Reserva reserva) {
        Reserva creada = reservaService.crearReserva(reserva);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("reserva", creada);
        String whatsappUrl = reservaService.generarWhatsappUrl(creada);
        if (whatsappUrl != null) {
            respuesta.put("whatsappUrl", whatsappUrl);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @GetMapping
    public List<Reserva> listar(@RequestParam(required = false) String q) {
        return reservaService.listarReservas(q);
    }

    @PatchMapping("/{id}/estado")
    public Reserva cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoUpdateRequest request) {
        return reservaService.cambiarEstado(id, request.getEstado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
