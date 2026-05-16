package com.smartlogix.usuariosservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(length = 100, nullable = false)
    private String nombre;
    
    @Column(length = 100, unique = true, nullable = false)
    private String email;
    
    @Column(length = 255, nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    public enum Rol {
        ADMIN, JEFE, SUPERVISOR, OPERADOR
    }
}
