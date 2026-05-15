package com.smartlogix.ubicacionesservice.controller;

import com.smartlogix.ubicacionesservice.dto.AsignarProductoRequest;
import com.smartlogix.ubicacionesservice.dto.AsignarProductoResponse;
import com.smartlogix.ubicacionesservice.dto.EgresoProductoRequest;
import com.smartlogix.ubicacionesservice.dto.EgresoProductoResponse;
import com.smartlogix.ubicacionesservice.dto.ReubicarProductoRequest;
import com.smartlogix.ubicacionesservice.dto.ReubicarProductoResponse;
import com.smartlogix.ubicacionesservice.dto.UbicacionResponse;
import com.smartlogix.ubicacionesservice.service.UbicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
@Slf4j
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Pong from UbicacionController");
    }

    @GetMapping
    public ResponseEntity<List<UbicacionResponse>> getUbicaciones(
            @RequestParam(required = false) Character piso,
            @RequestParam(required = false) Integer pasillo,
            @RequestParam(required = false) Boolean esEstante,
            @RequestParam(required = false) Integer nivel) {

        List<UbicacionResponse> response = ubicacionService.getAllUbicaciones();

        if (pasillo != null) {
            response = response.stream().filter(u -> pasillo.equals(u.getPasillo()))
                    .collect(java.util.stream.Collectors.toList());
        }
        if (piso != null) {
            response = response.stream().filter(u -> piso.equals(u.getPiso()))
                    .collect(java.util.stream.Collectors.toList());
        }
        if (esEstante != null) {
            response = response.stream().filter(u -> esEstante.equals(u.getEsEstante()))
                    .collect(java.util.stream.Collectors.toList());
        }
        if (nivel != null) {
            response = response.stream().filter(u -> nivel.equals(u.getNivel()))
                    .collect(java.util.stream.Collectors.toList());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<UbicacionResponse> getUbicacionByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ubicacionService.getUbicacionByCodigo(codigo));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UbicacionResponse> getUbicacionById(@PathVariable Integer id) {
        log.info("Solicitando ubicación por ID: {}", id);
        return ResponseEntity.ok(ubicacionService.getUbicacionById(id));
    }

    @GetMapping("/pasillo/{pasillo}/posicion/{numero}")
    public ResponseEntity<List<UbicacionResponse>> getUbicacionesByPasilloNumero(
            @PathVariable Integer pasillo,
            @PathVariable Integer numero) {
        return ResponseEntity.ok(ubicacionService.getUbicacionesByPasilloNumero(pasillo, numero));
    }

    @PostMapping("/asignar")
    @PreAuthorize("hasAnyAuthority('JEFE', 'SUPERVISOR', 'OPERADOR')")
    public ResponseEntity<AsignarProductoResponse> asignarProducto(
            @Valid @RequestBody AsignarProductoRequest request) {
        return ResponseEntity.ok(ubicacionService.asignarProducto(request));
    }

    @PostMapping("/egreso")
    @PreAuthorize("hasAnyAuthority('JEFE', 'SUPERVISOR')")
    public ResponseEntity<EgresoProductoResponse> egresoProducto(
            @Valid @RequestBody EgresoProductoRequest request) {
        return ResponseEntity.ok(ubicacionService.egresoProducto(request));
    }

    @PostMapping("/reubicar")
    @PreAuthorize("hasAnyAuthority('JEFE', 'SUPERVISOR')")
    public ResponseEntity<ReubicarProductoResponse> reubicarProducto(
            @Valid @RequestBody ReubicarProductoRequest request) {
        return ResponseEntity.ok(ubicacionService.reubicarProducto(request));
    }
}
