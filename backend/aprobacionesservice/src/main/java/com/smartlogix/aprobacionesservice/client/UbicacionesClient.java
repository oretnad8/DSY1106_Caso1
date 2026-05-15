package com.smartlogix.aprobacionesservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UbicacionesClient {

    private final RestTemplate restTemplate;

    @Value("${ubicaciones.service.url:http://localhost:8084}")
    private String ubicacionesServiceUrl;

    public void registrarIngreso(String sku, String codigoUbicacion, Integer cantidad) {
        try {
            String url = ubicacionesServiceUrl + "/api/ubicaciones/asignar";

            Map<String, Object> request = new HashMap<>();
            request.put("sku", sku);
            request.put("codigoUbicacion", codigoUbicacion);
            request.put("cantidad", cantidad);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(url, entity, Object.class);
            log.info("Ingreso registrado en ubicaciones-service: {} unidades de {} en {}", cantidad, sku,
                    codigoUbicacion);
        } catch (Exception e) {
            log.error("Error al registrar ingreso en ubicaciones-service: {}", e.getMessage());
            throw new RuntimeException("Error al ejecutar ingreso: " + e.getMessage());
        }
    }

    public void registrarEgreso(String sku, String codigoUbicacion, Integer cantidad) {
        try {
            String url = ubicacionesServiceUrl + "/api/ubicaciones/egreso";

            Map<String, Object> request = new HashMap<>();
            request.put("sku", sku);
            request.put("codigoUbicacion", codigoUbicacion);
            request.put("cantidad", cantidad);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(url, entity, Object.class);
            log.info("Egreso registrado en ubicaciones-service: {} unidades de {} desde {}", cantidad, sku,
                    codigoUbicacion);
        } catch (Exception e) {
            log.error("Error al registrar egreso en ubicaciones-service: {}", e.getMessage());
            throw new RuntimeException("Error al ejecutar egreso: " + e.getMessage());
        }
    }

    public void registrarReubicacion(String sku, String codigoUbicacionOrigen, String codigoUbicacionDestino,
            Integer cantidad) {
        try {
            String url = ubicacionesServiceUrl + "/api/ubicaciones/reubicar";

            Map<String, Object> request = new HashMap<>();
            request.put("sku", sku);
            request.put("codigoUbicacionOrigen", codigoUbicacionOrigen);
            request.put("codigoUbicacionDestino", codigoUbicacionDestino);
            request.put("cantidad", cantidad);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(url, entity, Object.class);
            log.info("Reubicación registrada en ubicaciones-service: {} unidades de {} de {} a {}",
                    cantidad, sku, codigoUbicacionOrigen, codigoUbicacionDestino);
        } catch (Exception e) {
            log.error("Error al registrar reubicación en ubicaciones-service: {}", e.getMessage());
            throw new RuntimeException("Error al ejecutar reubicación: " + e.getMessage());
        }
    }

    public String obtenerCodigoUbicacion(Integer idUbicacion) {
        try {
            String url = ubicacionesServiceUrl + "/api/ubicaciones/id/" + idUbicacion;

            // Realizar petición GET
            // Asumimos que la respuesta tiene un campo "codigoUbicacion".
            // Si UbicacionResponse tiene esa estructura, podemos mapearlo a un Map o una
            // clase DTO.
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("codigoUbicacion")) {
                return (String) response.get("codigoUbicacion");
            }

            throw new RuntimeException("No se encontró el código de ubicación para ID: " + idUbicacion);
        } catch (Exception e) {
            log.error("Error al obtener código de ubicación: {}", e.getMessage());
            throw new RuntimeException("Error al comunicarse con ubicaciones-service: " + e.getMessage());
        }
    }
}

