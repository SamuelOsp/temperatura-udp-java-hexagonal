package samuelospina.temperatura.adaptadores.red;

import samuelospina.temperatura.dominio.enums.EstadoServidor;
import samuelospina.temperatura.dominio.modelos.EventoServidor;

/** Quien quiera enterarse de lo que pasa en el servidor (la GUI) implementa esta interfaz. */
public interface ObservadorServidor {

  void alRecibirEvento(EventoServidor evento);

  void alCambiarEstado(EstadoServidor estado);
}
