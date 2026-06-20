package mx.academia.banca.modelo;

/**
 * Domicilio del solicitante. Variable anidada dentro de {@link Solicitante}.
 */
public class Domicilio {

    private String calle;
    private String ciudad;
    private String codigoPostal;
    private String pais;

    public Domicilio() {
    }

    public Domicilio(String calle, String ciudad, String codigoPostal, String pais) {
        this.calle = calle;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
        this.pais = pais;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    @Override
    public String toString() {
        return "Domicilio [calle=" + calle + ", ciudad=" + ciudad + ", codigoPostal=" + codigoPostal
                + ", pais=" + pais + "]";
    }
}
