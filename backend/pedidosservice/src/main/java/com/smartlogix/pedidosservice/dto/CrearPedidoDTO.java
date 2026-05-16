package com.smartlogix.pedidosservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CrearPedidoDTO {
    @NotBlank(message = "El cliente es obligatorio")
    private String cliente;

    @NotNull(message = "El vendedorId es obligatorio")
    @Min(value = 1, message = "vendedorId debe ser mayor a 0")
    @JsonProperty("vendedor_id")
    private Integer vendedorId;

    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Valid
    private List<DetallePedidoDTO> detalles;

    @Data
    public static class DetallePedidoDTO {
        @NotBlank(message = "El SKU es obligatorio")
        private String sku;

        private String descripcion;

        @NotNull(message = "La cantidad solicitada es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidadSolicitada;

        private String ubicacionSugerida;
    }
}

