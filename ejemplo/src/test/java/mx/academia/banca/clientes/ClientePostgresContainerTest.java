package mx.academia.banca.clientes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Misma prueba del repositorio, pero contra PostgreSQL REAL en un contenedor (Módulo 6).
 *
 * {@code disabledWithoutDocker = true}: si el demonio de Docker no está disponible,
 * la prueba se OMITE automáticamente (no rompe la build). Cuando Docker está activo,
 * Testcontainers levanta Postgres 16, corre la prueba y apaga el contenedor al terminar.
 *
 * Demuestra el contraste con {@code ClienteRepositoryTest} (que usa H2): el mismo código
 * de repositorio funciona contra el motor real.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ClientePostgresContainerTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    ClienteRepository repositorio;

    @Test
    void persisteYConsultaEnPostgresReal() {
        repositorio.save(new Cliente("Mar", "Lopez", "mar@example.com", "LOMM900101AAA"));

        assertThat(repositorio.existsByCorreo("mar@example.com")).isTrue();
        assertThat(repositorio.findByCorreo("mar@example.com"))
                .isPresent()
                .get()
                .extracting(Cliente::getNombre)
                .isEqualTo("Mar");
    }
}
