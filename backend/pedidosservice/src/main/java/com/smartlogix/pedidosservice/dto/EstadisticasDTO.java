package com.smartlogix.pedidosservice.dto;

import lombok.Data;

@Data
public class EstadisticasDTO {
    // We can use a Map or specific fields. For flexibility as per controller return
    // type Map<String, Object>
    // I will not use this class in the Controller return type to avoid changing
    // signature,
    // but I can use it internally or just build the Map.
    // The requirement says: Retorna un JSON con...
}

