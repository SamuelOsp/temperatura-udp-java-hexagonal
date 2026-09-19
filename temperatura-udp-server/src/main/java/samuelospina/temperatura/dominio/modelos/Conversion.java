package samuelospina.temperatura.dominio.modelos;

import java.util.Objects;
import samuelospina.temperatura.dominio.vo.Celsius;
import samuelospina.temperatura.dominio.vo.Fahrenheit;

/**
 * Entidad central del negocio. Es el ÚNICO lugar de todo el sistema donde vive la fórmula
 * F = C × 9/5 + 32. No sabe nada de UDP, Swing ni sockets.
 */
public final class Conversion {

  private final Celsius celsius;

  public Conversion(final Celsius celsius) {
    this.celsius = Objects.requireNonNull(celsius, "La temperatura en Celsius es obligatoria.");
  }

  public ResultadoConversion convertir() {
    final double fahrenheit = celsius.valor() * 9.0 / 5.0 + 32.0;
    return new ResultadoConversion(celsius, new Fahrenheit(fahrenheit));
  }

  public Celsius getCelsius() {
    return celsius;
  }
}
