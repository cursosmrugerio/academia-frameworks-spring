package mx.academia.banca.bitacora;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prueba del repositorio Mongo contra MongoDB REAL en un contenedor (Módulo 6).
 *
 * {@code disabledWithoutDocker = true}: se omite si no hay Docker, igual que la de Postgres.
 */
@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class EventoSolicitudMongoTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    EventoSolicitudRepository repositorio;

    @Test
    void guardaYConsultaElHistorialOrdenado() {
        repositorio.save(new EventoSolicitud("S-1", "CREADA", "Solicitud registrada", Instant.now()));
        repositorio.save(new EventoSolicitud("S-1", "APROBADA_1", "Aprobada primera linea", Instant.now()));

        assertThat(repositorio.findBySolicitudIdOrderByFechaAsc("S-1"))
                .hasSize(2)
                .extracting(EventoSolicitud::getTipo)
                .containsExactly("CREADA", "APROBADA_1");
    }
}
