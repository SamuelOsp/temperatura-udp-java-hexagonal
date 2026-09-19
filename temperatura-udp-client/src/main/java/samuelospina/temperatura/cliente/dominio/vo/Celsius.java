package samuelospina.temperatura.cliente.dominio.vo;

import samuelospina.temperatura.cliente.dominio.excepciones.TemperaturaIncorrectaException;

/**
 * Value Object: temperatura a enviar. El cliente valida ANTES de mandar el datagrama, pero no
 * convierte nada: la fórmula vive solo en el servidor.
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
