package com.smartlogix.aprobacionesservice.dto;

import com.smartlogix.aprobacionesservice.model.Aprobacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprobacionResponse {
    private Integer id;
    private Aprobacion.TipoMovimiento tipoMovimiento;
    private String sku;
    private Integer cantidad;
    private String motivo;
    private SolicitanteDTO solicitante;
    private LocalDateTime fechaSolicitud;
    private Aprobacion.Estado estado;
    private AprobadorDTO aprobador;
    private LocalDateTime fechaAprobacion;
    private String observaciones;
    private Integer idUbicacionOrigen;
    private Integer idUbicacionDestino;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolicitanteDTO {
        private Integer id;
        private String nombre;
        private String email;
        private String rol;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AprobadorDTO {
        private Integer id;
        private String nombre;
        private String email;
        private String rol;
    }
    
    public static AprobacionResponse fromEntity(Aprobacion aprobacion) {
        AprobacionResponseBuilder builder = AprobacionResponse.builder()
                .id(aprobacion.getId())
                .tipoMovimiento(aprobacion.getTipoMovimiento())
                .sku(aprobacion.getSku())
                .cantidad(aprobacion.getCantidad())
                .motivo(aprobacion.getMotivo())
                .fechaSolicitud(aprobacion.getFechaSolicitud())
                .estado(aprobacion.getEstado())
                .fechaAprobacion(aprobacion.getFechaAprobacion())
                .observaciones(aprobacion.getObservaciones())
                .idUbicacionOrigen(aprobacion.getIdUbicacionOrigen())
                .idUbicacionDestino(aprobacion.getIdUbicacionDestino());
        
        if (aprobacion.getSolicitante() != null) {
            builder.solicitante(SolicitanteDTO.builder()
                    .id(aprobacion.getSolicitante().getId())
                    .nombre(aprobacion.getSolicitante().getNombre())
                    .email(aprobacion.getSolicitante().getEmail())
                    .rol(aprobacion.getSolicitante().getRol().name())
                    .build());
        }
        
        if (aprobacion.getAprobador() != null) {
            builder.aprobador(AprobadorDTO.builder()
                    .id(aprobacion.getAprobador().getId())
                    .nombre(aprobacion.getAprobador().getNombre())
                    .email(aprobacion.getAprobador().getEmail())
                    .rol(aprobacion.getAprobador().getRol().name())
                    .build());
        }
        
        return builder.build();
    }
}
