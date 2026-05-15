package com.smartlogix.pedidosservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePedidoRequestDTO {
    @NotBlank
    private String cliente;

    @NotEmpty
    private List<DetallePedidoDTO> detalles; // Reuse DetallePedidoDTO for input, expecting sku and cantidadSolicitada
}

