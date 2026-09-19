package samuelospina.temperatura.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import samuelospina.temperatura.dominio.excepciones.TemperaturaIncorrectaException;
import samuelospina.temperatura.dominio.modelos.Conversion;
import samuelospina.temperatura.dominio.vo.Celsius;

class ConversionTest {

  @ParameterizedTest
  @CsvSource({"0, 32", "100, 212", "37, 98.6", "-40, -40", "-273.15, -459.67"})
  void conviertePuntosConocidos(final double celsius, final double fahrenheit) {
    assertEquals(fahrenheit, new Conversion(new Celsius(celsius)).convertir().fahrenheit().valor(), 1e-9);
  }

  @ParameterizedTest
  @ValueSource(doubles = {-273.16, -300, Double.NaN, Double.POSITIVE_INFINITY})
  void rechazaTemperaturasImposibles(final double celsius) {
    assertThrows(TemperaturaIncorrectaException.class, () -> new Celsius(celsius));
  }
}
