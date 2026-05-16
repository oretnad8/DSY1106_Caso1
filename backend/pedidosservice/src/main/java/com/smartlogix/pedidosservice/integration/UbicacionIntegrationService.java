package com.smartlogix.pedidosservice.integration;

import com.smartlogix.pedidosservice.dto.EgresoProductoRequestDTO;
import com.smartlogix.pedidosservice.dto.EgresoProductoResponseDTO;
import com.smartlogix.pedidosservice.dto.UbicacionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UbicacionIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${ubicaciones.service.url:http://localhost:8084}")
    private String ubicacionesServiceUrl;

    public Optional<String> obtenerMejorUbicacion(String sku) {
        try {
            String url = ubicacionesServiceUrl + "/api/ubicaciones";

            ResponseEntity<List<UbicacionResponseDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UbicacionResponseDTO>>() {
                    });

            List<UbicacionResponseDTO> ubicaciones = response.getBody();
            if (ubicaciones == null)
                return Optional.empty();

            // 1. Filtrar solo filas que contengan el SKU y con stock > 0
            List<UbicacionResponseDTO> candidatas = ubicaciones.stream()
                    .filter(u -> u.getProductos().stream()
                            .anyMatch(p -> p.getSku().equals(sku) && p.getCantidadEnUbicacion() > 0))
                    .collect(java.util.stream.Collectors.toList());

            if (candidatas.isEmpty())
                return Optional.empty();

            // 2. Ordenar priorizando Piso 'A' (o nombre contenga "-A-")
            // Asumimos 'piso' es Character
            candidatas.sort((u1, u2) -> {
                boolean u1EsA = (u1.getPiso() != null && u1.getPiso() == 'A')
                        || (u1.getCodigoUbicacion() != null && u1.getCodigoUbicacion().contains("-A-"));
                boolean u2EsA = (u2.getPiso() != null && u2.getPiso() == 'A')
                        || (u2.getCodigoUbicacion() != null && u2.getCodigoUbicacion().contains("-A-"));

                if (u1EsA && !u2EsA)
                    return -1; // u1 primero
                if (!u1EsA && u2EsA)
                    return 1; // u2 primero
                return 0; // Iguales en prioridad de piso
            });

            // 3. Retornar la primera
            return Optional.of(candidatas.get(0).getCodigoUbicacion());

        } catch (Exception e) {
            log.error("Error al obtener ubicaciones para SKU: {}", sku, e);
            return Optional.empty();
        }
    }

    public void egresarStock(String sku, Integer cantidad, String ubicacionCode, String referencia) {
        try {
            EgresoProductoRequestDTO request = EgresoProductoRequestDTO.builder()
                    .sku(sku)
                    .cantidad(cantidad)
                    .codigoUbicacion(ubicacionCode)
                    .motivo("VENTA: " + referencia)
                    .build();

            String url = ubicacionesServiceUrl + "/api/ubicaciones/egreso";

            restTemplate.postForEntity(url, request, EgresoProductoResponseDTO.class);
            log.info("Stock descontado correctamente: SKU={}, Cant={}, Ubication={}, Ref={}", sku, cantidad,
                    ubicacionCode, referencia);

        } catch (Exception e) {
            log.error("Error al descontar stock externa", e);
            throw new RuntimeException("Error al comunicarse con Ubicaciones Service para egreso: " + e.getMessage());
        }
    }
}

