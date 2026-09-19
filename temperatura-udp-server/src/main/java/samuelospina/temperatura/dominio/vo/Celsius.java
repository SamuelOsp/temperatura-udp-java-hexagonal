package samuelospina.temperatura.dominio.vo;

import samuelospina.temperatura.dominio.excepciones.TemperaturaIncorrectaException;

/**
 * Value Object: temperatura en grados Celsius. Regla de negocio: ninguna temperatura puede estar
 * por debajo del cero absoluto.
 */
public record Celsius(double valor) {

  public static final double CERO_ABSOLUTO = -273.15;

  public Celsius {
    if (!Double.isFinite(valor)) {
      throw new TemperaturaIncorrectaException("La temperatura debe ser un número finito.");
    }
    if (valor < CERO_ABSOLUTO) {
      throw new TemperaturaIncorrectaException(
          "No existen temperaturas por debajo del cero absoluto (-273.15 °C).");
    }
  }
}
