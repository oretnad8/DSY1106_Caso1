package com.smartlogix.pedidosservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EgresoStockDTO {
    private String sku;
    private Integer cantidad;
    private String ubicacionCodigo;
    private String referencia; // e.g., "Pedido #123"
}

