package samuelospina.temperatura.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import samuelospina.temperatura.cliente.adaptadores.red.AdaptadorClienteUdp;
import samuelospina.temperatura.cliente.adaptadores.red.CanalUdp;
import samuelospina.temperatura.cliente.adaptadores.red.ProtocoloUdpMapper;
import samuelospina.temperatura.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.temperatura.cliente.aplicacion.dto.ConvertirCommand;
import samuelospina.temperatura.cliente.aplicacion.excepciones.ClienteRedException;
import samuelospina.temperatura.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.temperatura.cliente.aplicacion.servicios.ClienteTemperaturaService;

/** Prueba el cliente real contra un servidor UDP de mentira que responde lo esperado. */
class ClienteUdpIntegracionTest {

  private static ClienteTemperaturaService nuevoCliente() {
    return new ClienteTemperaturaService(
        new AdaptadorClienteUdp(new CanalUdp(), new ProtocoloUdpMapper()), new ClienteMapper());
  }

  @Test
  void conectaConvierteYDesconecta() throws Exception {
    try (DatagramSocket servidorFalso = new DatagramSocket(0)) {
      final Thread hilo = new Thread(() -> {
        try {
          for (String respuesta : new String[] {"CONECTADO_OK;listo", "OK_CONVERSION;25.00;77.00"}) {
            final DatagramPacket p = new DatagramPacket(new byte[2048], 2048);
            servidorFalso.receive(p);
            final byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);
            servidorFalso.send(new DatagramPacket(datos, datos.length, p.getAddress(), p.getPort()));
          }
        } catch (final Exception ignorada) {
          // el socket se cierra al final de la prueba
        }
      });
      hilo.start();

      final ClienteTemperaturaService cliente = nuevoCliente();
      cliente.conectar(new ConectarCommand("127.0.0.1", String.valueOf(servidorFalso.getLocalPort())));
      assertTrue(cliente.estaConectado());
      assertEquals(77.0, cliente.convertir(new ConvertirCommand("25")).fahrenheit(), 1e-9);
      cliente.desconectar();
      assertFalse(cliente.estaConectado());
    }
  }

  @Test
  void sinServidorFallaPorTimeout() throws Exception {
    final int puertoMudo;
    try (DatagramSocket s = new DatagramSocket(0)) {
      puertoMudo = s.getLocalPort();
    }
    final ClienteTemperaturaService cliente = nuevoCliente();
    assertThrows(ClienteRedException.class,
        () -> cliente.conectar(new ConectarCommand("127.0.0.1", String.valueOf(puertoMudo))));
    assertThrows(ClienteRedException.class, () -> cliente.convertir(new ConvertirCommand("10")));
  }
}
