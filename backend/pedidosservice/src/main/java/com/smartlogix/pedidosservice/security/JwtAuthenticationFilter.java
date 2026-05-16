package com.smartlogix.pedidosservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        logger.info("Auth Header present: " + (authHeader != null));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);

            if (jwtService.validateToken(jwt)) {
                String email = jwtService.extractEmail(jwt);
                String rol = jwtService.extractRol(jwt);
                Integer userId = jwtService.extractUserId(jwt);
                String nombre = jwtService.extractNombre(jwt);

                // Agregar userId como atributo del request para usarlo en controllers
                request.setAttribute("userId", userId);
                request.setAttribute("userRole", rol);
                request.setAttribute("userEmail", email);
                request.setAttribute("userName", nombre);

                // Spring Security espera que los roles comiencen con ROLE_ por convención
                String authorityName = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(authorityName)));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Usuario autenticado: " + email + " con rol: " + authorityName);
            }
        } catch (Exception e) {
            logger.error("Error al procesar el token JWT para " + request.getRequestURI() + ": " + e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}

