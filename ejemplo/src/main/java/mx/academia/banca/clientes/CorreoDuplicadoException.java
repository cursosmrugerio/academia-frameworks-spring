package mx.academia.banca.clientes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza al intentar registrar un correo ya existente.
 * {@code @ResponseStatus} hace que Spring MVC responda 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class CorreoDuplicadoException extends RuntimeException {

    public CorreoDuplicadoException(String correo) {
        super("Ya existe un cliente con el correo " + correo);
    }
}
