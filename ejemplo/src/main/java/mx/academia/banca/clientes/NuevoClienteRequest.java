package mx.academia.banca.clientes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la petición para registrar un cliente (Módulo 5/6: Spring MVC + validación).
 *
 * Las anotaciones de Bean Validation se aplican con {@code @Valid} en el controller;
 * si fallan, Spring MVC responde 400 automáticamente.
 */
public record NuevoClienteRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Email String correo,
        @NotBlank String rfc) {

    public Cliente aCliente() {
        return new Cliente(nombre, apellido, correo, rfc);
    }
}
