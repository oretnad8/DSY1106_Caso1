package com.smartlogix.pedidosservice.repository;

import com.smartlogix.pedidosservice.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
}

