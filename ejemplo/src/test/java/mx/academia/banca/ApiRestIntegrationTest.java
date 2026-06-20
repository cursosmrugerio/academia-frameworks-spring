package mx.academia.banca;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import mx.academia.banca.clientes.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Prueba de integración de la API REST (Módulo 6: HTTP, verbos, URIs, response codes).
 *
 * Levanta la aplicación completa en un puerto aleatorio y le pega con un cliente HTTP real,
 * ejercitando TANTO los endpoints propios ({@code /api/clientes}) COMO los que Kogito
 * autogenera para el proceso ({@code /solicitudCuenta}). Sustituye a una demo manual con curl:
 * es repetible y se ejecuta con {@code mvn test}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiRestIntegrationTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void clientesVerbosYCodigosDeRespuesta() {
        // POST válido -> 201 Created
        Map<String, Object> nuevo = Map.of(
                "nombre", "Ana", "apellido", "Garcia",
                "correo", "ana.api@example.com", "rfc", "GAAA900101AAA");
        ResponseEntity<Cliente> creado = rest.postForEntity("/api/clientes", nuevo, Cliente.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creado.getBody()).isNotNull();
        assertThat(creado.getBody().getNumeroCuenta()).isNotBlank();
        Long id = creado.getBody().getId();

        // GET por id existente -> 200 OK
        assertThat(rest.getForEntity("/api/clientes/{id}", Cliente.class, id).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        // GET inexistente -> 404 Not Found
        assertThat(rest.getForEntity("/api/clientes/{id}", String.class, 999_999).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        // POST con correo inválido -> 400 Bad Request (validación)
        Map<String, Object> invalido = Map.of(
                "nombre", "Ana", "apellido", "Garcia", "correo", "no-es-correo", "rfc", "X");
        assertThat(rest.postForEntity("/api/clientes", invalido, String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void procesoRestAutogeneradoPorKogito() {
        // POST: inicia una instancia del proceso a partir de sus variables
        Map<String, Object> cuerpo = Map.of("solicitante", Map.of(
                "nombre", "Juan", "apellido", "Perez",
                "correo", "juan.api@example.com", "rfc", "PEJJ800101ABC",
                "ingresoMensual", 35000.0,
                "domicilio", Map.of("calle", "Reforma 100", "ciudad", "CDMX",
                        "codigoPostal", "06600", "pais", "MX")));

        ResponseEntity<Map> creado = rest.postForEntity("/solicitudCuenta", cuerpo, Map.class);
        assertThat(creado.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(creado.getBody()).containsKey("id");

        // GET: lista de instancias en curso -> 200 OK
        assertThat(rest.getForEntity("/solicitudCuenta", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}
