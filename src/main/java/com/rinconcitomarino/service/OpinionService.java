package com.rinconcitomarino.service;

import com.rinconcitomarino.model.Opinion;
import com.rinconcitomarino.repository.OpinionRepository;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class OpinionService {

    private final OpinionRepository opinionRepository;
    private final OpinionNotificationService opinionNotificationService;

    public OpinionService(
            OpinionRepository opinionRepository,
            OpinionNotificationService opinionNotificationService
    ) {
        this.opinionRepository = opinionRepository;
        this.opinionNotificationService = opinionNotificationService;
    }

    @Transactional
    public Opinion guardar(Opinion opinion) {
        opinion.setId(null);
        Opinion opinionGuardada = opinionRepository.save(opinion);
        opinionNotificationService.enviarOpinionRecibida(opinionGuardada);
        return opinionGuardada;
    }

    @Transactional(readOnly = true)
    public List<Opinion> listar(String busqueda) {
        List<Opinion> opiniones = opinionRepository.findAll(specBusqueda(busqueda));
        opiniones.sort(Comparator.comparing(Opinion::getFechaRegistro, Comparator.nullsLast(Comparator.reverseOrder())));
        return opiniones;
    }

    @Transactional
    public void cambiarModeracion(Long id, boolean visible, boolean destacado) {
        Opinion opinion = opinionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opinion no encontrada: " + id));
        opinion.setVisible(visible);
        opinion.setDestacado(destacado);
        opinionRepository.save(opinion);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!opinionRepository.existsById(id)) {
            throw new IllegalArgumentException("Opinion no encontrada: " + id);
        }
        opinionRepository.deleteById(id);
    }

    private Specification<Opinion> specBusqueda(String busqueda) {
        if (StringUtils.isBlank(busqueda)) {
            return (root, query, cb) -> cb.conjunction();
        }
        String termino = "%" + busqueda.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> {
            Predicate nombres = cb.like(cb.lower(root.get("nombres")), termino);
            Predicate correo = cb.like(cb.lower(root.get("correo")), termino);
            Predicate comentario = cb.like(cb.lower(root.get("comentario")), termino);
            return cb.or(nombres, correo, comentario);
        };
    }
}
