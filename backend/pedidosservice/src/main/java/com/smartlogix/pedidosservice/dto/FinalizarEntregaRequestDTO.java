package com.smartlogix.pedidosservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarEntregaRequestDTO {
    private String fotoEvidencia; // Base64 or URL
}

