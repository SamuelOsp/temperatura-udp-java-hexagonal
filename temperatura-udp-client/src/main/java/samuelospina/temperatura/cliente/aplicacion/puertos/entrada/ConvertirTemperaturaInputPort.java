package samuelospina.temperatura.cliente.aplicacion.puertos.entrada;

import samuelospina.temperatura.cliente.aplicacion.dto.ConvertirCommand;
import samuelospina.temperatura.cliente.dominio.modelos.ResultadoConversion;

/** Caso de uso: pedirle al servidor la conversión de una temperatura. */
public interface ConvertirTemperaturaInputPort {

  ResultadoConversion convertir(ConvertirCommand comando);
}
