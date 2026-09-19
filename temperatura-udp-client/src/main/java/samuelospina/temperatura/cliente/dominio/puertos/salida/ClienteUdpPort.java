package samuelospina.temperatura.cliente.dominio.puertos.salida;

import samuelospina.temperatura.cliente.dominio.modelos.ResultadoConversion;
import samuelospina.temperatura.cliente.dominio.vo.Celsius;
import samuelospina.temperatura.cliente.dominio.vo.DestinoServidor;

/** Puerto de salida: hablar con el servidor, sin que el núcleo sepa que es UDP. */
public interface ClienteUdpPort {

  void conectar(DestinoServidor destino);

  void desconectar();

  ResultadoConversion solicitarConversion(Celsius celsius);

  boolean estaConectado();
}
