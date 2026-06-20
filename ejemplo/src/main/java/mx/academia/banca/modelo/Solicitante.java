package mx.academia.banca.modelo;

/**
 * Datos del solicitante de apertura de cuenta.
 *
 * Es una variable del proceso BPMN {@code solicitudCuenta}; por eso requiere
 * constructor sin argumentos y getters/setters (marshalling de Kogito).
 */
public class Solicitante {

    private String nombre;
    private String apellido;
    private String correo;
    private String rfc;
    private double ingresoMensual;
    private Domicilio domicilio;

    public Solicitante() {
    }

    public Solicitante(String nombre, String apellido, String correo, String rfc,
                       double ingresoMensual, Domicilio domicilio) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rfc = rfc;
        this.ingresoMensual = ingresoMensual;
        this.domicilio = domicilio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public double getIngresoMensual() {
        return ingresoMensual;
    }

    public void setIngresoMensual(double ingresoMensual) {
        this.ingresoMensual = ingresoMensual;
    }

    public Domicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(Domicilio domicilio) {
        this.domicilio = domicilio;
    }

    @Override
    public String toString() {
        return "Solicitante [nombre=" + nombre + ", apellido=" + apellido + ", correo=" + correo
                + ", rfc=" + rfc + ", ingresoMensual=" + ingresoMensual + ", domicilio=" + domicilio + "]";
    }
}
