package mx.academia.banca.clientes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Prueba de integración del repositorio (Módulo 5: Spring Data JPA).
 *
 * {@code @DataJpaTest} levanta solo la capa JPA contra una base H2 en memoria
 * y revierte la transacción al final de cada prueba.
 */
@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    ClienteRepository repositorio;

    @Test
    void guardaYConsultaPorCorreo() {
        repositorio.save(new Cliente("Luis", "Soto", "luis@example.com", "SOLL850101AAA"));

        assertThat(repositorio.existsByCorreo("luis@example.com")).isTrue();
        assertThat(repositorio.findByCorreo("luis@example.com"))
                .isPresent()
                .get()
                .extracting(Cliente::getApellido)
                .isEqualTo("Soto");
    }
}
