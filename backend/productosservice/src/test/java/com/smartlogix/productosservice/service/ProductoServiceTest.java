package com.smartlogix.productosservice.service;

import com.smartlogix.productosservice.dto.ProductoRequest;
import com.smartlogix.productosservice.dto.ProductoResponse;
import com.smartlogix.productosservice.dto.ProductoUpdateRequest;
import com.smartlogix.productosservice.exception.ProductoNotFoundException;
import com.smartlogix.productosservice.exception.SkuAlreadyExistsException;
import com.smartlogix.productosservice.model.Producto;
import com.smartlogix.productosservice.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoRequest productoRequest;
    private ProductoUpdateRequest productoUpdateRequest;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setSku("CA30001");
        producto.setDescripcion("Caja de cartón mediana");
        producto.setStock(100);
        producto.setCodigoBarrasIndividual("7701234567890");
        producto.setLpn("LPN12345");
        producto.setLpnDesc("Lote principal");
        producto.setFechaVencimiento(LocalDate.now().plusMonths(3));
        producto.setUbicaciones(new ArrayList<>());
        producto.calcularVencimientoCercano();

        productoRequest = new ProductoRequest(
                "CA30001",
                "Caja de cartón mediana",
                100,
                "7701234567890",
                "LPN12345",
                "Lote principal",
                LocalDate.now().plusMonths(3)
        );

        productoUpdateRequest = new ProductoUpdateRequest(
                "Caja de cartón grande",
                200,
                "7701234567891",
                "LPN12346",
                "Lote secundario",
                LocalDate.now().plusDays(15) // Vencimiento cercano (< 2 meses)
        );
    }

    @Test
    void searchProductos_shouldReturnList() {
        when(productoRepository.searchProductos("Caja")).thenReturn(Collections.singletonList(producto));

        List<ProductoResponse> result = productoService.searchProductos("Caja");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CA30001", result.get(0).getSku());
        verify(productoRepository, times(1)).searchProductos("Caja");
    }

    @Test
    void getProductoBySku_whenExists_shouldReturnProducto() {
        when(productoRepository.findById("CA30001")).thenReturn(Optional.of(producto));

        ProductoResponse result = productoService.getProductoBySku("ca30001");

        assertNotNull(result);
        assertEquals("CA30001", result.getSku());
        verify(productoRepository, times(1)).findById("CA30001");
    }

    @Test
    void getProductoBySku_whenNotExists_shouldThrowException() {
        when(productoRepository.findById("CA30002")).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            productoService.getProductoBySku("CA30002");
        });
        verify(productoRepository, times(1)).findById("CA30002");
    }

    @Test
    void createProducto_whenSkuDoesNotExist_shouldCreateProducto() {
        when(productoRepository.existsById("CA30001")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        ProductoResponse result = productoService.createProducto(productoRequest);

        assertNotNull(result);
        assertEquals("CA30001", result.getSku());
        verify(productoRepository, times(1)).existsById("CA30001");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void createProducto_whenSkuExists_shouldThrowException() {
        when(productoRepository.existsById("CA30001")).thenReturn(true);

        assertThrows(SkuAlreadyExistsException.class, () -> {
            productoService.createProducto(productoRequest);
        });
        verify(productoRepository, times(1)).existsById("CA30001");
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void updateProducto_whenExists_shouldUpdateProducto() {
        when(productoRepository.findById("CA30001")).thenReturn(Optional.of(producto));
        // Configurar comportamiento para guardar y retornar el producto modificado
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoResponse result = productoService.updateProducto("CA30001", productoUpdateRequest);

        assertNotNull(result);
        assertEquals("Caja de cartón grande", result.getDescripcion());
        assertEquals(200, result.getStock());
        assertEquals("7701234567891", result.getCodigoBarrasIndividual());
        assertEquals("LPN12346", result.getLpn());
        assertEquals("Lote secundario", result.getLpnDesc());
        assertTrue(result.getVencimientoCercano()); // Vencimiento de 15 días es cercano (< 2 meses)
        verify(productoRepository, times(1)).findById("CA30001");
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void updateProducto_whenNotExists_shouldThrowException() {
        when(productoRepository.findById("CA30001")).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            productoService.updateProducto("CA30001", productoUpdateRequest);
        });
        verify(productoRepository, times(1)).findById("CA30001");
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void deleteProducto_whenExists_shouldDelete() {
        when(productoRepository.findById("CA30001")).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).delete(producto);

        assertDoesNotThrow(() -> {
            productoService.deleteProducto("CA30001");
        });
        verify(productoRepository, times(1)).findById("CA30001");
        verify(productoRepository, times(1)).delete(producto);
    }

    @Test
    void deleteProducto_whenNotExists_shouldThrowException() {
        when(productoRepository.findById("CA30001")).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            productoService.deleteProducto("CA30001");
        });
        verify(productoRepository, times(1)).findById("CA30001");
        verify(productoRepository, never()).delete(any(Producto.class));
    }

    @Test
    void actualizarVencimientosCercanos_shouldUpdateAll() {
        List<Producto> productos = new ArrayList<>();
        productos.add(producto);
        when(productoRepository.findAll()).thenReturn(productos);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            productoService.actualizarVencimientosCercanos();
        });

        verify(productoRepository, times(1)).findAll();
        verify(productoRepository, times(1)).save(any(Producto.class));
    }
}
