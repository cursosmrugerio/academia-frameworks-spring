package mx.academia.banca;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.academia.banca.modelo.Domicilio;
import mx.academia.banca.modelo.Solicitante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.Model;
import org.kie.kogito.auth.IdentityProviders;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prueba de proceso (JUnit 5) del flujo de solicitud de cuenta.
 *
 * Verifica el camino feliz: arranque del proceso, aprobación de primera línea y de
 * segunda línea (por un aprobador distinto), y finalización del proceso.
 *
 * Anticipa el Módulo 5: JUnit + Spring Boot test. En F1/F2 se añaden Mockito,
 * controllers REST y pruebas de los endpoints autogenerados.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BancaApplication.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class SolicitudCuentaProcessTest {

    @Autowired
    @Qualifier("solicitudCuenta")
    Process<? extends Model> solicitudCuentaProcess;

    @Test
    public void flujoDeAprobacionDeDosLineas() {
        assertNotNull(solicitudCuentaProcess);

        Model modelo = solicitudCuentaProcess.createModel();
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("solicitante",
                new Solicitante("Juan", "Perez", "juan.perez@example.com", "PEJJ800101ABC", 35000.0,
                        new Domicilio("Av. Reforma 100", "CDMX", "06600", "MX")));
        modelo.fromMap(parametros);

        ProcessInstance<?> instancia = solicitudCuentaProcess.createInstance(modelo);
        instancia.start();
        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE, instancia.status());

        // Aprobación de primera línea (cualquier integrante del grupo "managers").
        SecurityPolicy politica = SecurityPolicy.of(IdentityProviders.of("admin", Collections.singletonList("managers")));
        List<WorkItem> tareas = instancia.workItems(politica);
        assertEquals(1, tareas.size());
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("approved", true);
        instancia.completeWorkItem(tareas.get(0).getId(), resultado, politica);

        // Aprobación de segunda línea: el aprobador anterior queda excluido, usamos otro usuario del grupo.
        politica = SecurityPolicy.of(IdentityProviders.of("juan", Collections.singletonList("managers")));
        tareas = instancia.workItems(politica);
        assertEquals(1, tareas.size());
        resultado.put("approved", false);
        instancia.completeWorkItem(tareas.get(0).getId(), resultado, politica);

        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, instancia.status());

        Model variablesFinales = (Model) instancia.variables();
        assertEquals(Boolean.TRUE, variablesFinales.toMap().get("aprobacionPrimeraLinea"));
        assertEquals(Boolean.FALSE, variablesFinales.toMap().get("aprobacionSegundaLinea"));
    }
}
