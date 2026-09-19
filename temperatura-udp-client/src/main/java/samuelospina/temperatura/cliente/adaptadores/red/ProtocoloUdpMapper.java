package samuelospina.temperatura.cliente.adaptadores.red;

import java.util.Locale;
import java.util.Objects;
import samuelospina.temperatura.cliente.dominio.excepciones.RespuestaServidorException;
import samuelospina.temperatura.cliente.dominio.modelos.ResultadoConversion;
import samuelospina.temperatura.cliente.dominio.vo.Celsius;

/** Traduce entre objetos del dominio y el texto que viaja en cada datagrama. */
public final class ProtocoloUdpMapper {

  public static final String CONECTAR = "CONECTAR";
  public static final String DESCONECTAR = "DESCONECTAR";

  public String serializarConversion(final Celsius celsius) {
    Objects.requireNonNull(celsius, "La temperatura es obligatoria.");
    return String.format(Locale.US, "CONVERTIR;%.2f", celsius.valor());
  }

  public void validarConexion(final String respuesta) {
    if (Objects.isNull(respuesta) || !respuesta.startsWith("CONECTADO_OK;")) {
      throw new RespuestaServidorException("Respuesta de conexión inesperada: " + respuesta);
    }
  }

  public ResultadoConversion parsearConversion(final String respuesta) {
    if (Objects.isNull(respuesta) || respuesta.isBlank()) {
      throw new RespuestaServidorException("El servidor envió una respuesta vacía.");
    }
    if (respuesta.startsWith("ERROR;")) {
      throw new RespuestaServidorException(respuesta.substring("ERROR;".length()));
    }
    final String[] partes = respuesta.split(";", -1);
    if (partes.length != 3 || !"OK_CONVERSION".equals(partes[0])) {
      throw new RespuestaServidorException("Respuesta no reconocida: " + respuesta);
    }
    try {
      return new ResultadoConversion(Double.parseDouble(partes[1]), Double.parseDouble(partes[2]));
    } catch (final NumberFormatException excepcion) {
      throw new RespuestaServidorException("El servidor envió valores no numéricos: " + respuesta);
    }
  }
}
