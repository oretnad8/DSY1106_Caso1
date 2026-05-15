package com.smartlogix.ubicacionesservice.repository;

import com.smartlogix.ubicacionesservice.model.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {
    Optional<Ubicacion> findByCodigoUbicacion(String codigoUbicacion);
    
    // Buscar por piso (todos los pasillos)
    List<Ubicacion> findByPisoOrderByPasilloAscNumeroAsc(Character piso);
    
    // Buscar por pasillo (todos los pisos)
    List<Ubicacion> findByPasilloOrderByPisoAscNumeroAsc(Integer pasillo);
    
    // Buscar por pasillo y piso
    List<Ubicacion> findByPasilloAndPisoOrderByNumero(Integer pasillo, Character piso);
    
    // Buscar por pasillo y número (todos los pisos)
    List<Ubicacion> findByPasilloAndNumeroOrderByPiso(Integer pasillo, Integer numero);
    
    boolean existsByCodigoUbicacion(String codigoUbicacion);
}
