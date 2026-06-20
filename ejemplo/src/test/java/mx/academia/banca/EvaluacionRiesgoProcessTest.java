package mx.academia.banca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import mx.academia.banca.modelo.Domicilio;
import mx.academia.banca.modelo.Solicitante;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Prueba del proceso con service task (Módulo 6): el proceso invoca al @Component
 * {@code EvaluacionRiesgoService} y, al ser una tarea automática, completa de inmediato.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest // contexto web MOCK por defecto: Kogito usa "application scope", que requiere contexto web
class EvaluacionRiesgoProcessTest {

    @Autowired
    @Qualifier("evaluacionRiesgo")
    Process<? extends Model> evaluacionRiesgoProcess;

    @Test
    void evaluaRiesgoYCompleta() {
        assertNotNull(evaluacionRiesgoProcess);

        Model modelo = evaluacionRiesgoProcess.createModel();
        modelo.fromMap(Map.of("solicitante",
                new Solicitante("Ana", "Garcia", "ana@example.com", "GAAA900101AAA", 35000.0,
                        new Domicilio("Av. Reforma 100", "CDMX", "06600", "MX"))));

        ProcessInstance<?> instancia = evaluacionRiesgoProcess.createInstance(modelo);
        instancia.start();

        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, instancia.status());
        assertEquals("BAJO", ((Model) instancia.variables()).toMap().get("nivelRiesgo"));
    }
}
