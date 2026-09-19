package samuelospina.temperatura.cliente.dominio.excepciones;

public final class TemperaturaIncorrectaException extends DominioException {

  public TemperaturaIncorrectaException(final String mensaje) {
    super(mensaje);
  }
}
