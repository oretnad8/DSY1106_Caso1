package com.smartlogix.aprobacionesservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechazarRequest {
    
    @NotBlank(message = "Las observaciones son obligatorias al rechazar")
    private String observaciones;
}
