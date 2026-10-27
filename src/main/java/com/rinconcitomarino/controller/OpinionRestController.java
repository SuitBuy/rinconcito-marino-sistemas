package com.rinconcitomarino.controller;

import com.rinconcitomarino.model.Opinion;
import com.rinconcitomarino.service.OpinionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/opiniones")
public class OpinionRestController {

    private final OpinionService opinionService;

    public OpinionRestController(OpinionService opinionService) {
        this.opinionService = opinionService;
    }

    @PostMapping
    public ResponseEntity<Opinion> guardar(@Valid @RequestBody Opinion opinion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opinionService.guardar(opinion));
    }

    @GetMapping
    public List<Opinion> listar(@RequestParam(required = false) String q) {
        return opinionService.listar(q);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        opinionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
