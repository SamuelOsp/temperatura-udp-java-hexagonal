package samuelospina.temperatura.dominio.modelos;

import java.util.Locale;
import java.util.Objects;
import samuelospina.temperatura.dominio.vo.Celsius;
import samuelospina.temperatura.dominio.vo.Fahrenheit;

/** Resultado de una conversión: ambas unidades juntas. */
public record ResultadoConversion(Celsius celsius, Fahrenheit fahrenheit) {

  public ResultadoConversion {
    Objects.requireNonNull(celsius, "Los grados Celsius son obligatorios.");
    Objects.requireNonNull(fahrenheit, "Los grados Fahrenheit son obligatorios.");
  }

  public String celsiusFormateado() {
    return String.format(Locale.US, "%.2f", celsius.valor());
  }

  public String fahrenheitFormateado() {
    return String.format(Locale.US, "%.2f", fahrenheit.valor());
  }
}
