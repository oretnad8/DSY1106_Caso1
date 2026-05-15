package com.smartlogix.usuariosservice.dto;

import com.smartlogix.usuariosservice.model.Usuario;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateRequest {
    
    private String nombre;
    
    @Email(message = "Email inválido")
    private String email;
    
    private String password;
    
    private Usuario.Rol rol;
}
