package com.rinconcitomarino.repository;

import com.rinconcitomarino.model.ReservaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaHistorialRepository extends JpaRepository<ReservaHistorial, Long> {

    List<ReservaHistorial> findTop20ByOrderByFechaDesc();
}
