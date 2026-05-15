package com.smartlogix.ubicacionesservice.repository;

import com.smartlogix.ubicacionesservice.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {
}
