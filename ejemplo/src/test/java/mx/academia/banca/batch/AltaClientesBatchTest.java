package mx.academia.banca.batch;

import static org.assertj.core.api.Assertions.assertThat;

import mx.academia.banca.clientes.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba del job de Spring Batch (Módulo 6).
 *
 * Con el perfil "batch" activo existe el Job; {@code JobLauncherTestUtils} lo lanza
 * explícitamente y se verifica que dio de alta a los clientes del CSV.
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("batch")
class AltaClientesBatchTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    ClienteRepository clienteRepository;

    @Test
    void elJobDaDeAltaLosClientesDelCsv() throws Exception {
        JobExecution ejecucion = jobLauncherTestUtils.launchJob();

        assertThat(ejecucion.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(clienteRepository.existsByCorreo("carlos.ruiz@example.com")).isTrue();
        assertThat(clienteRepository.existsByCorreo("diana.mora@example.com")).isTrue();
        assertThat(clienteRepository.existsByCorreo("esteban.vela@example.com")).isTrue();
    }
}
