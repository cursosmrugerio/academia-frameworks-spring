package mx.academia.banca.batch;

/**
 * Una fila del archivo CSV de alta masiva (Módulo 6: Spring Batch).
 * Spring Batch 5 mapea cada renglón a este record automáticamente.
 */
public record SolicitanteCsv(String nombre, String apellido, String correo, String rfc) {
}
