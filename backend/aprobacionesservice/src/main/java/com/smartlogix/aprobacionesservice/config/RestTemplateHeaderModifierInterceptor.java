package com.smartlogix.aprobacionesservice.config;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class RestTemplateHeaderModifierInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        // Opción 1: Obtener del SecurityContext (si estamos en un hilo con contexto de
        // seguridad)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Opción 2: Obtener del Request original (más robusto si se pasa el header tal
        // cual)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest originalRequest = attributes.getRequest();
            String authHeader = originalRequest.getHeader("Authorization");

            if (authHeader != null && !authHeader.isEmpty()) {
                request.getHeaders().add("Authorization", authHeader);
            }
        }

        return execution.execute(request, body);
    }
}

