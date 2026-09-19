package samuelospina.temperatura.aplicacion.excepciones;

/** Falla de infraestructura (por ejemplo, el puerto ya está en uso). */
public final class ServidorRedException extends RuntimeException {

  public ServidorRedException(final String mensaje, final Throwable causa) {
    super(mensaje, causa);
  }
}
