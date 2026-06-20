package mx.academia.banca.clientes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prueba UNITARIA de {@link ClienteService} (Módulo 5: JUnit + Mockito + assertions con AssertJ).
 *
 * El repositorio se reemplaza por un mock: la prueba no toca base de datos ni Spring,
 * solo verifica la lógica del servicio.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    ClienteRepository repositorio;

    @InjectMocks
    ClienteService servicio;

    @Test
    void registrarAsignaCuentaEstatusYFecha() {
        when(repositorio.existsByCorreo("ana@example.com")).thenReturn(false);
        when(repositorio.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Cliente resultado = servicio.registrar(
                new Cliente("Ana", "Garcia", "ana@example.com", "GAAA900101AAA"));

        assertThat(resultado.getEstatus()).isEqualTo("ACTIVO");
        assertThat(resultado.getNumeroCuenta()).isNotBlank().hasSize(10);
        assertThat(resultado.getFechaAlta()).isNotNull();
        verify(repositorio).save(resultado);
    }

    @Test
    void registrarRechazaCorreoDuplicado() {
        when(repositorio.existsByCorreo("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                servicio.registrar(new Cliente("Ana", "Garcia", "ana@example.com", "GAAA900101AAA")))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(repositorio, never()).save(any());
    }

    @Test
    void porIdLanzaCuandoNoExiste() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.porId(99L))
                .isInstanceOf(ClienteNoEncontradoException.class);
    }
}
