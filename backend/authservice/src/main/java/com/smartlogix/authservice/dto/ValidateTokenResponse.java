package com.smartlogix.authservice.dto;

import com.smartlogix.authservice.model.Usuario;

public class ValidateTokenResponse {
    private boolean valid;
    private Integer userId;
    private String email;
    private Usuario.Rol rol;
    private String message;

    public ValidateTokenResponse() {
    }

    public ValidateTokenResponse(boolean valid, Integer userId, String email, Usuario.Rol rol, String message) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
        this.rol = rol;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Manual builder implementation (replaces Lombok @Builder)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean valid;
        private Integer userId;
        private String email;
        private Usuario.Rol rol;
        private String message;

        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder userId(Integer userId) {
            this.userId = userId;
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

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ValidateTokenResponse build() {
            ValidateTokenResponse r = new ValidateTokenResponse(this.valid, this.userId, this.email, this.rol, this.message);
            return r;
        }
    }
}
