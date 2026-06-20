package mx.academia.banca.clientes;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de {@link Cliente} (Módulo 5: Spring Data JPA).
 *
 * Al extender {@link JpaRepository} se obtienen gratis las operaciones CRUD;
 * los métodos derivados (query methods) se resuelven por el nombre.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
