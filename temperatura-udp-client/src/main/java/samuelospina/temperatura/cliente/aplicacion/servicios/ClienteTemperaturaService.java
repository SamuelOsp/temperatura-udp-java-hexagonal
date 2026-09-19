package samuelospina.temperatura.cliente.aplicacion.servicios;

import java.util.Objects;
import samuelospina.temperatura.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.temperatura.cliente.aplicacion.dto.ConvertirCommand;
import samuelospina.temperatura.cliente.aplicacion.excepciones.ClienteRedException;
import samuelospina.temperatura.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.temperatura.cliente.aplicacion.puertos.entrada.ConvertirTemperaturaInputPort;
import samuelospina.temperatura.cliente.aplicacion.puertos.entrada.GestionarConexionInputPort;
import samuelospina.temperatura.cliente.dominio.modelos.ResultadoConversion;
import samuelospina.temperatura.cliente.dominio.puertos.salida.ClienteUdpPort;

/** Implementa los casos de uso del cliente: valida con el dominio y delega en el puerto de red. */
public final class ClienteTemperaturaService
    implements GestionarConexionInputPort, ConvertirTemperaturaInputPort {

  private final ClienteUdpPort clienteUdp;
  private final ClienteMapper mapper;

  public ClienteTemperaturaService(final ClienteUdpPort clienteUdp, final ClienteMapper mapper) {
    this.clienteUdp = Objects.requireNonNull(clienteUdp, "El puerto UDP es obligatorio.");
    this.mapper = Objects.requireNonNull(mapper, "El mapper es obligatorio.");
  }

  @Override
  public void conectar(final ConectarCommand comando) {
    clienteUdp.conectar(mapper.toDestino(comando));
  }

  @Override
  public void desconectar() {
    clienteUdp.desconectar();
  }

  @Override
  public boolean estaConectado() {
    return clienteUdp.estaConectado();
  }

  @Override
  public ResultadoConversion convertir(final ConvertirCommand comando) {
    if (!clienteUdp.estaConectado()) {
      throw new ClienteRedException("Cliente offline: conecte con el servidor primero.");
    }
    return clienteUdp.solicitarConversion(mapper.toCelsius(comando));
  }
}
