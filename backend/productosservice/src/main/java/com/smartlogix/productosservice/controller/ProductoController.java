package com.smartlogix.productosservice.controller;

import com.smartlogix.productosservice.dto.ProductoRequest;
import com.smartlogix.productosservice.dto.ProductoResponse;
import com.smartlogix.productosservice.dto.ProductoUpdateRequest;
import com.smartlogix.productosservice.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    
    private final ProductoService productoService;
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductoResponse>> searchProductos(
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(productoService.searchProductos(q));
    }
    
    @GetMapping("/{sku}")
    public ResponseEntity<ProductoResponse> getProductoBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productoService.getProductoBySku(sku));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'JEFE', 'SUPERVISOR')")
    public ResponseEntity<ProductoResponse> createProducto(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.createProducto(request));
    }
    
    @PutMapping("/{sku}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'JEFE', 'SUPERVISOR')")
    public ResponseEntity<ProductoResponse> updateProducto(
            @PathVariable String sku,
            @Valid @RequestBody ProductoUpdateRequest request) {
        return ResponseEntity.ok(productoService.updateProducto(sku, request));
    }
    
    @DeleteMapping("/{sku}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'JEFE')")
    public ResponseEntity<Void> deleteProducto(@PathVariable String sku) {
        productoService.deleteProducto(sku);
        return ResponseEntity.noContent().build();
    }
}
