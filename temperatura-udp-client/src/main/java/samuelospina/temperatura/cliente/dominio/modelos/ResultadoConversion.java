package samuelospina.temperatura.cliente.dominio.modelos;

/** Lo que el servidor devolvió: la temperatura en ambas unidades. */
public record ResultadoConversion(double celsius, double fahrenheit) {}
