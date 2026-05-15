package com.smartlogix.ubicacionesservice.repository;

import com.smartlogix.ubicacionesservice.model.ProductoUbicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoUbicacionRepository extends JpaRepository<ProductoUbicacion, Integer> {
    
    Optional<ProductoUbicacion> findByProductoSkuAndUbicacionIdUbicacion(String sku, Integer idUbicacion);
    
    @Query("SELECT COALESCE(SUM(pu.cantidadEnUbicacion), 0) FROM ProductoUbicacion pu WHERE pu.producto.sku = :sku")
    Integer sumCantidadByProductoSku(@Param("sku") String sku);
}
