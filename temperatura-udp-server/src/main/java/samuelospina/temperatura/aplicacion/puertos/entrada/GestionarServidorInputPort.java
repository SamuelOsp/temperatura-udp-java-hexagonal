package samuelospina.temperatura.aplicacion.puertos.entrada;

/** Caso de uso: encender y apagar el servidor. */
public interface GestionarServidorInputPort {

  void iniciar(int puerto);

  void detener();

  boolean estaActivo();
}
