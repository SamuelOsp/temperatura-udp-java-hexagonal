package samuelospina.temperatura;

import javax.swing.SwingUtilities;
import samuelospina.temperatura.adaptadores.red.AdaptadorControlServidorRed;
import samuelospina.temperatura.adaptadores.red.AdaptadorNotificacionEvento;
import samuelospina.temperatura.adaptadores.red.AdaptadorSalidaUdp;
import samuelospina.temperatura.adaptadores.red.CanalUdp;
import samuelospina.temperatura.adaptadores.red.mapper.UdpNetworkMapper;
import samuelospina.temperatura.aplicacion.mapper.PeticionMapper;
import samuelospina.temperatura.aplicacion.puertos.entrada.GestionarServidorInputPort;
import samuelospina.temperatura.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import samuelospina.temperatura.aplicacion.servicios.GestionarServidorService;
import samuelospina.temperatura.aplicacion.servicios.ProcesarPeticionUdpService;
import samuelospina.temperatura.dominio.puertos.salida.ControladorServidorRedPort;
import samuelospina.temperatura.dominio.puertos.salida.PuertoSalidaRed;
import samuelospina.temperatura.entrypoint.gui.ServidorFrame;
import samuelospina.temperatura.entrypoint.udp.ReceptorPeticionesUdp;

/**
 * Composition Root: el único lugar que conoce todas las piezas concretas y las conecta. El resto
 * del código depende solo de interfaces (puertos).
 *
 * @author Samuel David Ospina De Avila
 */
public final class Main {

  private Main() {}

  public static void main(final String[] args) {
    // 1. Infraestructura de red (adaptadores de salida)
    final CanalUdp canal = new CanalUdp();
    final AdaptadorNotificacionEvento notificador = new AdaptadorNotificacionEvento();
    final PuertoSalidaRed salidaRed = new AdaptadorSalidaUdp(canal, new UdpNetworkMapper(), notificador);

    // 2. Casos de uso
    final ProcesarPeticionUdpInputPort procesarPeticion =
        new ProcesarPeticionUdpService(salidaRed, notificador, new PeticionMapper());

    // 3. Entrypoint UDP y su control
    final ReceptorPeticionesUdp receptor = new ReceptorPeticionesUdp(canal, procesarPeticion, notificador);
    final ControladorServidorRedPort controlRed =
        new AdaptadorControlServidorRed(canal, receptor::iniciar, receptor::detener);
    final GestionarServidorInputPort gestionarServidor =
        new GestionarServidorService(controlRed, notificador);

    // 4. Entrypoint GUI en el hilo de Swing
    SwingUtilities.invokeLater(() -> {
      final ServidorFrame frame = new ServidorFrame(gestionarServidor);
      notificador.registrarObservador(frame);
      frame.setVisible(true);
    });
  }
}
