package mx.academia.banca.clientes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba de la capa web (Módulo 5: Spring MVC) con MockMvc y el servicio mockeado.
 *
 * {@code @WebMvcTest} carga solo el controller y la infraestructura MVC (no JPA ni Kogito).
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @MockBean
    ClienteService servicio;

    @Test
    void registrarDevuelve201ConElClienteCreado() throws Exception {
        Cliente creado = new Cliente("Ana", "Garcia", "ana@example.com", "GAAA900101AAA");
        creado.setId(1L);
        creado.setNumeroCuenta("0000000001");
        when(servicio.registrar(any())).thenReturn(creado);

        NuevoClienteRequest peticion =
                new NuevoClienteRequest("Ana", "Garcia", "ana@example.com", "GAAA900101AAA");

        mvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("0000000001"));
    }

    @Test
    void registrarConCorreoInvalidoDevuelve400() throws Exception {
        NuevoClienteRequest peticion =
                new NuevoClienteRequest("Ana", "Garcia", "no-es-correo", "GAAA900101AAA");

        mvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(peticion)))
                .andExpect(status().isBadRequest());
    }
}
