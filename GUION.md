# Guion del video de sustentación (unos 8–10 min)

## 0. Presentación (30 s)
"Soy Samuel David Ospina De Avila. Este es el Ejercicio 3, conversión de Celsius a Fahrenheit,
ahora con UDP/IP y Arquitectura Hexagonal."

## 1. Demo (2 min)
1. Terminal: `./mvnw package` en el servidor y mostrar que las pruebas pasan.
2. `java -jar target/temperatura-udp-server.jar` → **INICIAR** en 9007 → estado ESCUCHANDO.
3. Abrir el cliente → **Conectar** a `localhost:9007`. Se va a la pestaña CONVERTIR.
4. Convertir 100 (212 °F), -40 (-40 °F) y 37 (98.6 °F). Mostrar el **log del servidor**: cada línea
   trae la IP y el **puerto efímero** del cliente.
5. Errores: escribir `abc` y `-300` (lo valida el dominio del cliente).
6. **Detener** el servidor y convertir otra vez: tras 3 s aparece el aviso de timeout. Esto no pasa
   en TCP y muestra que UDP no garantiza entrega.
7. (Opcional) Abrir dos clientes a la vez: el mismo socket del servidor atiende a ambos sin hilos extra.

## 2. UDP en el código (3 min)
- **Servidor, `adaptadores/red/CanalUdp.java`**: un solo `new DatagramSocket(puerto)`. No hay `accept()`.
- **`entrypoint/udp/ReceptorPeticionesUdp.java`**: hilo con `receive(paquete)`. De cada paquete
  saco el texto, `getAddress()` y `getPort()`, o sea a quién responder. Arma un
  `ProcesarPeticionUdpCommand` y llama al puerto de entrada.
- **`adaptadores/red/AdaptadorSalidaUdp.java`**: `send()` de un `DatagramPacket` dirigido a esa IP y
  ese puerto.
- **Cliente, `adaptadores/red/CanalUdp.java`**: `new DatagramSocket()` en un puerto efímero,
  `setSoTimeout(3000)`, e `intercambiar()` (envía y espera respuesta, y descarta datagramas que
  no vengan del servidor).
- **`AdaptadorClienteUdp.conectar()`**: como UDP no tiene conexión, "conectar" es un saludo
  `CONECTAR → CONECTADO_OK`. Mostrar el protocolo en `UdpNetworkMapper` / `ProtocoloUdpMapper`.
- Detener: cerrar el socket desbloquea `receive()` con una `SocketException`, y así termina el hilo.

## 3. Hexagonal en el código (3 min)
- Recorrer las carpetas: `dominio` → `aplicacion` → `adaptadores` → `entrypoint`.
- **Dominio**: `Celsius` (value object, cero absoluto) y `Conversion` (la fórmula, único lugar).
  No importa nada de `java.net` ni de Swing.
- **Puertos**: de entrada (`ProcesarPeticionUdpInputPort`, `GestionarServidorInputPort`) y de
  salida (`PuertoSalidaRed`, `PuertoNotificacionEvento`, `ControladorServidorRedPort`).
- **Servicio** `ProcesarPeticionUdpService`: interpreta el comando, llama al dominio y responde
  por `PuertoSalidaRed` sin saber que existe UDP.
- **Observer**: `AdaptadorNotificacionEvento` avisa a `ServidorFrame`, que pinta el log con `invokeLater`.
- **`Main` (composition root)**: aquí se enchufa todo. Mostrar que la GUI recibe solo interfaces.
- **Prueba**: `ProcesarPeticionUdpServiceTest` usa el servicio con puertos falsos, sin red. Esa es
  la ventaja de la hexagonal: el núcleo se prueba solo.
- Cierre: "del 1er corte al 2º, la fórmula no cambió. Solo cambiaron los adaptadores. Esa es la idea."
