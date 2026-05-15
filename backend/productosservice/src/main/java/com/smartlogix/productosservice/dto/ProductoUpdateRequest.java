package com.smartlogix.productosservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoUpdateRequest {
    
    private String descripcion;
    
    private Integer stock;
    
    private String codigoBarrasIndividual;
    
    private String lpn;
    
    private String lpnDesc;
    
    private LocalDate fechaVencimiento;
}
