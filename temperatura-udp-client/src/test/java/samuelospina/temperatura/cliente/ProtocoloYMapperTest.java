package samuelospina.temperatura.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import samuelospina.temperatura.cliente.adaptadores.red.ProtocoloUdpMapper;
import samuelospina.temperatura.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.temperatura.cliente.aplicacion.dto.ConvertirCommand;
import samuelospina.temperatura.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.temperatura.cliente.dominio.excepciones.DestinoIncorrectoException;
import samuelospina.temperatura.cliente.dominio.excepciones.RespuestaServidorException;
import samuelospina.temperatura.cliente.dominio.excepciones.TemperaturaIncorrectaException;
import samuelospina.temperatura.cliente.dominio.modelos.ResultadoConversion;
import samuelospina.temperatura.cliente.dominio.vo.Celsius;

class ProtocoloYMapperTest {

  private final ProtocoloUdpMapper protocolo = new ProtocoloUdpMapper();
  private final ClienteMapper mapper = new ClienteMapper();

  @Test
  void serializaYParsea() {
    assertEquals("CONVERTIR;36.50", protocolo.serializarConversion(new Celsius(36.5)));
    assertEquals(new ResultadoConversion(100, 212), protocolo.parsearConversion("OK_CONVERSION;100.00;212.00"));
  }

  @Test
  void errorDelServidorLlegaComoExcepcion() {
    final RespuestaServidorException e = assertThrows(RespuestaServidorException.class,
        () -> protocolo.parsearConversion("ERROR;La temperatura debe ser numérica."));
    assertEquals("La temperatura debe ser numérica.", e.getMessage());
    assertThrows(RespuestaServidorException.class, () -> protocolo.validarConexion("HOLA"));
  }

  @Test
  void validaEntradasDelUsuario() {
    assertEquals(new Celsius(-12.5), mapper.toCelsius(new ConvertirCommand(" -12,5 ")));
    assertThrows(TemperaturaIncorrectaException.class, () -> mapper.toCelsius(new ConvertirCommand("")));
    assertThrows(TemperaturaIncorrectaException.class, () -> mapper.toCelsius(new ConvertirCommand("abc")));
    assertThrows(TemperaturaIncorrectaException.class, () -> mapper.toCelsius(new ConvertirCommand("-300")));
    assertThrows(DestinoIncorrectoException.class, () -> mapper.toDestino(new ConectarCommand("localhost", "x")));
    assertThrows(DestinoIncorrectoException.class, () -> mapper.toDestino(new ConectarCommand(" ", "9007")));
  }
}
