package com.smartlogix.productosservice.service;

import com.smartlogix.productosservice.dto.ProductoRequest;
import com.smartlogix.productosservice.dto.ProductoResponse;
import com.smartlogix.productosservice.dto.ProductoUpdateRequest;
import com.smartlogix.productosservice.exception.ProductoNotFoundException;
import com.smartlogix.productosservice.exception.SkuAlreadyExistsException;
import com.smartlogix.productosservice.model.Producto;
import com.smartlogix.productosservice.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {
    
    private final ProductoRepository productoRepository;
    
    public List<ProductoResponse> searchProductos(String query) {
        List<Producto> productos = productoRepository.searchProductos(query);
        return productos.stream()
                .map(ProductoResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public ProductoResponse getProductoBySku(String sku) {
        Producto producto = productoRepository.findById(sku.toUpperCase())
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con SKU: " + sku));
        return ProductoResponse.fromEntity(producto);
    }
    
    @Transactional
    public ProductoResponse createProducto(ProductoRequest request) {
        String sku = request.getSku().toUpperCase();
        
        if (productoRepository.existsById(sku)) {
            throw new SkuAlreadyExistsException("Ya existe un producto con el SKU: " + sku);
        }
        
        Producto producto = new Producto();
        producto.setSku(sku);
        producto.setDescripcion(request.getDescripcion());
        producto.setStock(request.getStock());
        producto.setCodigoBarrasIndividual(request.getCodigoBarrasIndividual());
        producto.setLpn(request.getLpn());
        producto.setLpnDesc(request.getLpnDesc());
        producto.setFechaVencimiento(request.getFechaVencimiento());
        producto.calcularVencimientoCercano();
        
        Producto savedProducto = productoRepository.save(producto);
        log.info("Producto creado: {}", savedProducto.getSku());
        return ProductoResponse.fromEntity(savedProducto);
    }
    
    @Transactional
    public ProductoResponse updateProducto(String sku, ProductoUpdateRequest request) {
        Producto producto = productoRepository.findById(sku.toUpperCase())
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con SKU: " + sku));
        
        if (request.getDescripcion() != null) {
            producto.setDescripcion(request.getDescripcion());
        }
        
        if (request.getStock() != null) {
            producto.setStock(request.getStock());
        }
        
        if (request.getCodigoBarrasIndividual() != null) {
            producto.setCodigoBarrasIndividual(request.getCodigoBarrasIndividual());
        }
        
        if (request.getLpn() != null) {
            producto.setLpn(request.getLpn());
        }
        
        if (request.getLpnDesc() != null) {
            producto.setLpnDesc(request.getLpnDesc());
        }
        
        if (request.getFechaVencimiento() != null) {
            producto.setFechaVencimiento(request.getFechaVencimiento());
        }
        
        producto.calcularVencimientoCercano();
        
        Producto updatedProducto = productoRepository.save(producto);
        log.info("Producto actualizado: {}", updatedProducto.getSku());
        return ProductoResponse.fromEntity(updatedProducto);
    }
    
    @Transactional
    public void deleteProducto(String sku) {
        Producto producto = productoRepository.findById(sku.toUpperCase())
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con SKU: " + sku));
        
        productoRepository.delete(producto);
        log.info("Producto eliminado: {}", sku);
    }
    
    // Tarea programada para actualizar vencimientos cercanos cada día a las 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void actualizarVencimientosCercanos() {
        log.info("Iniciando actualización de vencimientos cercanos...");
        List<Producto> productos = productoRepository.findAll();
        
        productos.forEach(producto -> {
            producto.calcularVencimientoCercano();
            productoRepository.save(producto);
        });
        
        long vencimientosCercanos = productos.stream()
                .filter(Producto::getVencimientoCercano)
                .count();
        
        log.info("Actualización completada. {} productos con vencimiento cercano", vencimientosCercanos);
    }
}
