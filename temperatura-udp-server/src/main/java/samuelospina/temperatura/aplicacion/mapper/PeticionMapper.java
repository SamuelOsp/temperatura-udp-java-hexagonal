package samuelospina.temperatura.aplicacion.mapper;

import samuelospina.temperatura.aplicacion.dto.ProcesarPeticionUdpCommand;
import samuelospina.temperatura.dominio.vo.Destinatario;

/** Traduce el comando de entrada a objetos del dominio. */
public final class PeticionMapper {

  public Destinatario toDestinatario(final ProcesarPeticionUdpCommand comando) {
    return new Destinatario(comando.ipOrigen(), comando.puertoOrigen());
  }
}
