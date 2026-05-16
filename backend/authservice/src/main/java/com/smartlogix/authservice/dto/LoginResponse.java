package com.smartlogix.authservice.dto;

import com.smartlogix.authservice.model.Usuario;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String nombre;
    private String email;
    private Usuario.Rol rol;

    public LoginResponse() {
    }

    public LoginResponse(String token, Integer id, String nombre, String email, Usuario.Rol rol) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Usuario.Rol getRol() {
        return rol;
    }

    public void setRol(Usuario.Rol rol) {
        this.rol = rol;
    }

    // Manual builder implementation (replaces Lombok @Builder)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String type;
        private Integer id;
        private String nombre;
        private String email;
        private Usuario.Rol rol;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder rol(Usuario.Rol rol) {
            this.rol = rol;
            return this;
        }

        public LoginResponse build() {
            LoginResponse r = new LoginResponse(this.token, this.id, this.nombre, this.email, this.rol);
            if (this.type != null) r.setType(this.type);
            return r;
        }
    }
}
