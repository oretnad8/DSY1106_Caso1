package com.smartlogix.pedidosservice.dto;

import com.smartlogix.pedidosservice.model.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoreoDTO {
    private Map<EstadoPedido, Long> conteoPorEstado;
    private Long totalPedidos;
}

