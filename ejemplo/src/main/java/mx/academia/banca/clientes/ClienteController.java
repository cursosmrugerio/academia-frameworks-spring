package mx.academia.banca.clientes;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * API REST de clientes (Módulo 5: Spring MVC; Módulo 6: verbos HTTP, URIs, response codes).
 *
 * Convive con los endpoints REST que Kogito autogenera para el proceso.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService servicio;

    public ClienteController(ClienteService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<Cliente> registrar(@Valid @RequestBody NuevoClienteRequest peticion,
                                             UriComponentsBuilder uriBuilder) {
        Cliente creado = servicio.registrar(peticion.aCliente());
        URI ubicacion = uriBuilder.path("/api/clientes/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping("/{id}")
    public Cliente porId(@PathVariable Long id) {
        return servicio.porId(id);
    }

    @GetMapping
    public List<Cliente> listar() {
        return servicio.listar();
    }
}
