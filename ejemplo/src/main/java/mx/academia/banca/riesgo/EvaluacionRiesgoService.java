package mx.academia.banca.riesgo;

import mx.academia.banca.modelo.Solicitante;
import org.springframework.stereotype.Component;

/**
 * Servicio de negocio invocado por el proceso mediante un <b>service task</b> (Módulo 6).
 *
 * Es un {@code @Component} de Spring: Kogito lo resuelve del contexto e inyecta sus
 * dependencias (inyección de dependencias dentro del flujo BPM). Aquí calcula un nivel
 * de riesgo simple a partir del ingreso mensual del solicitante.
 */
@Component
public class EvaluacionRiesgoService {

    public String evaluar(Solicitante solicitante) {
        double ingreso = (solicitante == null) ? 0.0 : solicitante.getIngresoMensual();
        if (ingreso >= 30_000) {
            return "BAJO";
        }
        if (ingreso >= 12_000) {
            return "MEDIO";
        }
        return "ALTO";
    }
}
