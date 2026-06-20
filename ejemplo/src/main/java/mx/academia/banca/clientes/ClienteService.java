package mx.academia.banca.clientes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio del alta de clientes (Módulo 5: Spring Core / inyección de dependencias).
 *
 * Recibe el {@link ClienteRepository} por constructor (inyección de dependencias):
 * esto permite probar la clase en aislamiento con un mock del repositorio (Mockito).
 */
@Service
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Cliente registrar(Cliente nuevo) {
        if (repositorio.existsByCorreo(nuevo.getCorreo())) {
            throw new CorreoDuplicadoException(nuevo.getCorreo());
        }
        nuevo.setNumeroCuenta(generarNumeroCuenta());
        nuevo.setEstatus("ACTIVO");
        nuevo.setFechaAlta(LocalDateTime.now());
        return repositorio.save(nuevo);
    }

    @Transactional(readOnly = true)
    public Cliente porId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repositorio.findAll();
    }

    private String generarNumeroCuenta() {
        return String.format("%010d", ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L));
    }
}
