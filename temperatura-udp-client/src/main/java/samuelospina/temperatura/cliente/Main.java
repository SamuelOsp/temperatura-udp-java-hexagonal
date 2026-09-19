package samuelospina.temperatura.cliente;

import javax.swing.SwingUtilities;
import samuelospina.temperatura.cliente.adaptadores.red.AdaptadorClienteUdp;
import samuelospina.temperatura.cliente.adaptadores.red.CanalUdp;
import samuelospina.temperatura.cliente.adaptadores.red.ProtocoloUdpMapper;
import samuelospina.temperatura.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.temperatura.cliente.aplicacion.servicios.ClienteTemperaturaService;
import samuelospina.temperatura.cliente.dominio.puertos.salida.ClienteUdpPort;
import samuelospina.temperatura.cliente.entrypoint.gui.ClienteFrame;

/**
 * Composition Root del cliente: crea el adaptador UDP, se lo inyecta al servicio y entrega el
 * servicio (visto como puertos de entrada) a la GUI.
 *
 * @author Samuel David Ospina De Avila
 */
public final class Main {

  private Main() {}

  public static void main(final String[] args) {
    final ClienteUdpPort clienteUdp = new AdaptadorClienteUdp(new CanalUdp(), new ProtocoloUdpMapper());
    final ClienteTemperaturaService servicio = new ClienteTemperaturaService(clienteUdp, new ClienteMapper());

    SwingUtilities.invokeLater(() -> new ClienteFrame(servicio, servicio).setVisible(true));
  }
}
