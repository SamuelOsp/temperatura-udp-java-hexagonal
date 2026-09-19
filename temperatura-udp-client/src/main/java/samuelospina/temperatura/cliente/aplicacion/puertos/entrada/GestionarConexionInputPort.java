package samuelospina.temperatura.cliente.aplicacion.puertos.entrada;

import samuelospina.temperatura.cliente.aplicacion.dto.ConectarCommand;

/** Caso de uso: verificar que el servidor responde y terminar la sesión. */
public interface GestionarConexionInputPort {

  void conectar(ConectarCommand comando);

  void desconectar();

  boolean estaConectado();
}
