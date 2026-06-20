package mx.academia.banca.bitacora;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de documentos de bitácora (Módulo 6: Spring Data MongoDB).
 *
 * Igual que con JPA, basta extender la interfaz; el query method se deriva del nombre.
 */
public interface EventoSolicitudRepository extends MongoRepository<EventoSolicitud, String> {

    List<EventoSolicitud> findBySolicitudIdOrderByFechaAsc(String solicitudId);
}
