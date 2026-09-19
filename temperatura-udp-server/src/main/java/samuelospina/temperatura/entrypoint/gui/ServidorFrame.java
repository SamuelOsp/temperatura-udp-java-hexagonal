package samuelospina.temperatura.entrypoint.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import samuelospina.temperatura.adaptadores.red.ObservadorServidor;
import samuelospina.temperatura.aplicacion.excepciones.ServidorRedException;
import samuelospina.temperatura.aplicacion.puertos.entrada.GestionarServidorInputPort;
import samuelospina.temperatura.dominio.enums.EstadoServidor;
import samuelospina.temperatura.dominio.excepciones.DominioException;
import samuelospina.temperatura.dominio.modelos.EventoServidor;

/**
 * Entrypoint GUI del servidor. Solo conoce el puerto de entrada GestionarServidorInputPort para
 * encender/apagar, y recibe los eventos como ObservadorServidor. No toca sockets.
 *
 * @author Samuel David Ospina De Avila
 */
public final class ServidorFrame extends JFrame implements ObservadorServidor {

  private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

  private final transient GestionarServidorInputPort gestionarServidor;

  private CardLayout tarjetas;
  private JPanel contenedor;
  private Glass.Segmentado segmentado;
  private Glass.Pildora btnIniciar;
  private Glass.Campo campoIp;
  private Glass.Campo campoPuerto;
  private JLabel txtEstado;
  private JTextArea cajaLog;

  public ServidorFrame(final GestionarServidorInputPort gestionarServidor) {
    this.gestionarServidor = Objects.requireNonNull(gestionarServidor, "El puerto de entrada es obligatorio.");
    initComponents();
    campoIp.setText(ipLocal());
  }

  private void initComponents() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle("Conversor de Temperatura · Servidor UDP");

    final Glass.Fondo fondo = new Glass.Fondo();
    fondo.setLayout(new BorderLayout(0, 18));
    fondo.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
    setContentPane(fondo);
    fondo.add(construirCabecera(), BorderLayout.NORTH);

    tarjetas = new CardLayout();
    contenedor = new JPanel(tarjetas);
    contenedor.setOpaque(false);
    contenedor.add(construirPanelConexion(), "conexion");
    contenedor.add(construirPanelLog(), "log");

    segmentado = new Glass.Segmentado(
        new String[] {"CONEXION", "LOG DE PETICIONES"},
        i -> tarjetas.show(contenedor, i == 0 ? "conexion" : "log"));

    final JPanel centro = new JPanel(new BorderLayout(0, 16));
    centro.setOpaque(false);
    centro.add(segmentado, BorderLayout.NORTH);
    centro.add(contenedor, BorderLayout.CENTER);
    fondo.add(centro, BorderLayout.CENTER);

    setMinimumSize(new Dimension(700, 560));
    setSize(getMinimumSize());
    setLocationRelativeTo(null);
  }

  private JPanel construirCabecera() {
    final JPanel cabecera = new JPanel(new BorderLayout());
    cabecera.setOpaque(false);

    final JPanel titulos = new JPanel();
    titulos.setOpaque(false);
    titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

    final JLabel titulo = new JLabel("Servidor de Temperatura");
    titulo.setFont(Glass.fuente(Font.BOLD, 27f));
    titulo.setForeground(Glass.TINTA);

    final JLabel subtitulo = new JLabel("DatagramSocket UDP  ·  Arquitectura Hexagonal  ·  Ejercicio 3");
    subtitulo.setFont(Glass.fuente(Font.PLAIN, 12f));
    subtitulo.setForeground(Glass.TINTA_SUAVE);

    titulos.add(titulo);
    titulos.add(Box.createVerticalStrut(5));
    titulos.add(subtitulo);
    cabecera.add(titulos, BorderLayout.WEST);

    final JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    derecha.setOpaque(false);
    derecha.add(new Glass.Chip("UDP", Glass.FRIO));
    derecha.add(new Glass.Chip("C  →  F", Glass.CALOR));
    cabecera.add(derecha, BorderLayout.EAST);
    return cabecera;
  }

  private JPanel construirPanelConexion() {
    final Glass.Tarjeta panel = new Glass.Tarjeta();
    panel.setLayout(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

    final GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(9, 0, 9, 12);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;

    campoIp = new Glass.Campo("", Glass.CALOR);
    campoIp.setEditable(false);
    campoPuerto = new Glass.Campo("9007", Glass.CALOR);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    panel.add(Glass.rotulo("DIRECCION IP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoIp, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0;
    panel.add(Glass.rotulo("PUERTO UDP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoPuerto, c);

    btnIniciar = new Glass.Pildora("INICIAR", Glass.VERDE);
    btnIniciar.addActionListener(evt -> alternarServidor());
    c.gridx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(16, 0, 9, 0);
    panel.add(btnIniciar, c);

    txtEstado = new JLabel("DETENIDO");
    txtEstado.setFont(Glass.fuente(Font.BOLD, 13f));
    txtEstado.setForeground(Glass.ROJO);

    final JPanel estado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
    estado.setOpaque(false);
    estado.add(new Glass.Punto(txtEstado));
    estado.add(txtEstado);

    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 2;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(10, 0, 0, 0);
    panel.add(estado, c);

    final JLabel ayuda = new JLabel("<html><div style='width:450px'>Un único DatagramSocket atiende a "
        + "todos los clientes: no hay conexión ni un hilo por cliente. Cada datagrama trae la IP y el "
        + "puerto de quien lo envió, y a esa dirección se devuelve la respuesta.</div></html>");
    ayuda.setFont(Glass.fuente(Font.PLAIN, 11f));
    ayuda.setForeground(Glass.TINTA_SUAVE);
    c.gridy = 4;
    c.weighty = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    panel.add(ayuda, c);
    return panel;
  }

  private JPanel construirPanelLog() {
    final Glass.Tarjeta panel = new Glass.Tarjeta();
    panel.setLayout(new BorderLayout(0, 14));
    panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

    cajaLog = new JTextArea();
    cajaLog.setEditable(false);
    cajaLog.setRows(10);
    cajaLog.setLineWrap(true);
    cajaLog.setWrapStyleWord(true);
    cajaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    cajaLog.setOpaque(false);
    cajaLog.setForeground(new Color(0x9FB4C7));
    cajaLog.setCaretColor(Glass.VERDE);
    cajaLog.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

    final JScrollPane scroll = new JScrollPane(cajaLog);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // La consola va dentro de su propia lámina de vidrio, más oscura
    final Glass.Tarjeta consola = new Glass.Tarjeta(18) {
      @Override
      protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(6, 11, 25, 190));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.dispose();
      }
    };
    consola.setLayout(new BorderLayout());
    consola.add(scroll, BorderLayout.CENTER);
    panel.add(consola, BorderLayout.CENTER);

    final Glass.Pildora btnLimpiar = new Glass.Pildora("LIMPIAR", new Color(0x263140));
    btnLimpiar.addActionListener(evt -> cajaLog.setText(""));
    final JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    pie.setOpaque(false);
    pie.add(btnLimpiar);
    panel.add(pie, BorderLayout.SOUTH);
    return panel;
  }

  // ------------------------------------------------------------------
  // Interacción con el núcleo: solo a través del puerto de entrada
  // ------------------------------------------------------------------
  private void alternarServidor() {
    if (gestionarServidor.estaActivo()) {
      gestionarServidor.detener();
      return;
    }
    final int puerto;
    try {
      puerto = Integer.parseInt(campoPuerto.getText().trim());
    } catch (final NumberFormatException excepcion) {
      advertir("El PUERTO UDP debe ser un número entero.");
      return;
    }
    try {
      gestionarServidor.iniciar(puerto);
      segmentado.setSeleccion(1);
    } catch (final DominioException | ServidorRedException excepcion) {
      advertir(excepcion.getMessage());
    }
  }

  private void advertir(final String mensaje) {
    JOptionPane.showMessageDialog(this, mensaje, "Servidor UDP", JOptionPane.WARNING_MESSAGE);
  }

  private static String ipLocal() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (final UnknownHostException excepcion) {
      return "127.0.0.1";
    }
  }

  // ------------------------------------------------------------------
  // ObservadorServidor: llega desde el hilo receptor, se pinta en el EDT
  // ------------------------------------------------------------------
  @Override
  public void alRecibirEvento(final EventoServidor evento) {
    final String linea = String.format("[%s] %s  %s%n",
        evento.hora().format(HORA), evento.origen(), evento.descripcion());
    SwingUtilities.invokeLater(() -> {
      cajaLog.append(linea);
      cajaLog.setCaretPosition(cajaLog.getDocument().getLength());
    });
  }

  @Override
  public void alCambiarEstado(final EstadoServidor estado) {
    SwingUtilities.invokeLater(() -> {
      final boolean escuchando = estado == EstadoServidor.ESCUCHANDO;
      btnIniciar.setText(escuchando ? "DETENER" : "INICIAR");
      btnIniciar.setBackground(escuchando ? Glass.ROJO : Glass.VERDE);
      campoPuerto.setEditable(!escuchando);
      txtEstado.setText(escuchando ? "ESCUCHANDO · UDP " + campoPuerto.getText().trim() : "DETENIDO");
      txtEstado.setForeground(escuchando ? Glass.VERDE : Glass.ROJO);
      repaint();
    });
  }
}
