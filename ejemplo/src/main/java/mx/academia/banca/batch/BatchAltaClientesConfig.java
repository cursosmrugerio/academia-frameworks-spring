package mx.academia.banca.batch;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import mx.academia.banca.clientes.Cliente;
import mx.academia.banca.clientes.ClienteRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job de Spring Batch (Módulo 6): alta masiva de clientes desde un CSV.
 *
 * Patrón chunk-oriented: reader (CSV) -> processor (mapea y completa) -> writer (repositorio JPA).
 * Se aísla tras el perfil "batch" para no ejecutarse en el resto de pruebas, y el processor es
 * idempotente (omite correos ya existentes), así que relanzar el job no duplica datos.
 */
@Configuration
@Profile("batch")
public class BatchAltaClientesConfig {

    @Bean
    public FlatFileItemReader<SolicitanteCsv> lectorSolicitantes() {
        return new FlatFileItemReaderBuilder<SolicitanteCsv>()
                .name("lectorSolicitantes")
                .resource(new ClassPathResource("datos/solicitantes.csv"))
                .linesToSkip(1) // encabezado
                .delimited().delimiter(",")
                .names("nombre", "apellido", "correo", "rfc")
                .targetType(SolicitanteCsv.class)
                .build();
    }

    @Bean
    public ItemProcessor<SolicitanteCsv, Cliente> procesadorAltaCliente(ClienteRepository repositorio) {
        return fila -> {
            if (repositorio.existsByCorreo(fila.correo())) {
                return null; // idempotente: Spring Batch descarta los items null
            }
            Cliente cliente = new Cliente(fila.nombre(), fila.apellido(), fila.correo(), fila.rfc());
            cliente.setEstatus("ACTIVO");
            cliente.setFechaAlta(LocalDateTime.now());
            cliente.setNumeroCuenta(String.format("%010d",
                    ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L)));
            return cliente;
        };
    }

    @Bean
    public RepositoryItemWriter<Cliente> escritorClientes(ClienteRepository repositorio) {
        return new RepositoryItemWriterBuilder<Cliente>()
                .repository(repositorio)
                .methodName("save")
                .build();
    }

    @Bean
    public Step altaClientesStep(JobRepository jobRepository, PlatformTransactionManager txManager,
                                 FlatFileItemReader<SolicitanteCsv> lectorSolicitantes,
                                 ItemProcessor<SolicitanteCsv, Cliente> procesadorAltaCliente,
                                 RepositoryItemWriter<Cliente> escritorClientes) {
        return new StepBuilder("altaClientesStep", jobRepository)
                .<SolicitanteCsv, Cliente>chunk(10, txManager)
                .reader(lectorSolicitantes)
                .processor(procesadorAltaCliente)
                .writer(escritorClientes)
                .build();
    }

    @Bean
    public Job altaClientesJob(JobRepository jobRepository, Step altaClientesStep) {
        return new JobBuilder("altaClientesJob", jobRepository)
                .start(altaClientesStep)
                .build();
    }
}
