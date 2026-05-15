package com.smartlogix.pedidosservice.controller;

import com.smartlogix.pedidosservice.model.Vendedor;
import com.smartlogix.pedidosservice.service.VendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
@RequiredArgsConstructor
public class VendedorController {

    private final VendedorService vendedorService;

    @GetMapping
    public List<Vendedor> listar(@RequestParam(required = false) Boolean soloActivos) {
        if (Boolean.TRUE.equals(soloActivos)) {
            return vendedorService.listarActivos();
        }
        return vendedorService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendedor> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(vendedorService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Vendedor> crear(@RequestBody Vendedor vendedor) {
        return ResponseEntity.ok(vendedorService.crear(vendedor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vendedor> actualizar(@PathVariable Integer id, @RequestBody Vendedor vendedor) {
        return ResponseEntity.ok(vendedorService.actualizar(id, vendedor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        vendedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

