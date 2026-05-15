package com.smartlogix.pedidosservice.service;

import com.smartlogix.pedidosservice.dto.PedidoDTO;
import com.smartlogix.pedidosservice.dto.PedidoDetalleDTO;
import com.smartlogix.pedidosservice.dto.ConfirmarItemDTO;
import com.smartlogix.pedidosservice.dto.CrearPedidoDTO;
import com.smartlogix.pedidosservice.dto.HojaPickingDTO;
import com.smartlogix.pedidosservice.dto.HistorialEstadoDTO;
import com.smartlogix.pedidosservice.integration.UbicacionIntegrationService;
import com.smartlogix.pedidosservice.model.DetallePedido;
import com.smartlogix.pedidosservice.model.EstadoPedido;
import com.smartlogix.pedidosservice.model.HistorialEstado;
import com.smartlogix.pedidosservice.model.Pedido;
import com.smartlogix.pedidosservice.model.Vendedor;
import com.smartlogix.pedidosservice.repository.HistorialEstadoRepository;
import com.smartlogix.pedidosservice.repository.PedidoRepository;
import com.smartlogix.pedidosservice.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final HistorialEstadoRepository historialRepository;
    private final UbicacionIntegrationService ubicacionService;
    private final VendedorRepository vendedorRepository;

    // Métodos de consulta

    public List<Pedido> getPedidosPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstadoActual(estado);
    }

    public List<PedidoDTO> getPedidosActivos() {
        return pedidoRepository.findByEstadoActualIn(List.of(
                EstadoPedido.PENDIENTE,
                EstadoPedido.PICKING_ASIGNADO,
                EstadoPedido.EN_PICKING))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> getTodosLosPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> getMisPedidosEnPicking(Integer operadorId) {
        return pedidoRepository.findByOperadorIdAndEstadoActualIn(operadorId, List.of(
                EstadoPedido.PICKING_ASIGNADO,
                EstadoPedido.EN_PICKING))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> getPedidosFacturados() {
        return pedidoRepository.findByEstadoActual(EstadoPedido.FACTURADO)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoDTO> getPedidosEntregados() {
        return pedidoRepository.findByEstadoActual(EstadoPedido.ENTREGADO)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PedidoDTO mapToDTO(Pedido p) {
        return PedidoDTO.builder()
                .id(p.getId())
                .cliente(p.getCliente())
                .fechaCreacion(p.getFechaCreacion())
                .estado(p.getEstadoActual())
                .estadoActual(p.getEstadoActual()) // For React
                .vendedorId(p.getVendedorId())
                .vendedorNombre(p.getVendedorNombre())
                .operadorId(p.getOperadorId())
                .operadorNombre(p.getOperadorNombre())
                .transportistaNombre(p.getTransportistaNombre())
                .fotoEvidencia(p.getFotoEntrega())
                .numeroFactura(p.getNumeroFactura())
                .fechaEntrega(p.getFechaEntrega())
                .totalItems(
                        p.getDetalles() != null
                                ? p.getDetalles().stream()
                                        .mapToInt(
                                                d -> d.getCantidadSolicitada() != null ? d.getCantidadSolicitada() : 0)
                                        .sum()
                                : 0)
                .build();
    }

    public PedidoDetalleDTO getPedidoDetalle(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Build responsables from entity fields + historial
        PedidoDetalleDTO.ResponsableDTO vendedor = PedidoDetalleDTO.ResponsableDTO.builder()
                .id(pedido.getVendedorId())
                .nombre(pedido.getVendedorNombre())
                .build();

        PedidoDetalleDTO.ResponsableDTO operadorPicking = null;
        if (pedido.getOperadorId() != null) {
            operadorPicking = PedidoDetalleDTO.ResponsableDTO.builder()
                    .id(pedido.getOperadorId())
                    .nombre(pedido.getOperadorNombre())
                    .build();
        }

        PedidoDetalleDTO.ResponsableDTO operadorTransporte = null;
        if (pedido.getTransportistaId() != null) {
            operadorTransporte = PedidoDetalleDTO.ResponsableDTO.builder()
                    .id(pedido.getTransportistaId())
                    .nombre(pedido.getTransportistaNombre())
                    .build();
        }

        List<PedidoDetalleDTO.ItemDetalleDTO> detalles = pedido.getDetalles().stream()
                .map(d -> PedidoDetalleDTO.ItemDetalleDTO.builder()
                        .sku(d.getSku())
                        .descripcion(d.getDescripcion())
                        .cantidadSolicitada(d.getCantidadSolicitada())
                        .cantidadPickeada(d.getCantidadPickeada())
                        .ubicacionSugerida(d.getUbicacionSugerida())
                        .build())
                .collect(Collectors.toList());

        List<HistorialEstadoDTO> historial = pedido.getHistorial().stream()
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .map(h -> HistorialEstadoDTO.builder()
                        .estado(h.getEstado().name())
                        .timestamp(h.getTimestamp())
                        .usuarioId(h.getUsuarioId())
                        .usuarioNombre(h.getUsuarioNombre())
                        .build())
                .collect(Collectors.toList());

        return PedidoDetalleDTO.builder()
                .id(pedido.getId())
                .cliente(pedido.getCliente())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaEntrega(pedido.getFechaEntrega())
                .estadoActual(pedido.getEstadoActual().name())
                .numeroFactura(pedido.getNumeroFactura())
                .fotoEntrega(pedido.getFotoEntrega())
                .vendedor(vendedor)
                .operadorPicking(operadorPicking)
                .operadorTransporte(operadorTransporte)
                .detalles(detalles)
                .historial(historial)
                .build();
    }

    public Map<String, Object> getEstadisticas() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        long total = pedidos.size();

        // 1. Pedidos Totales
        long pedidosTotales = total;

        // 2. Distribución de Estados (incluir TODOS los estados, incluso los que tienen
        // 0)
        Map<String, Long> distribucionEstados = new java.util.LinkedHashMap<>();
        for (EstadoPedido estado : EstadoPedido.values()) {
            distribucionEstados.put(estado.name(), 0L);
        }
        pedidos.stream()
                .collect(Collectors.groupingBy(Pedido::getEstadoActual, Collectors.counting()))
                .forEach((estado, count) -> distribucionEstados.put(estado.name(), count));

        // 3. Eficiencia Global: % pedidos ENTREGADOS / total
        long entregados = distribucionEstados.getOrDefault("ENTREGADO", 0L);
        double eficienciaGlobal = total > 0 ? Math.round((double) entregados / total * 10000.0) / 100.0 : 0.0;

        // 4. Promedio Picking (PICKING_ASIGNADO -> PICKING_COMPLETADO) en minutos
        double promedioPickingMinutos = pedidos.stream()
                .filter(p -> p.getHistorial().stream().anyMatch(h -> h.getEstado() == EstadoPedido.PICKING_COMPLETADO))
                .mapToLong(p -> {
                    LocalDateTime start = p.getHistorial().stream()
                            .filter(h -> h.getEstado() == EstadoPedido.PICKING_ASIGNADO)
                            .map(HistorialEstado::getTimestamp)
                            .findFirst().orElse(null);

                    LocalDateTime end = p.getHistorial().stream()
                            .filter(h -> h.getEstado() == EstadoPedido.PICKING_COMPLETADO)
                            .map(HistorialEstado::getTimestamp)
                            .findFirst().orElse(null);

                    if (start != null && end != null) {
                        return java.time.Duration.between(start, end).toMinutes();
                    }
                    return 0;
                })
                .filter(duration -> duration > 0)
                .average()
                .orElse(0.0);

        // 5. Operadores Activos: operadores con pedidos en PICKING_ASIGNADO,
        // EN_PICKING,
        // TRANSPORTE_ASIGNADO o EN_TRANSPORTE
        java.util.Set<Integer> operadoresActivos = new java.util.HashSet<>();
        pedidos.stream()
                .filter(p -> p.getEstadoActual() == EstadoPedido.PICKING_ASIGNADO
                        || p.getEstadoActual() == EstadoPedido.EN_PICKING)
                .filter(p -> p.getOperadorId() != null)
                .forEach(p -> operadoresActivos.add(p.getOperadorId()));
        pedidos.stream()
                .filter(p -> p.getEstadoActual() == EstadoPedido.TRANSPORTE_ASIGNADO
                        || p.getEstadoActual() == EstadoPedido.EN_TRANSPORTE)
                .filter(p -> p.getTransportistaId() != null)
                .forEach(p -> operadoresActivos.add(p.getTransportistaId()));

        // 6. Eficiencia por Operador: para cada operador de picking,
        // % de sus pedidos que llegaron a ENTREGADO vs total asignados
        Map<String, Map<String, Object>> eficienciaPorOperador = new java.util.LinkedHashMap<>();
        // Agrupar pedidos por operador de picking
        Map<Integer, List<Pedido>> pedidosPorOperador = pedidos.stream()
                .filter(p -> p.getOperadorId() != null)
                .collect(Collectors.groupingBy(Pedido::getOperadorId));

        pedidosPorOperador.forEach((operadorId, pedidosOperador) -> {
            String nombre = pedidosOperador.stream()
                    .map(Pedido::getOperadorNombre)
                    .filter(n -> n != null && !n.isEmpty())
                    .findFirst().orElse("Operador #" + operadorId);

            long totalOperador = pedidosOperador.size();
            long entregadosOperador = pedidosOperador.stream()
                    .filter(p -> p.getEstadoActual() == EstadoPedido.ENTREGADO)
                    .count();
            double eficiencia = totalOperador > 0
                    ? Math.round((double) entregadosOperador / totalOperador * 10000.0) / 100.0
                    : 0.0;

            Map<String, Object> datos = new java.util.LinkedHashMap<>();
            datos.put("nombre", nombre);
            datos.put("totalAsignados", totalOperador);
            datos.put("entregados", entregadosOperador);
            datos.put("eficiencia", eficiencia);
            eficienciaPorOperador.put(String.valueOf(operadorId), datos);
        });

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("pedidosTotales", pedidosTotales);
        result.put("eficienciaGlobal", eficienciaGlobal);
        result.put("promedioPickingMinutos", Math.round(promedioPickingMinutos * 100.0) / 100.0);
        result.put("operadoresActivos", operadoresActivos.size());
        result.put("eficienciaPorOperador", eficienciaPorOperador);
        result.put("distribucionEstados", distribucionEstados);
        return result;
    }

    public HojaPickingDTO getHojaPicking(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        return HojaPickingDTO.builder()
                .pedidoId(pedido.getId())
                .cliente(pedido.getCliente())
                .fechaCreacion(pedido.getFechaCreacion())
                .estado(pedido.getEstadoActual().name())
                .items(pedido.getDetalles().stream().map(d -> HojaPickingDTO.ItemPickingDTO.builder()
                        .sku(d.getSku())
                        .descripcion(d.getDescripcion())
                        .cantidadSolicitada(d.getCantidadSolicitada())
                        .cantidadPickeada(d.getCantidadPickeada())
                        .ubicacionSugerida(d.getUbicacionSugerida())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    // Métodos transaccionales (Cambios de estado)

    @Transactional
    public Pedido crearPedido(CrearPedidoDTO dto, Integer userId, String userEmail) {
        String vendedorNombre = "Desconocido";
        if (dto.getVendedorId() != null) {
            vendedorNombre = vendedorRepository.findById(dto.getVendedorId())
                    .map(Vendedor::getNombre)
                    .orElse("Vendedor #" + dto.getVendedorId());
        }

        Pedido pedido = Pedido.builder()
                .cliente(dto.getCliente())
                .fechaCreacion(LocalDateTime.now())
                .vendedorId(dto.getVendedorId())
                .vendedorNombre(vendedorNombre)
                .estadoActual(EstadoPedido.PENDIENTE)
                .build();

        dto.getDetalles().forEach(item -> {
            // Consultar ubicación sugerida si no viene en el DTO
            String ubicacion = item.getUbicacionSugerida();
            if (ubicacion == null || ubicacion.isEmpty()) {
                ubicacion = ubicacionService.obtenerMejorUbicacion(item.getSku()).orElse("SIN_STOCK");
            }

            DetallePedido detalle = DetallePedido.builder()
                    .sku(item.getSku())
                    .descripcion(item.getDescripcion())
                    .cantidadSolicitada(item.getCantidadSolicitada())
                    .cantidadPickeada(0) // Inicialmente 0
                    .ubicacionSugerida(ubicacion)
                    .build();
            pedido.addDetalle(detalle);
        });

        Pedido guardado = pedidoRepository.save(pedido);
        registrarHistorial(guardado, EstadoPedido.PENDIENTE, userId, userEmail);
        return guardado;
    }

    @Transactional
    public PedidoDTO asignarsePicking(Long id, Integer userId, String nombreUsuario) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstadoActual() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("Solo pedidos pendientes pueden ser asignados.");
        }

        pedido.setEstadoActual(EstadoPedido.PICKING_ASIGNADO);
        pedido.setOperadorId(userId);
        pedido.setOperadorNombre(nombreUsuario);

        Pedido guardado = pedidoRepository.save(pedido);
        registrarHistorial(guardado, EstadoPedido.PICKING_ASIGNADO, userId, nombreUsuario);
        return mapToDTO(guardado);
    }

    @Transactional
    public PedidoDTO confirmarItemPicking(Long id, ConfirmarItemDTO confirmacion) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validar Estado: Debe estar en PICKING_ASIGNADO o EN_PICKING
        if (pedido.getEstadoActual() != EstadoPedido.PICKING_ASIGNADO
                && pedido.getEstadoActual() != EstadoPedido.EN_PICKING) {
            throw new RuntimeException("El pedido no está en proceso de picking.");
        }

        // Transición automática a EN_PICKING si es el primer item
        if (pedido.getEstadoActual() == EstadoPedido.PICKING_ASIGNADO) {
            pedido.setEstadoActual(EstadoPedido.EN_PICKING);
        }

        DetallePedido detalle = pedido.getDetalles().stream()
                .filter(d -> d.getSku().equals(confirmacion.getSku()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("SKU no encontrado en el pedido"));

        // VALIDACIÓN DE UBICACIÓN
        if (!detalle.getUbicacionSugerida().equalsIgnoreCase(confirmacion.getCodigoUbicacion())) {
            throw new RuntimeException("Ubicación incorrecta. Sugerida: " + detalle.getUbicacionSugerida());
        }

        // Actualizar cantidad pickeada
        int nuevaCantidad = detalle.getCantidadPickeada() + confirmacion.getCantidad();
        if (nuevaCantidad > detalle.getCantidadSolicitada()) {
            throw new RuntimeException("Cantidad excede lo solicitado.");
        }
        detalle.setCantidadPickeada(nuevaCantidad);

        Pedido guardado = pedidoRepository.save(pedido);
        return mapToDTO(guardado);
    }

    @Transactional
    public PedidoDTO completarPicking(Long id, Integer userId, String nombreUsuario) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validar que todo esté pickeado
        boolean todoCompleto = pedido.getDetalles().stream()
                .allMatch(d -> d.getCantidadPickeada().equals(d.getCantidadSolicitada()));

        if (!todoCompleto) {
            throw new RuntimeException("No se puede completar. Faltan items por pickear.");
        }

        pedido.setEstadoActual(EstadoPedido.PICKING_COMPLETADO);
        Pedido guardado = pedidoRepository.save(pedido);
        registrarHistorial(guardado, EstadoPedido.PICKING_COMPLETADO, userId, nombreUsuario);

        // Log notificación
        log.info("PEDIDO LISTO PARA FACTURACION: #{}", pedido.getId());
        return mapToDTO(guardado);
    }

    @Transactional
    public void registrarFactura(Long id, String numeroFactura, Integer userId, String nombreUsuario) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setNumeroFactura(numeroFactura);
        pedido.setEstadoActual(EstadoPedido.FACTURADO);
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.FACTURADO, userId, nombreUsuario);
    }

    @Transactional
    public void revisionFactura(Long id, Integer userId, String nombreUsuario) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstadoActual() != EstadoPedido.FACTURADO) {
            throw new RuntimeException("Debe estar facturado para revisión.");
        }

        // AQUÍ SE HACE EL EGRESO DE STOCK REAL EN WMS
        pedido.getDetalles().forEach(d -> {
            ubicacionService.egresarStock(
                    d.getSku(),
                    d.getCantidadPickeada(),
                    d.getUbicacionSugerida(),
                    "PEDIDO #" + pedido.getId());
        });

        pedido.setEstadoActual(EstadoPedido.REVISION_FACTURA);
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.REVISION_FACTURA, userId, nombreUsuario);
    }

    @Transactional
    public void asignarTransporte(Long id, Integer userId, String nombreUsuario) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setTransportistaId(userId);
        pedido.setTransportistaNombre(nombreUsuario);
        pedido.setEstadoActual(EstadoPedido.TRANSPORTE_ASIGNADO);
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.TRANSPORTE_ASIGNADO, userId, nombreUsuario);
    }

    @Transactional
    public void iniciarTransporte(Long id, Integer userId, String nombreUsuario) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstadoActual(EstadoPedido.EN_TRANSPORTE);
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.EN_TRANSPORTE, userId, nombreUsuario);
    }

    @Transactional
    public void finalizarEntrega(Long id, String fotoUrl, Integer userId, String nombreUsuario) {
        log.info("Iniciando finalizarEntrega para pedido #{} - Usuario: {} (ID: {})", id, nombreUsuario, userId);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error: Pedido #{} no encontrado", id);
                    return new RuntimeException("Pedido no encontrado");
                });

        log.debug("Estado actual del pedido #{}: {}", id, pedido.getEstadoActual());

        pedido.setFotoEntrega(fotoUrl);
        pedido.setEstadoActual(EstadoPedido.ENTREGADO);
        pedido.setFechaEntrega(LocalDateTime.now());
        pedidoRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.ENTREGADO, userId, nombreUsuario);
        log.info("Pedido #{} finalizado exitosamente por {}", id, nombreUsuario);
    }

    private void registrarHistorial(Pedido pedido, EstadoPedido estado, Integer userId, String usuarioNombre) {
        HistorialEstado historial = HistorialEstado.builder()
                .estado(estado)
                .timestamp(LocalDateTime.now())
                .usuarioId(userId)
                .usuarioNombre(usuarioNombre)
                .build();
        pedido.addHistorial(historial);
        historialRepository.save(historial);
    }
}

