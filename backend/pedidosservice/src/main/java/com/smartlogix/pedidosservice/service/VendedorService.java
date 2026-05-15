package com.smartlogix.pedidosservice.service;

import com.smartlogix.pedidosservice.model.Vendedor;
import com.smartlogix.pedidosservice.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendedorService {

    private final VendedorRepository vendedorRepository;

    public List<Vendedor> listarTodos() {
        return vendedorRepository.findAll();
    }

    public List<Vendedor> listarActivos() {
        return vendedorRepository.findByActivoTrue();
    }

    public Vendedor obtenerPorId(Integer id) {
        return vendedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado con ID: " + id));
    }

    @Transactional
    public Vendedor crear(Vendedor vendedor) {
        return vendedorRepository.save(vendedor);
    }

    @Transactional
    public Vendedor actualizar(Integer id, Vendedor vendedorDetalles) {
        Vendedor vendedor = obtenerPorId(id);
        vendedor.setNombre(vendedorDetalles.getNombre());
        if (vendedorDetalles.getActivo() != null) {
            vendedor.setActivo(vendedorDetalles.getActivo());
        }
        return vendedorRepository.save(vendedor);
    }

    @Transactional
    public void eliminar(Integer id) {
        Vendedor vendedor = obtenerPorId(id);
        vendedor.setActivo(false); // Eliminación lógica
        vendedorRepository.save(vendedor);
    }
}

