package samuelospina.temperatura.dominio.puertos.salida;

import samuelospina.temperatura.dominio.enums.EstadoServidor;
import samuelospina.temperatura.dominio.modelos.EventoServidor;

/** Puerto de salida: avisar hacia afuera (la GUI) lo que ocurre en el servidor. */
public interface PuertoNotificacionEvento {

  void notificarEvento(EventoServidor evento);

  void notificarEstado(EstadoServidor estado);
}
