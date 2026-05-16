package com.smartlogix.pedidosservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadoDTO {
    private String estado;
    private LocalDateTime timestamp;
    private Integer usuarioId;
    private String usuarioNombre;
}

