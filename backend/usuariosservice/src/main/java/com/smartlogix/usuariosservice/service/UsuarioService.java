package com.smartlogix.usuariosservice.service;

import com.smartlogix.usuariosservice.dto.UsuarioRequest;
import com.smartlogix.usuariosservice.dto.UsuarioResponse;
import com.smartlogix.usuariosservice.dto.UsuarioUpdateRequest;
import com.smartlogix.usuariosservice.exception.EmailAlreadyExistsException;
import com.smartlogix.usuariosservice.exception.UsuarioNotFoundException;
import com.smartlogix.usuariosservice.model.Usuario;
import com.smartlogix.usuariosservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<UsuarioResponse> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public UsuarioResponse getUsuarioById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        return UsuarioResponse.fromEntity(usuario);
    }
    
    @Transactional
    public UsuarioResponse createUsuario(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado: " + request.getEmail());
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario.setActivo(true);
        
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(savedUsuario);
    }
    
    @Transactional
    public UsuarioResponse updateUsuario(Integer id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        
        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException("El email ya está registrado: " + request.getEmail());
            }
            usuario.setEmail(request.getEmail());
        }
        
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }
        
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(updatedUsuario);
    }
    
    @Transactional
    public void deleteUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        usuarioRepository.delete(usuario);
    }
    
    @Transactional
    public UsuarioResponse toggleActivo(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        
        usuario.setActivo(!usuario.getActivo());
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(updatedUsuario);
    }
}
