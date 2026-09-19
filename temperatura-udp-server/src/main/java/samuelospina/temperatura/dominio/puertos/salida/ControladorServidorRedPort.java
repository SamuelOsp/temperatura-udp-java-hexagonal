package samuelospina.temperatura.dominio.puertos.salida;

import samuelospina.temperatura.dominio.vo.PuertoRed;

/** Puerto de salida: abrir y cerrar la infraestructura de red que recibe peticiones. */
public interface ControladorServidorRedPort {

  void iniciar(PuertoRed puerto);

  void detener();

  boolean estaActivo();
}
