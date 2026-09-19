package samuelospina.temperatura.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samuelospina.temperatura.adaptadores.red.mapper.UdpNetworkMapper;
import samuelospina.temperatura.aplicacion.dto.ProcesarPeticionUdpCommand;
import samuelospina.temperatura.aplicacion.mapper.PeticionMapper;
import samuelospina.temperatura.aplicacion.servicios.ProcesarPeticionUdpService;
import samuelospina.temperatura.dominio.enums.EstadoServidor;
import samuelospina.temperatura.dominio.modelos.EventoServidor;
import samuelospina.temperatura.dominio.puertos.salida.PuertoNotificacionEvento;

/** El caso de uso se prueba sin red: los puertos de salida se reemplazan por dobles en memoria. */
class ProcesarPeticionUdpServiceTest {

  private final List<String> enviados = new ArrayList<>();
  private final List<EventoServidor> eventos = new ArrayList<>();
  private ProcesarPeticionUdpService servicio;

  @BeforeEach
  void preparar() {
    final UdpNetworkMapper mapper = new UdpNetworkMapper();
    final PuertoNotificacionEvento notificador = new PuertoNotificacionEvento() {
      @Override
      public void notificarEvento(final EventoServidor evento) {
        eventos.add(evento);
      }

      @Override
      public void notificarEstado(final EstadoServidor estado) {}
    };
    servicio = new ProcesarPeticionUdpService(
        r -> enviados.add(mapper.toNetworkResponse(r).payload()), notificador, new PeticionMapper());
  }

  private String enviar(final String mensaje) {
    servicio.procesar(new ProcesarPeticionUdpCommand("127.0.0.1", 50000, mensaje));
    return enviados.get(enviados.size() - 1);
  }

  @Test
  void conectarYDesconectar() {
    assertEquals("CONECTADO_OK;Servidor UDP listo para convertir temperaturas", enviar("conectar"));
    assertEquals("DESCONECTADO_OK;Sesión finalizada", enviar("DESCONECTAR"));
    assertEquals(2, eventos.size());
  }

  @Test
  void convierte() {
    assertEquals("OK_CONVERSION;100.00;212.00", enviar("CONVERTIR;100"));
    assertEquals("OK_CONVERSION;36.50;97.70", enviar("CONVERTIR;36,5"));
  }

  @Test
  void respondeErrores() {
    assertEquals("ERROR;La temperatura debe ser numérica.", enviar("CONVERTIR;abc"));
    assertEquals("ERROR;Formato inválido. Se esperaba CONVERTIR;celsius", enviar("CONVERTIR;1;2"));
    assertEquals("ERROR;No existen temperaturas por debajo del cero absoluto (-273.15 °C).",
        enviar("CONVERTIR;-300"));
    assertEquals("ERROR;Comando no reconocido por el servidor UDP.", enviar("HOLA"));
    assertEquals("ERROR;Mensaje vacío recibido.", enviar("   "));
  }
}
