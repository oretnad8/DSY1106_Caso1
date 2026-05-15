package com.smartlogix.productosservice.repository;

import com.smartlogix.productosservice.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
    
    @Query("SELECT DISTINCT p FROM Producto p " +
           "LEFT JOIN p.ubicaciones pu " +
           "LEFT JOIN pu.ubicacion u " +
           "WHERE LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.codigoBarrasIndividual) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.lpn) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.lpnDesc) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.codigoUbicacion) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Producto> searchProductos(@Param("query") String query);
    

    List<Producto> findByVencimientoCercanoTrue();
}
