package mx.academia.banca.bitacora;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Evento de la bitácora de una solicitud (Módulo 6: MongoDB).
 *
 * Documento de MongoDB: datos de tipo "histórico/append-only" que encajan mejor
 * en un almacén documental que en tablas relacionales.
 */
@Document(collection = "eventos_solicitud")
public class EventoSolicitud {

    @Id
    private String id;

    private String solicitudId;
    private String tipo;
    private String detalle;
    private Instant fecha;

    public EventoSolicitud() {
    }

    public EventoSolicitud(String solicitudId, String tipo, String detalle, Instant fecha) {
        this.solicitudId = solicitudId;
        this.tipo = tipo;
        this.detalle = detalle;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(String solicitudId) {
        this.solicitudId = solicitudId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
