package samuelospina.temperatura.cliente.entrypoint.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import samuelospina.temperatura.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.temperatura.cliente.aplicacion.dto.ConvertirCommand;
import samuelospina.temperatura.cliente.aplicacion.puertos.entrada.ConvertirTemperaturaInputPort;
import samuelospina.temperatura.cliente.aplicacion.puertos.entrada.GestionarConexionInputPort;
import samuelospina.temperatura.cliente.dominio.modelos.ResultadoConversion;

/**
 * Entrypoint GUI del cliente. Solo habla con los puertos de entrada; no sabe que existe UDP. Las
 * llamadas de red corren fuera del hilo de Swing para que la ventana no se congele mientras se
 * espera un datagrama.
 *
 * @author Samuel David Ospina De Avila
 */
public final class ClienteFrame extends JFrame {

  private final transient GestionarConexionInputPort conexion;
  private final transient ConvertirTemperaturaInputPort conversor;

  private CardLayout tarjetas;
  private JPanel contenedor;
  private Glass.Segmentado segmentado;
  private Glass.Pildora btnConectar;
  private Glass.Pildora btnConvertir;
  private Glass.Campo campoHost;
  private Glass.Campo campoPuerto;
  private Glass.Campo campoCelsius;
  private JLabel txtEstado;
  private JLabel txtResultado;
  private JLabel txtMensaje;
  private float ultimoValor;
  private Timer contador;

  public ClienteFrame(
      final GestionarConexionInputPort conexion, final ConvertirTemperaturaInputPort conversor) {
    this.conexion = Objects.requireNonNull(conexion, "El puerto de conexión es obligatorio.");
    this.conversor = Objects.requireNonNull(conversor, "El puerto de conversión es obligatorio.");
    initComponents();
  }

  private void initComponents() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle("Conversor de Temperatura · Cliente UDP");
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        if (conexion.estaConectado()) {
          try {
            conexion.desconectar();
          } catch (final RuntimeException ignorada) {
            // se está cerrando la ventana: no hay a quién avisar
          }
        }
      }
    });

    final Glass.Fondo fondo = new Glass.Fondo();
    fondo.setLayout(new BorderLayout(0, 18));
    fondo.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
    setContentPane(fondo);
    fondo.add(construirCabecera(), BorderLayout.NORTH);

    tarjetas = new CardLayout();
    contenedor = new JPanel(tarjetas);
    contenedor.setOpaque(false);
    contenedor.add(construirPanelConexion(), "conexion");
    contenedor.add(construirPanelConversion(), "convertir");

    segmentado = new Glass.Segmentado(new String[] {"CONEXION", "CONVERTIR"},
        i -> tarjetas.show(contenedor, i == 0 ? "conexion" : "convertir"));

    final JPanel centro = new JPanel(new BorderLayout(0, 16));
    centro.setOpaque(false);
    centro.add(segmentado, BorderLayout.NORTH);
    centro.add(contenedor, BorderLayout.CENTER);
    fondo.add(centro, BorderLayout.CENTER);

    setMinimumSize(new Dimension(660, 560));
    setSize(getMinimumSize());
    setLocationRelativeTo(null);
  }

  private JPanel construirCabecera() {
    final JPanel cabecera = new JPanel(new BorderLayout());
    cabecera.setOpaque(false);

    final JPanel titulos = new JPanel();
    titulos.setOpaque(false);
    titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

    final JLabel titulo = new JLabel("Conversor de Temperatura");
    titulo.setFont(Glass.fuente(Font.BOLD, 27f));
    titulo.setForeground(Glass.TINTA);

    final JLabel subtitulo = new JLabel("Cliente UDP  ·  Arquitectura Hexagonal  ·  Samuel Ospina");
    subtitulo.setFont(Glass.fuente(Font.PLAIN, 12f));
    subtitulo.setForeground(Glass.TINTA_SUAVE);

    titulos.add(titulo);
    titulos.add(Box.createVerticalStrut(5));
    titulos.add(subtitulo);
    cabecera.add(titulos, BorderLayout.WEST);

    final JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    derecha.setOpaque(false);
    derecha.add(new Glass.Chip("°C  →  °F", Glass.FRIO));
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

    campoHost = new Glass.Campo("localhost", Glass.FRIO);
    campoPuerto = new Glass.Campo("9007", Glass.FRIO);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    panel.add(Glass.rotulo("DIRECCION IP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoHost, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0;
    panel.add(Glass.rotulo("PUERTO UDP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoPuerto, c);

    btnConectar = new Glass.Pildora("Conectar", Glass.VERDE);
    btnConectar.addActionListener(evt -> alternarConexion());
    c.gridx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(16, 0, 9, 0);
    panel.add(btnConectar, c);

    txtEstado = new JLabel("Desconectado");
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

    final JLabel ayuda = new JLabel("<html><div style='width:440px'>UDP no establece conexión: "
        + "\"Conectar\" envía un datagrama CONECTAR y espera CONECTADO_OK (máx. 3 s) para comprobar "
        + "que el servidor está escuchando.</div></html>");
    ayuda.setFont(Glass.fuente(Font.PLAIN, 11f));
    ayuda.setForeground(Glass.TINTA_SUAVE);
    c.gridy = 4;
    c.weighty = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    panel.add(ayuda, c);
    return panel;
  }

  private JPanel construirPanelConversion() {
    final Glass.Tarjeta panel = new Glass.Tarjeta();
    panel.setLayout(new BorderLayout(0, 22));
    panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

    final JPanel entrada = new JPanel(new GridBagLayout());
    entrada.setOpaque(false);
    final GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, 0, 0, 12);
    c.anchor = GridBagConstraints.WEST;
    entrada.add(Glass.rotulo("TEMPERATURA (°C)"), c);

    campoCelsius = new Glass.Campo("", Glass.FRIO);
    campoCelsius.setFont(Glass.fuente(Font.BOLD, 22f));
    campoCelsius.setHorizontalAlignment(SwingConstants.CENTER);
    campoCelsius.setPreferredSize(new Dimension(130, 50));
    campoCelsius.addActionListener(evt -> convertir());
    c.gridx = 1;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    entrada.add(campoCelsius, c);

    btnConvertir = new Glass.Pildora("CONVERTIR", Glass.CALOR);
    btnConvertir.addActionListener(evt -> convertir());
    c.gridx = 2;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 0, 0, 0);
    entrada.add(btnConvertir, c);
    panel.add(entrada, BorderLayout.NORTH);

    final JPanel resultado = new JPanel();
    resultado.setOpaque(false);
    resultado.setLayout(new BoxLayout(resultado, BoxLayout.Y_AXIS));

    final JLabel rotulo = new JLabel("RESULTADO EN GRADOS FAHRENHEIT");
    rotulo.setFont(Glass.fuente(Font.BOLD, 11f));
    rotulo.setForeground(Glass.TINTA_SUAVE);
    rotulo.setAlignmentX(LEFT_ALIGNMENT);

    txtResultado = new JLabel("0.00 °F");
    txtResultado.setFont(Glass.fuente(Font.BOLD, 52f));
    txtResultado.setForeground(Glass.CALOR);
    txtResultado.setAlignmentX(LEFT_ALIGNMENT);

    txtMensaje = new JLabel("Conecte con el servidor y escriba una temperatura.");
    txtMensaje.setFont(Glass.fuente(Font.PLAIN, 13f));
    txtMensaje.setForeground(Glass.TINTA);
    txtMensaje.setAlignmentX(LEFT_ALIGNMENT);

    resultado.add(rotulo);
    resultado.add(Box.createVerticalStrut(8));
    resultado.add(txtResultado);
    resultado.add(Box.createVerticalStrut(12));
    resultado.add(txtMensaje);
    resultado.add(Box.createVerticalStrut(22));

    // Accesos rápidos: solo rellenan el campo, el cálculo sigue siendo del servidor
    final JPanel rapidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    rapidos.setOpaque(false);
    rapidos.setAlignmentX(LEFT_ALIGNMENT);
    final JLabel sugerencia = new JLabel("PRUEBE CON  ");
    sugerencia.setFont(Glass.fuente(Font.BOLD, 11f));
    sugerencia.setForeground(Glass.TINTA_SUAVE);
    rapidos.add(sugerencia);
    for (final String v : new String[] {"0", "25", "37", "100", "-40"}) {
      final Glass.Chip chip = new Glass.Chip(v + " °C", Glass.FRIO);
      chip.setFont(Glass.fuente(Font.BOLD, 11f));
      chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      chip.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(final MouseEvent e) {
          campoCelsius.setText(v);
          campoCelsius.requestFocusInWindow();
        }
      });
      rapidos.add(chip);
    }
    resultado.add(rapidos);
    panel.add(resultado, BorderLayout.CENTER);

    final JLabel pie = new JLabel("La fórmula vive en el dominio del servidor; este cliente solo valida y muestra.");
    pie.setFont(Glass.fuente(Font.ITALIC, 11f));
    pie.setForeground(Glass.TINTA_SUAVE);
    panel.add(pie, BorderLayout.SOUTH);
    return panel;
  }

  // ------------------------------------------------------------------
  // Acciones: siempre a través de los puertos de entrada
  // ------------------------------------------------------------------
  private void alternarConexion() {
    if (conexion.estaConectado()) {
      enSegundoPlano(() -> {
        conexion.desconectar();
        return null;
      }, nada -> pintarConexion(false));
      return;
    }
    final ConectarCommand comando = new ConectarCommand(campoHost.getText(), campoPuerto.getText());
    btnConectar.setEnabled(false);
    txtEstado.setText("Enviando CONECTAR...");
    enSegundoPlano(() -> {
      conexion.conectar(comando);
      return null;
    }, nada -> {
      pintarConexion(true);
      segmentado.setSeleccion(1);
    });
  }

  private void convertir() {
    final ConvertirCommand comando = new ConvertirCommand(campoCelsius.getText());
    btnConvertir.setEnabled(false);
    enSegundoPlano(() -> conversor.convertir(comando), this::mostrarResultado);
  }

  private void mostrarResultado(final ResultadoConversion resultado) {
    final float fahrenheit = (float) resultado.fahrenheit();
    txtResultado.setForeground(Glass.CALOR);
    animarResultado(ultimoValor, fahrenheit);
    ultimoValor = fahrenheit;
    txtMensaje.setText(String.format(Locale.US, "%.2f °C equivalen a %.2f °F",
        resultado.celsius(), resultado.fahrenheit()));
  }

  private void pintarConexion(final boolean conectado) {
    btnConectar.setText(conectado ? "Desconectar" : "Conectar");
    btnConectar.setBackground(conectado ? Glass.ROJO : Glass.VERDE);
    txtEstado.setText(conectado
        ? "Servidor UDP responde en " + campoHost.getText().trim() + ":" + campoPuerto.getText().trim()
        : "Desconectado");
    txtEstado.setForeground(conectado ? Glass.VERDE : Glass.ROJO);
    repaint();
  }

  /** Ejecuta la llamada de red en otro hilo y vuelve al EDT con el resultado o el error. */
  private <T> void enSegundoPlano(final Supplier<T> tarea, final Consumer<T> alTerminar) {
    new Thread(() -> {
      try {
        final T valor = tarea.get();
        SwingUtilities.invokeLater(() -> {
          reactivarBotones();
          alTerminar.accept(valor);
        });
      } catch (final RuntimeException excepcion) {
        SwingUtilities.invokeLater(() -> {
          reactivarBotones();
          pintarConexion(conexion.estaConectado());
          JOptionPane.showMessageDialog(this, excepcion.getMessage(), "Cliente UDP",
              JOptionPane.WARNING_MESSAGE);
        });
      }
    }, "Cliente-UDP").start();
  }

  private void reactivarBotones() {
    btnConectar.setEnabled(true);
    btnConvertir.setEnabled(true);
  }

  /** Animación del contador: interpola del valor anterior al nuevo (ease-out cúbico). */
  private void animarResultado(final float desde, final float hasta) {
    if (contador != null && contador.isRunning()) {
      contador.stop();
    }
    final long inicio = System.currentTimeMillis();
    final int duracion = 420;
    contador = new Timer(16, null);
    contador.addActionListener(e -> {
      final float p = Math.min(1f, (System.currentTimeMillis() - inicio) / (float) duracion);
      final float suave = 1 - (1 - p) * (1 - p) * (1 - p);
      txtResultado.setText(String.format(Locale.US, "%.2f °F", desde + (hasta - desde) * suave));
      if (p >= 1f) {
        contador.stop();
      }
    });
    contador.start();
  }
}
