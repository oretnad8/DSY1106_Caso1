package com.smartlogix.ubicacionesservice.service;

import com.smartlogix.ubicacionesservice.dto.AsignarProductoRequest;
import com.smartlogix.ubicacionesservice.dto.AsignarProductoResponse;
import com.smartlogix.ubicacionesservice.dto.EgresoProductoRequest;
import com.smartlogix.ubicacionesservice.dto.EgresoProductoResponse;
import com.smartlogix.ubicacionesservice.dto.ReubicarProductoRequest;
import com.smartlogix.ubicacionesservice.dto.ReubicarProductoResponse;
import com.smartlogix.ubicacionesservice.dto.UbicacionResponse;
import com.smartlogix.ubicacionesservice.exception.ProductoNotFoundException;
import com.smartlogix.ubicacionesservice.exception.StockInsuficienteException;
import com.smartlogix.ubicacionesservice.exception.UbicacionNotFoundException;
import com.smartlogix.ubicacionesservice.model.Producto;
import com.smartlogix.ubicacionesservice.model.ProductoUbicacion;
import com.smartlogix.ubicacionesservice.model.Ubicacion;
import com.smartlogix.ubicacionesservice.repository.ProductoRepository;
import com.smartlogix.ubicacionesservice.repository.ProductoUbicacionRepository;
import com.smartlogix.ubicacionesservice.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UbicacionService {

        private final UbicacionRepository ubicacionRepository;
        private final ProductoRepository productoRepository;
        private final ProductoUbicacionRepository productoUbicacionRepository;

        public List<UbicacionResponse> getAllUbicaciones() {
                return ubicacionRepository.findAll().stream()
                                .map(UbicacionResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        public List<UbicacionResponse> getUbicacionesByPiso(Character piso) {
                validarPiso(piso);
                return ubicacionRepository.findByPisoOrderByPasilloAscNumeroAsc(piso).stream()
                                .map(UbicacionResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        public List<UbicacionResponse> getUbicacionesByPasillo(Integer pasillo) {
                validarPasillo(pasillo);
                return ubicacionRepository.findByPasilloOrderByPisoAscNumeroAsc(pasillo).stream()
                                .map(UbicacionResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        public List<UbicacionResponse> getUbicacionesByPasilloPiso(Integer pasillo, Character piso) {
                validarPasillo(pasillo);
                validarPiso(piso);
                return ubicacionRepository.findByPasilloAndPisoOrderByNumero(pasillo, piso).stream()
                                .map(UbicacionResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        public List<UbicacionResponse> getUbicacionesByPasilloNumero(Integer pasillo, Integer numero) {
                validarPasillo(pasillo);
                validarNumero(numero);
                return ubicacionRepository.findByPasilloAndNumeroOrderByPiso(pasillo, numero).stream()
                                .map(UbicacionResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        public UbicacionResponse getUbicacionByCodigo(String codigo) {
                Ubicacion ubicacion = ubicacionRepository.findByCodigoUbicacion(codigo.toUpperCase())
                                .orElseThrow(() -> new UbicacionNotFoundException(
                                                "Ubicación no encontrada: " + codigo));
                return UbicacionResponse.fromEntity(ubicacion);
        }

        public UbicacionResponse getUbicacionById(Integer id) {
                Ubicacion ubicacion = ubicacionRepository.findById(id)
                                .orElseThrow(() -> new UbicacionNotFoundException(
                                                "Ubicación no encontrada con ID: " + id));
                return UbicacionResponse.fromEntity(ubicacion);
        }

        @Transactional
        public AsignarProductoResponse asignarProducto(AsignarProductoRequest request) {
                String sku = request.getSku().toUpperCase();
                String codigoUbicacion = request.getCodigoUbicacion().toUpperCase();

                // Validar formato de código de ubicación (P1-A-01 a P5-C-60)
                validarFormatoCodigo(codigoUbicacion);

                // Verificar que el producto existe
                Producto producto = productoRepository.findById(sku)
                                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado: " + sku));

                // Verificar que la ubicación existe
                Ubicacion ubicacion = ubicacionRepository.findByCodigoUbicacion(codigoUbicacion)
                                .orElseThrow(() -> new UbicacionNotFoundException(
                                                "Ubicación no encontrada: " + codigoUbicacion));

                // Verificar stock disponible
                Integer cantidadActualmenteAsignada = productoUbicacionRepository.sumCantidadByProductoSku(sku);
                if (cantidadActualmenteAsignada == null) {
                        cantidadActualmenteAsignada = 0;
                }

                Integer stockDisponible = producto.getStock() - cantidadActualmenteAsignada;

                if (stockDisponible < request.getCantidad()) {
                        throw new StockInsuficienteException(
                                        String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                                                        stockDisponible, request.getCantidad()));
                }

                // Buscar si ya existe una asignación en esta ubicación
                ProductoUbicacion productoUbicacion = productoUbicacionRepository
                                .findByProductoSkuAndUbicacionIdUbicacion(sku, ubicacion.getIdUbicacion())
                                .orElse(null);

                if (productoUbicacion != null) {
                        // Ya existe, incrementar cantidad
                        productoUbicacion.setCantidadEnUbicacion(
                                        productoUbicacion.getCantidadEnUbicacion() + request.getCantidad());
                } else {
                        // Nueva asignación
                        productoUbicacion = new ProductoUbicacion();
                        productoUbicacion.setProducto(producto);
                        productoUbicacion.setUbicacion(ubicacion);
                        productoUbicacion.setCantidadEnUbicacion(request.getCantidad());
                }

                productoUbicacionRepository.save(productoUbicacion);

                // Incrementar stock del producto (INGRESO)
                producto.setStock(producto.getStock() + request.getCantidad());
                productoRepository.save(producto);

                Integer nuevoStockAsignado = productoUbicacionRepository.sumCantidadByProductoSku(sku);
                Integer nuevoStockDisponible = producto.getStock() - nuevoStockAsignado;

                log.info("Producto {} asignado a ubicación {}. Cantidad: {}. Nuevo stock: {}", sku, codigoUbicacion,
                                request.getCantidad(), producto.getStock());

                return AsignarProductoResponse.builder()
                                .mensaje("Producto asignado exitosamente")
                                .sku(sku)
                                .codigoUbicacion(codigoUbicacion)
                                .cantidadAsignada(request.getCantidad())
                                .stockRestante(nuevoStockDisponible)
                                .build();
        }

        // Validaciones
        private void validarPiso(Character piso) {
                if (piso != 'A' && piso != 'B' && piso != 'C') {
                        throw new IllegalArgumentException("Piso debe ser A, B o C");
                }
        }

        private void validarPasillo(Integer pasillo) {
                if (pasillo < 1 || pasillo > 5) {
                        throw new IllegalArgumentException("Pasillo debe estar entre 1 y 5");
                }
        }

        private void validarNumero(Integer numero) {
                if (numero < 1 || numero > 60) {
                        throw new IllegalArgumentException("Número de posición debe estar entre 1 y 60");
                }
        }

        private void validarFormatoCodigo(String codigo) {
                // Formato esperado: Standard P1-A-01 a P5-C-60 o Special Shelf P3-A/EST-31,1
                boolean isStandard = codigo.matches("^P[1-5]-[ABC]-(?:0[1-9]|[1-5][0-9]|60)$");
                boolean isShelf = codigo.matches("^P3-A/EST-(?:[1-9]|[1-5][0-9]|60),(?:[1-9]|10)$");

                if (!isStandard && !isShelf) {
                        throw new IllegalArgumentException(
                                        "Formato de código inválido. Debe ser: P[1-5]-[A|B|C]-[01-60] o P3-A/EST-[Numero],[Nivel]");
                }
        }

        // Helper para buscar ProductoUbicacion con reintento para SKUs con espacios
        // (legacy data)
        private ProductoUbicacion findProductoUbicacionWithRetry(String sku, Integer idUbicacion,
                        String codigoUbicacion) {
                return productoUbicacionRepository
                                .findByProductoSkuAndUbicacionIdUbicacion(sku, idUbicacion)
                                .orElseGet(() -> {
                                        // Intento fallback: buscar con espacio al final (legacy data)
                                        return productoUbicacionRepository
                                                        .findByProductoSkuAndUbicacionIdUbicacion(sku + " ",
                                                                        idUbicacion)
                                                        .orElseThrow(() -> new RuntimeException(
                                                                        String.format("El producto %s no se encuentra en la ubicación %s",
                                                                                        sku,
                                                                                        codigoUbicacion)));
                                });
        }

        @Transactional
        public EgresoProductoResponse egresoProducto(EgresoProductoRequest request) {
                String sku = request.getSku().trim().toUpperCase();
                String codigoUbicacion = request.getCodigoUbicacion().trim().toUpperCase();

                // Validar formato de código de ubicación
                validarFormatoCodigo(codigoUbicacion);

                // Verificar que el producto existe (Buscamos trimmed)
                Producto producto = productoRepository.findById(sku)
                                .orElseGet(() -> productoRepository.findById(sku + " ") // Fallback producto
                                                .orElseThrow(() -> new ProductoNotFoundException(
                                                                "Producto no encontrado: " + sku)));

                // Verificar que la ubicación existe
                Ubicacion ubicacion = ubicacionRepository.findByCodigoUbicacion(codigoUbicacion)
                                .orElseThrow(() -> new UbicacionNotFoundException(
                                                "Ubicación no encontrada: " + codigoUbicacion));

                // Buscar la asignación en esta ubicación (Usando helper con retry)
                ProductoUbicacion productoUbicacion = findProductoUbicacionWithRetry(sku, ubicacion.getIdUbicacion(),
                                codigoUbicacion);

                // Verificar que hay suficiente cantidad en la ubicación
                if (productoUbicacion.getCantidadEnUbicacion() < request.getCantidad()) {
                        throw new StockInsuficienteException(
                                        String.format("Cantidad insuficiente en ubicación. Disponible: %d, Solicitado: %d",
                                                        productoUbicacion.getCantidadEnUbicacion(),
                                                        request.getCantidad()));
                }

                // Reducir cantidad en ubicación
                productoUbicacion.setCantidadEnUbicacion(
                                productoUbicacion.getCantidadEnUbicacion() - request.getCantidad());

                // Si la cantidad llega a 0, eliminar la asignación
                if (productoUbicacion.getCantidadEnUbicacion() == 0) {
                        productoUbicacionRepository.delete(productoUbicacion);
                } else {
                        productoUbicacionRepository.save(productoUbicacion);
                }

                // Reducir stock del producto
                producto.setStock(producto.getStock() - request.getCantidad());
                productoRepository.save(producto);

                Integer cantidadRestante = productoUbicacion.getCantidadEnUbicacion() == 0 ? 0
                                : productoUbicacion.getCantidadEnUbicacion();

                log.info("Egreso de producto {} desde ubicación {}. Cantidad: {}", sku, codigoUbicacion,
                                request.getCantidad());

                return EgresoProductoResponse.builder()
                                .mensaje("Egreso registrado exitosamente")
                                .sku(sku)
                                .codigoUbicacion(codigoUbicacion)
                                .cantidadRetirada(request.getCantidad())
                                .stockProducto(producto.getStock())
                                .cantidadRestanteEnUbicacion(cantidadRestante)
                                .build();
        }

        @Transactional
        public ReubicarProductoResponse reubicarProducto(ReubicarProductoRequest request) {
                String sku = request.getSku().trim().toUpperCase();
                String codigoOrigen = request.getCodigoUbicacionOrigen().trim().toUpperCase();
                String codigoDestino = request.getCodigoUbicacionDestino().trim().toUpperCase();

                // Validar que origen y destino sean diferentes
                if (codigoOrigen.equals(codigoDestino)) {
                        throw new IllegalArgumentException("La ubicación de origen y destino deben ser diferentes");
                }

                // Validar formatos
                validarFormatoCodigo(codigoOrigen);
                validarFormatoCodigo(codigoDestino);

                // Verificar que el producto existe
                Producto producto = productoRepository.findById(sku)
                                .orElseGet(() -> productoRepository.findById(sku + " ")
                                                .orElseThrow(() -> new ProductoNotFoundException(
                                                                "Producto no encontrado: " + sku)));

                // Verificar que las ubicaciones existen
                Ubicacion ubicacionOrigen = ubicacionRepository.findByCodigoUbicacion(codigoOrigen)
                                .orElseThrow(() -> new UbicacionNotFoundException(
                                                "Ubicación origen no encontrada: " + codigoOrigen));

                Ubicacion ubicacionDestino = ubicacionRepository.findByCodigoUbicacion(codigoDestino)
                                .orElseThrow(() -> new UbicacionNotFoundException(
                                                "Ubicación destino no encontrada: " + codigoDestino));

                // Buscar la asignación en ubicación origen (Helper con retry)
                ProductoUbicacion productoUbicacionOrigen = findProductoUbicacionWithRetry(sku,
                                ubicacionOrigen.getIdUbicacion(), codigoOrigen);

                // Verificar que hay suficiente cantidad en origen
                if (productoUbicacionOrigen.getCantidadEnUbicacion() < request.getCantidad()) {
                        throw new StockInsuficienteException(
                                        String.format("Cantidad insuficiente en ubicación origen. Disponible: %d, Solicitado: %d",
                                                        productoUbicacionOrigen.getCantidadEnUbicacion(),
                                                        request.getCantidad()));
                }

                // Reducir cantidad en origen
                productoUbicacionOrigen.setCantidadEnUbicacion(
                                productoUbicacionOrigen.getCantidadEnUbicacion() - request.getCantidad());

                Integer cantidadRestanteOrigen;
                // Si la cantidad en origen llega a 0, eliminar la asignación
                if (productoUbicacionOrigen.getCantidadEnUbicacion() == 0) {
                        productoUbicacionRepository.delete(productoUbicacionOrigen);
                        cantidadRestanteOrigen = 0;
                } else {
                        productoUbicacionRepository.save(productoUbicacionOrigen);
                        cantidadRestanteOrigen = productoUbicacionOrigen.getCantidadEnUbicacion();
                }

                // Buscar o crear asignación en destino
                // Para destino, preferimos SKU limpio si es nuevo, pero respetamos si ya existe
                // el sucio
                ProductoUbicacion productoUbicacionDestino = productoUbicacionRepository
                                .findByProductoSkuAndUbicacionIdUbicacion(sku, ubicacionDestino.getIdUbicacion())
                                .orElseGet(() -> productoUbicacionRepository
                                                .findByProductoSkuAndUbicacionIdUbicacion(sku + " ",
                                                                ubicacionDestino.getIdUbicacion())
                                                .orElse(null));

                if (productoUbicacionDestino != null) {
                        // Ya existe, incrementar cantidad
                        productoUbicacionDestino.setCantidadEnUbicacion(
                                        productoUbicacionDestino.getCantidadEnUbicacion() + request.getCantidad());
                } else {
                        // Nueva asignación en destino (Siempre con SKU limpio si es nuevo)
                        productoUbicacionDestino = new ProductoUbicacion();
                        productoUbicacionDestino.setProducto(producto); // Usa el producto encontrado (que puede tener
                                                                        // SKU con
                                                                        // espacio, ojo)
                        productoUbicacionDestino.setUbicacion(ubicacionDestino);
                        productoUbicacionDestino.setCantidadEnUbicacion(request.getCantidad());
                }

                productoUbicacionRepository.save(productoUbicacionDestino);

                log.info("Producto {} reubicado de {} a {}. Cantidad: {}",
                                sku, codigoOrigen, codigoDestino, request.getCantidad());

                return ReubicarProductoResponse.builder()
                                .mensaje("Reubicación realizada exitosamente")
                                .sku(sku)
                                .codigoUbicacionOrigen(codigoOrigen)
                                .codigoUbicacionDestino(codigoDestino)
                                .cantidadMovida(request.getCantidad())
                                .cantidadRestanteEnOrigen(cantidadRestanteOrigen)
                                .cantidadEnDestino(productoUbicacionDestino.getCantidadEnUbicacion())
                                .build();
        }
}
