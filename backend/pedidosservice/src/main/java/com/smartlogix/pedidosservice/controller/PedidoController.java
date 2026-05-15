package com.smartlogix.pedidosservice.controller;

import com.smartlogix.pedidosservice.dto.*;
import com.smartlogix.pedidosservice.model.EstadoPedido;
import com.smartlogix.pedidosservice.model.Pedido;
import com.smartlogix.pedidosservice.service.PedidoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private final PedidoService pedidoService;

    // Helper to get user info from request attributes (set by JwtFilter)
    private Integer getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null)
            return 0; // Or throw exception
        return (Integer) userId;
    }

    private String getUserEmail(HttpServletRequest request) {
        Object email = request.getAttribute("userEmail");
        return email != null ? (String) email : "system";
    }

    private String getUserName(HttpServletRequest request) {
        Object nombre = request.getAttribute("userName");
        return nombre != null ? (String) nombre : getUserEmail(request);
    }

    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@Valid @RequestBody CrearPedidoDTO dto, HttpServletRequest request) {
        log.debug("Recibiendo CrearPedidoDTO: {}", dto);
        return ResponseEntity.ok(pedidoService.crearPedido(dto, getUserId(request), getUserEmail(request)));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<PedidoDTO>> obtenerPendientes() {
        return ResponseEntity.ok(pedidoService.getPedidosActivos());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<PedidoDTO>> obtenerTodos() {
        return ResponseEntity.ok(pedidoService.getTodosLosPedidos());
    }

    @GetMapping("/picking")
    public ResponseEntity<List<PedidoDTO>> obtenerEnPicking(HttpServletRequest request) {
        return ResponseEntity.ok(pedidoService.getMisPedidosEnPicking(getUserId(request)));
    }

    @GetMapping("/facturados")
    public ResponseEntity<List<PedidoDTO>> obtenerFacturados() {
        return ResponseEntity.ok(pedidoService.getPedidosFacturados());
    }

    @GetMapping("/entregados")
    public ResponseEntity<List<PedidoDTO>> obtenerEntregados() {
        return ResponseEntity.ok(pedidoService.getPedidosEntregados());
    }

    @PostMapping("/{id}/asignar-picking")
    public ResponseEntity<PedidoDTO> asignarPicking(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(pedidoService.asignarsePicking(id, getUserId(request), getUserName(request)));
    }

    @GetMapping("/{id}/hoja-picking")
    public ResponseEntity<HojaPickingDTO> obtenerHojaPicking(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.getHojaPicking(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDetalleDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.getPedidoDetalle(id));
    }

    @PostMapping("/{id}/confirmar-item")
    public ResponseEntity<PedidoDTO> confirmarItem(@PathVariable Long id, @RequestBody ConfirmarItemDTO dto) {
        return ResponseEntity.ok(pedidoService.confirmarItemPicking(id, dto));
    }

    @PutMapping("/{id}/completar-picking")
    public ResponseEntity<PedidoDTO> completarPicking(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(pedidoService.completarPicking(id, getUserId(request), getUserName(request)));
    }

    @PutMapping("/{id}/registrar-factura")
    public ResponseEntity<Void> registrarFactura(@PathVariable Long id, @RequestBody RegistrarFacturaDTO dto,
            HttpServletRequest request) {
        pedidoService.registrarFactura(id, dto.getNumeroFactura(), getUserId(request), getUserName(request));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/revision-factura")
    public ResponseEntity<Void> revisionFactura(@PathVariable Long id, HttpServletRequest request) {
        pedidoService.revisionFactura(id, getUserId(request), getUserName(request));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/asignar-transporte")
    public ResponseEntity<Void> asignarTransporte(@PathVariable Long id, HttpServletRequest request) {
        pedidoService.asignarTransporte(id, getUserId(request), getUserName(request));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/iniciar-transporte")
    public ResponseEntity<Void> iniciarTransporte(@PathVariable Long id, HttpServletRequest request) {
        pedidoService.iniciarTransporte(id, getUserId(request), getUserName(request));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/finalizar-entrega")
    public ResponseEntity<Void> finalizarEntrega(@PathVariable Long id, @RequestBody FinalizarEntregaRequestDTO dto,
            HttpServletRequest request) {
        pedidoService.finalizarEntrega(id, dto.getFotoEvidencia(), getUserId(request), getUserName(request));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        return ResponseEntity.ok(pedidoService.getEstadisticas());
    }
}

