package samuelospina.temperatura.aplicacion.servicios;

import java.util.Locale;
import java.util.Objects;
import samuelospina.temperatura.aplicacion.dto.ProcesarPeticionUdpCommand;
import samuelospina.temperatura.aplicacion.mapper.PeticionMapper;
import samuelospina.temperatura.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import samuelospina.temperatura.dominio.excepciones.DominioException;
import samuelospina.temperatura.dominio.modelos.Conversion;
import samuelospina.temperatura.dominio.modelos.EventoServidor;
import samuelospina.temperatura.dominio.modelos.RespuestaCliente;
import samuelospina.temperatura.dominio.modelos.ResultadoConversion;
import samuelospina.temperatura.dominio.puertos.salida.PuertoNotificacionEvento;
import samuelospina.temperatura.dominio.puertos.salida.PuertoSalidaRed;
import samuelospina.temperatura.dominio.vo.Celsius;
import samuelospina.temperatura.dominio.vo.Destinatario;

/**
 * Caso de uso principal. Interpreta el protocolo de texto (CONECTAR, DESCONECTAR,
 * CONVERTIR;celsius), delega la conversión al dominio y responde por el puerto de salida.
 */
public final class ProcesarPeticionUdpService implements ProcesarPeticionUdpInputPort {

  private final PuertoSalidaRed puertoSalidaRed;
  private final PuertoNotificacionEvento notificador;
  private final PeticionMapper peticionMapper;

  public ProcesarPeticionUdpService(
      final PuertoSalidaRed puertoSalidaRed,
      final PuertoNotificacionEvento notificador,
      final PeticionMapper peticionMapper) {
    this.puertoSalidaRed = Objects.requireNonNull(puertoSalidaRed, "El puerto de salida es obligatorio.");
    this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
    this.peticionMapper = Objects.requireNonNull(peticionMapper, "El mapper es obligatorio.");
  }

  @Override
  public void procesar(final ProcesarPeticionUdpCommand comando) {
    Objects.requireNonNull(comando, "El comando es obligatorio.");
    final Destinatario destinatario = peticionMapper.toDestinatario(comando);
    final String texto = Objects.isNull(comando.mensaje()) ? "" : comando.mensaje().trim();
    final String orden = texto.toUpperCase(Locale.ROOT);

    if (orden.isEmpty()) {
      responderError(destinatario, "Mensaje vacío recibido.", "datos recibidos --> [vacío]");
    } else if (orden.equals("CONECTAR")) {
      puertoSalidaRed.enviarRespuesta(
          RespuestaCliente.conectado(destinatario, "Servidor UDP listo para convertir temperaturas"));
      notificar(destinatario, "CONECTAR --> CONECTADO_OK");
    } else if (orden.equals("DESCONECTAR")) {
      puertoSalidaRed.enviarRespuesta(RespuestaCliente.desconectado(destinatario, "Sesión finalizada"));
      notificar(destinatario, "DESCONECTAR --> sesión finalizada");
    } else if (orden.startsWith("CONVERTIR;")) {
      procesarConversion(destinatario, texto);
    } else {
      responderError(
          destinatario,
          "Comando no reconocido por el servidor UDP.",
          "datos recibidos --> comando desconocido [" + texto + "]");
    }
  }

  private void procesarConversion(final Destinatario destinatario, final String texto) {
    final String[] partes = texto.split(";", -1);
    if (partes.length != 2) {
      responderError(
          destinatario,
          "Formato inválido. Se esperaba CONVERTIR;celsius",
          "datos recibidos --> formato incorrecto [" + texto + "]");
      return;
    }
    try {
      final double valor = Double.parseDouble(partes[1].trim().replace(',', '.'));
      final ResultadoConversion resultado = new Conversion(new Celsius(valor)).convertir();

      puertoSalidaRed.enviarRespuesta(RespuestaCliente.conversionExitosa(destinatario, resultado));
      notificar(
          destinatario,
          "recibido " + resultado.celsiusFormateado() + " °C --> enviado "
              + resultado.fahrenheitFormateado() + " °F");
    } catch (final NumberFormatException excepcion) {
      responderError(
          destinatario,
          "La temperatura debe ser numérica.",
          "datos recibidos --> no numérico [" + partes[1] + "]");
    } catch (final DominioException excepcion) {
      responderError(
          destinatario,
          excepcion.getMessage(),
          "datos recibidos --> validación fallida: " + excepcion.getMessage());
    }
  }

  private void responderError(
      final Destinatario destinatario, final String mensaje, final String descripcionLog) {
    puertoSalidaRed.enviarRespuesta(RespuestaCliente.error(destinatario, mensaje));
    notificar(destinatario, descripcionLog);
  }

  private void notificar(final Destinatario destinatario, final String descripcion) {
    notificador.notificarEvento(new EventoServidor("EVENTO", destinatario.endpoint(), descripcion));
  }
}
