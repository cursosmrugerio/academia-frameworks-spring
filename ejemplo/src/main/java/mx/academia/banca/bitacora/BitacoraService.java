package mx.academia.banca.bitacora;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Registra y consulta la bitácora de eventos de una solicitud (Módulo 6: MongoDB).
 */
@Service
public class BitacoraService {

    private final EventoSolicitudRepository repositorio;

    public BitacoraService(EventoSolicitudRepository repositorio) {
        this.repositorio = repositorio;
    }

    public EventoSolicitud registrar(String solicitudId, String tipo, String detalle) {
        return repositorio.save(new EventoSolicitud(solicitudId, tipo, detalle, Instant.now()));
    }

    public List<EventoSolicitud> historial(String solicitudId) {
        return repositorio.findBySolicitudIdOrderByFechaAsc(solicitudId);
    }
}
