package samuelospina.temperatura.cliente.dominio.excepciones;

public final class RespuestaServidorException extends DominioException {

  public RespuestaServidorException(final String mensaje) {
    super(mensaje);
  }
}
