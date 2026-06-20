package mx.academia.banca.clientes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando no existe el cliente solicitado.
 * {@code @ResponseStatus} hace que Spring MVC responda 404 (Módulo 6: response codes).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(Long id) {
        super("No existe el cliente con id " + id);
    }
}
