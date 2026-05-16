package com.smartlogix.authservice.controller;

import com.smartlogix.authservice.dto.ResetPasswordRequest;
import com.smartlogix.authservice.model.Usuario;
import com.smartlogix.authservice.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/dev")
public class DevController {

    private static final Logger log = LoggerFactory.getLogger(DevController.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DevController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Dev endpoint: resetea la contraseña (codifica con BCrypt y actualiza la fila).
     * Uso: POST /dev/reset-password { "email": "...", "password": "..." }
     * Nota: endpoint para desarrollo. Elimínalo antes de producción.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        Optional<Usuario> opt = usuarioRepository.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        Usuario u = opt.get();
        String hashed = passwordEncoder.encode(req.getPassword());
        u.setPassword(hashed);
        usuarioRepository.save(u);
        log.info("Dev reset password for {}: newHashLen={}", u.getEmail(), hashed.length());
        return ResponseEntity.ok().body("Password actualizado");
    }
}

