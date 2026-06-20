package mx.academia.banca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot que hospeda el motor BPM (Kogito).
 *
 * El escaneo incluye {@code org.kie.kogito.**} (código generado por Kogito en tiempo de build)
 * además del paquete propio del dominio de banca.
 */
@SpringBootApplication(scanBasePackages = { "org.kie.kogito.**", "mx.academia.banca.**" })
public class BancaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancaApplication.class, args);
    }
}
