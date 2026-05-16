package com.smartlogix.authservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartlogix.authservice.dto.LoginRequest;
import com.smartlogix.authservice.dto.LoginResponse;
import com.smartlogix.authservice.dto.ValidateTokenResponse;
import com.smartlogix.authservice.exception.InvalidCredentialsException;
import com.smartlogix.authservice.model.Usuario;
import com.smartlogix.authservice.repository.UsuarioRepository;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for email={}", request.getEmail());
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(request.getEmail())
                .orElseThrow(() -> {
                    log.debug("Usuario no encontrado o inactivo: {}", request.getEmail());
                    return new InvalidCredentialsException("Credenciales inválidas");
                });

        boolean matches = passwordEncoder.matches(request.getPassword(), usuario.getPassword());
        log.debug("Password match result for {}: {}", request.getEmail(), matches);
        if (!matches) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getNombre());
        log.debug("Token generated for user {}: (hidden)", usuario.getEmail());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .build();
    }

    public ValidateTokenResponse validateToken(String token) {
        try {
            // first basic JWT validation
            if (!jwtService.validateToken(token)) {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .message("Token expirado o inválido")
                        .build();
            }

            // then check blacklist
            if (tokenBlacklistService.isBlacklisted(token)) {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .message("Token revocado (logout)")
                        .build();
            }

            Integer userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String rol = jwtService.extractRol(token);

            return ValidateTokenResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .rol(Usuario.Rol.valueOf(rol))
                    .message("Token válido")
                    .build();
        } catch (Exception e) {
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Token inválido: " + e.getMessage())
                    .build();
        }
    }

    public void logout(String token) {
        try {
            long expMillis = jwtService.extractExpirationMillis(token);
            if (expMillis <= 0L) {
                // fallback: set short-lived blacklist time (e.g., now + 5 minutes)
                expMillis = System.currentTimeMillis() + 5 * 60 * 1000L;
            }
            tokenBlacklistService.add(token, expMillis);
            log.info("Token invalidado (logout) hasta {}", expMillis);
        } catch (Exception e) {
            log.warn("Logout: no se pudo invalidar token: {}", e.getMessage());
        }
    }
}
