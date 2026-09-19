# Guion del video de sustentación

Lo que va entre **[corchetes]** es lo que hago en pantalla. Lo demás es lo que digo.
Duración aproximada: 9 a 10 minutos.

## 0. Preparación ANTES de grabar (no sale en el video)

Haz todo esto en orden. Cuando termines, tendrás abierto todo lo que el video necesita.

**Paso 1.** Cierra todo lo demás y sube el tamaño de letra del IDE y de la terminal.

**Paso 2. Terminal 1 (pruebas).** Abre una terminal y pega esto. Compila los dos proyectos y
ejecuta sus pruebas. La primera vez tarda porque Maven descarga cosas.

```bash
cd "/Users/samuelospina/Documents/universidad/sistemas distribuidos/UdpHexagonal-Temperatura" && clear; echo "=== SERVIDOR ===" && (cd temperatura-udp-server && ./mvnw clean package | grep -E "Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$|BUILD") && echo "=== CLIENTE ===" && (cd temperatura-udp-client && ./mvnw clean package | grep -E "Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$|BUILD")
```

Debe quedar así en pantalla. **No cierres esta terminal**, porque la vas a mostrar en el video:

```
=== SERVIDOR ===
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
=== CLIENTE ===
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Paso 3. Terminal 2 (servidor).** Abre otra terminal y pega esto. Se abre la ventana del servidor.
**No le des a INICIAR todavía**: debe quedar en DETENIDO, porque lo inicias en el video.

```bash
cd "/Users/samuelospina/Documents/universidad/sistemas distribuidos/UdpHexagonal-Temperatura/temperatura-udp-server" && java -jar target/temperatura-udp-server.jar
```

**Paso 4. Terminal 3 (cliente).** Abre una tercera terminal y pega esto. Se abre la ventana del
cliente. **No le des a Conectar todavía**: debe decir "Desconectado".

```bash
cd "/Users/samuelospina/Documents/universidad/sistemas distribuidos/UdpHexagonal-Temperatura/temperatura-udp-client" && java -jar target/temperatura-udp-client.jar
```

**Paso 5. Navegador.** Abre el README del repositorio:
https://github.com/SamuelOsp/temperatura-udp-java-hexagonal

**Paso 6. IDE.** Abre la carpeta `UdpHexagonal-Temperatura` en VS Code con las carpetas de los dos
proyectos visibles en el explorador. Los archivos se abren durante el video.

**Resumen de lo que debe estar abierto al empezar a grabar:** el navegador con el README, la
Terminal 1 con el resultado de las pruebas, la ventana del servidor (DETENIDO), la ventana del
cliente (Desconectado) y VS Code.

Si una ventana se cierra por error, vuelve a pegar el comando del Paso 3 o del Paso 4 en su
terminal.

---

## 1. Presentación (≈ 30 s)

**[Pantalla: el README del repositorio en el navegador]**

> Hola, mi nombre es Samuel David Ospina De Avila. En este video voy a presentar el taller del
> segundo corte de Sistemas Distribuidos. Es el mismo ejercicio que hice en el primer corte, el
> ejercicio 3: convertir una temperatura de grados Celsius a grados Fahrenheit. La diferencia es que
> antes lo hice con sockets TCP y ahora lo rehice con el protocolo UDP y con arquitectura hexagonal.
>
> Primero voy a mostrar la aplicación funcionando, después explico cómo está implementado UDP en el
> código y al final explico cómo está organizada la arquitectura hexagonal.

---

## 2. Demostración (≈ 3 min)

**[Mostrar la Terminal 1 con el resultado de las pruebas]**

> Antes de empezar compilé los dos proyectos. Cada uno trae el Maven Wrapper, entonces no hace
> falta instalar Maven. Al compilar se ejecutan las pruebas automáticas: el servidor tiene 14
> pruebas y el cliente 5, todas sin fallas ni errores, y los dos terminan en BUILD SUCCESS. Una de
> las pruebas del cliente comprueba qué pasa cuando el servidor no responde, que es algo que voy a
> mostrar más adelante.

**[Mostrar la ventana del servidor, que está en DETENIDO]**

> Esta es la ventana del servidor, que ya tengo abierta. Aquí aparece la IP de mi equipo y el
> puerto, que por defecto es el 9007. Le doy a Iniciar.

**[Clic en INICIAR. Se ve "ESCUCHANDO" y pasa a la pestaña del log]**

> El servidor queda escuchando en el puerto UDP 9007, y en el log aparece que el DatagramSocket
> está escuchando.

**[Mostrar la ventana del cliente, que dice "Desconectado"]**

> Y este es el cliente, que también ya tengo abierto. Dejo localhost y el puerto 9007, y le doy a
> Conectar.

**[Clic en Conectar. Pasa a la pestaña CONVERTIR]**

> Aquí hay algo importante: UDP no tiene conexión. Lo que hace este botón es enviar un datagrama con
> la palabra CONECTAR, y si el servidor responde CONECTADO_OK, sé que está vivo y escuchando.

**[Escribir 100 y CONVERTIR. Luego -40 y luego 37]**

> Escribo 100 grados Celsius, le doy Convertir, y el servidor me responde 212 grados Fahrenheit.
> Pruebo con menos 40, que da menos 40, y con 37, que da 98.6.

**[Mostrar la ventana del servidor, pestaña LOG]**

> En el log del servidor se ve cada petición: la hora, la IP y el puerto del cliente, lo que
> recibió y lo que envió. Ese puerto que aparece es un puerto temporal que el sistema operativo le
> asignó al cliente. Es la dirección de retorno que viene dentro de cada datagrama.

**[En el cliente escribir `abc`, luego `-300`]**

> Ahora pruebo errores. Si escribo letras, me dice que no es un número válido. Si escribo menos 300,
> me dice que no existen temperaturas por debajo del cero absoluto, que es menos 273.15.

**[En el servidor clic en DETENER. En el cliente escribir 25 y CONVERTIR. Esperar el aviso]**

> Y esta es la prueba que más muestra la diferencia con TCP. Detengo el servidor e intento convertir.
> El cliente espera 3 segundos y me dice que el servidor no respondió. Como UDP no garantiza que
> el paquete llegue ni que haya respuesta, yo le puse un tiempo máximo de espera al socket. Si no
> lo hiciera, el cliente se quedaría bloqueado para siempre.

**[En el servidor clic en INICIAR otra vez. En el cliente clic en Desconectar y luego en Conectar. Así todo queda funcionando antes de pasar al código]**

---

## 3. UDP en el código (≈ 3 min)

**[Abrir `temperatura-udp-server/.../adaptadores/red/CanalUdp.java`]**

> Ahora voy al código. Empiezo por el servidor. Esta clase, CanalUdp, envuelve el único
> DatagramSocket del servidor. Fíjense que aquí no hay ServerSocket ni accept como en TCP. Se crea
> un solo socket en el puerto, y ese mismo socket recibe los mensajes de todos los clientes y envía
> todas las respuestas.

**[Abrir `entrypoint/udp/ReceptorPeticionesUdp.java`, método `cicloEscucha`]**

> Esta clase es la que recibe. Tiene un hilo que está en un ciclo haciendo receive. Cuando llega un
> paquete, saco tres cosas: el texto del mensaje, la IP del que lo envió con getAddress y su puerto
> con getPort. Con eso armo un comando y se lo paso al caso de uso.
>
> En el primer corte, con TCP, yo tenía un hilo por cada cliente. Aquí no hace falta, porque cada
> datagrama es independiente y trae consigo a quién hay que responderle.

**[Abrir `adaptadores/red/AdaptadorSalidaUdp.java`]**

> Y para responder, este adaptador arma un DatagramPacket con la IP y el puerto del cliente y lo
> envía con send.

**[Abrir `adaptadores/red/mapper/UdpNetworkMapper.java`]**

> El protocolo que definí es texto separado por punto y coma. El cliente manda CONVERTIR y el valor,
> y el servidor responde OK_CONVERSION con los grados Celsius y los Fahrenheit, o ERROR con el
> mensaje. También están CONECTAR y DESCONECTAR.

**[Abrir `temperatura-udp-client/.../adaptadores/red/CanalUdp.java`]**

> En el cliente, el DatagramSocket se crea sin puerto, entonces el sistema operativo le asigna uno
> libre. Aquí está el setSoTimeout de 3000 milisegundos que mostré en la demo. Y el método
> intercambiar envía el mensaje y espera la respuesta. Además verifica que la respuesta venga del
> mismo servidor, y si llega un datagrama de otro lado lo descarta.

**[Abrir `adaptadores/red/AdaptadorClienteUdp.java`, método `conectar`]**

> Y aquí está el conectar. Envío CONECTAR, valido que la respuesta sea CONECTADO_OK y guardo la
> dirección del servidor para las siguientes peticiones. Si hay timeout o no se encuentra el host,
> lo convierto en un mensaje claro para el usuario.

---

## 4. Arquitectura hexagonal en el código (≈ 3 min)

**[En el IDE, desplegar las carpetas del servidor: dominio, aplicacion, adaptadores, entrypoint]**

> Ahora la arquitectura. Organicé los dos proyectos igual que los repositorios guía del profesor,
> en cuatro capas: dominio, aplicación, adaptadores y entrypoint. La regla principal es que las
> dependencias siempre apuntan hacia adentro, hacia el dominio.

**[Abrir `dominio/vo/Celsius.java` y `dominio/modelos/Conversion.java`]**

> El dominio es el centro. Aquí está Celsius, que es un value object: si alguien intenta crear una
> temperatura por debajo del cero absoluto, lanza una excepción. Y aquí está Conversion, que es el
> único lugar de todo el sistema donde está la fórmula: F igual a C por 9 quintos más 32.
> Fíjense en los imports: no hay nada de java.net ni de Swing. El dominio no sabe que existe UDP.

**[Abrir `dominio/puertos/salida/`]**

> Estos son los puertos de salida. Son interfaces: PuertoSalidaRed, para responder al cliente,
> PuertoNotificacionEvento, para avisar lo que pasa, y ControladorServidorRedPort, para encender y
> apagar la red. El núcleo solo conoce estas interfaces, no sus implementaciones.

**[Abrir `aplicacion/puertos/entrada/` y luego `aplicacion/servicios/ProcesarPeticionUdpService.java`]**

> En la capa de aplicación están los puertos de entrada y los casos de uso. Este servicio,
> ProcesarPeticionUdpService, recibe el comando, mira si es CONECTAR, DESCONECTAR o CONVERTIR, le
> pide al dominio que haga la conversión y responde por el puerto de salida. En ningún momento sabe
> que la respuesta viaja por UDP.

**[Volver a `adaptadores/red/`]**

> Los adaptadores son los que implementan esos puertos con tecnología concreta. AdaptadorSalidaUdp
> implementa PuertoSalidaRed usando datagramas. Y AdaptadorNotificacionEvento usa el patrón
> Observer: la ventana del servidor se registra como observador y así le llegan los eventos que
> muestra en el log.

**[Abrir `Main.java` del servidor]**

> Y todo se une en el Main, que es el composition root. Es el único lugar donde se crean las
> clases concretas y se conectan entre sí. La ventana recibe solo el puerto de entrada, no el
> servicio ni el socket.

**[Abrir `src/test/.../ProcesarPeticionUdpServiceTest.java`]**

> Una ventaja de esta arquitectura se ve en las pruebas. Aquí pruebo el caso de uso completo sin
> red: reemplazo el puerto de salida por una lista en memoria y verifico lo que el servidor habría
> respondido.

**[Desplegar rápido las carpetas del cliente]**

> El cliente sigue la misma estructura. La ventana llama a los puertos de entrada, el servicio
> valida con el dominio, y el único que sabe de UDP es AdaptadorClienteUdp.

---

## 5. Cierre (≈ 30 s)

**[Volver al README en GitHub]**

> Para terminar: entre el primer y el segundo corte, la fórmula de conversión no cambió. Lo que
> cambió fue cómo se comunican el cliente y el servidor, que pasó de TCP a UDP, y eso solo tocó los
> adaptadores y los entrypoints. Esa es justamente la idea de la arquitectura hexagonal: poder
> cambiar la tecnología sin tocar la lógica del negocio.
>
> El código está en el repositorio de GitHub que aparece en el documento. Muchas gracias.
