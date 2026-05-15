package com.smartlogix.aprobacionesservice.repository;

import com.smartlogix.aprobacionesservice.model.UbicacionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UbicacionMappingRepository extends JpaRepository<UbicacionMapping, Integer> {
    Optional<UbicacionMapping> findByCodigoUbicacion(String codigoUbicacion);
}

