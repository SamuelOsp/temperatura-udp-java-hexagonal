package samuelospina.temperatura.adaptadores.red.mapper;

import java.util.Objects;
import samuelospina.temperatura.adaptadores.red.response.UdpResponse;
import samuelospina.temperatura.dominio.modelos.RespuestaCliente;

/**
 * Serializa una respuesta del dominio al protocolo de texto que viaja en el datagrama:
 * CONECTADO_OK;msg · DESCONECTADO_OK;msg · OK_CONVERSION;celsius;fahrenheit · ERROR;msg
 */
public final class UdpNetworkMapper {

  public UdpResponse toNetworkResponse(final RespuestaCliente respuesta) {
    Objects.requireNonNull(respuesta, "No se puede serializar una respuesta nula.");
    final String payload =
        switch (respuesta.tipo()) {
          case CONECTADO -> "CONECTADO_OK;" + respuesta.mensaje();
          case DESCONECTADO -> "DESCONECTADO_OK;" + respuesta.mensaje();
          case OK_CONVERSION ->
              "OK_CONVERSION;"
                  + respuesta.resultado().celsiusFormateado()
                  + ";"
                  + respuesta.resultado().fahrenheitFormateado();
          case ERROR ->
              "ERROR;" + (Objects.nonNull(respuesta.mensaje()) ? respuesta.mensaje() : "Error desconocido.");
        };
    return new UdpResponse(payload, respuesta.destinatario().ip(), respuesta.destinatario().puerto());
  }
}
