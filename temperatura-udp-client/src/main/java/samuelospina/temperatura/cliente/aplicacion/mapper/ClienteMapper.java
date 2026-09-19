package samuelospina.temperatura.cliente.aplicacion.mapper;

import java.util.Objects;
import samuelospina.temperatura.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.temperatura.cliente.aplicacion.dto.ConvertirCommand;
import samuelospina.temperatura.cliente.dominio.excepciones.DestinoIncorrectoException;
import samuelospina.temperatura.cliente.dominio.excepciones.TemperaturaIncorrectaException;
import samuelospina.temperatura.cliente.dominio.vo.Celsius;
import samuelospina.temperatura.cliente.dominio.vo.DestinoServidor;

/** Convierte el texto de la GUI en objetos de dominio validados. */
public final class ClienteMapper {

  public DestinoServidor toDestino(final ConectarCommand comando) {
    final String puerto = Objects.toString(comando.puerto(), "").trim();
    try {
      return new DestinoServidor(comando.host(), Integer.parseInt(puerto));
    } catch (final NumberFormatException excepcion) {
      throw new DestinoIncorrectoException("El PUERTO DE RED debe ser un número entero.");
    }
  }

  public Celsius toCelsius(final ConvertirCommand comando) {
    final String texto = Objects.toString(comando.celsius(), "").trim().replace(',', '.');
    if (texto.isEmpty()) {
      throw new TemperaturaIncorrectaException("Escriba la temperatura en grados Celsius.");
    }
    try {
      return new Celsius(Double.parseDouble(texto));
    } catch (final NumberFormatException excepcion) {
      throw new TemperaturaIncorrectaException("\"" + texto + "\" no es un número válido.");
    }
  }
}
