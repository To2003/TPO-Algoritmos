package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import datos.Personaje;
import funcionalidades.Benchmark;
import funcionalidades.MotorJuego;
import funcionalidades.Razonador;
import interfaces.IObservadorJuego;

// La ventana principal. Coordina los distintos paneles (menú, selección de
// personaje, juego) usando un CardLayout, y hace de "traductor" entre lo que
// hace el MotorJuego y lo que se ve en pantalla. Es la única clase de la GUI
// que conoce al MotorJuego; el resto de los paneles son controles tontos.
public class VentanaPrincipal extends JFrame implements IObservadorJuego {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedor = new JPanel(cardLayout);

    private final Razonador razonador = new Razonador();
    private MotorJuego motor;

    private PanelTablero tableroSeleccion;
    private Personaje personajeElegido;
    private MotorJuego.Modo modoElegido;

    private PanelTablero tableroPropio;
    private PanelTablero tableroRival;
    private PanelEstado panelEstado;
    private PanelPreguntas panelPreguntas;
    private JPanel panelJuegoActual;

    private boolean turnoMaquinaEnCurso = false;

    public VentanaPrincipal() {
        super("Adivina Quién - TP Diseño y Análisis de Algoritmos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);

        // El motor se crea una sola vez: adentro arma y ordena el mazo de 23 personajes.
        motor = new MotorJuego(razonador);
        motor.agregarObservador(this);

        contenedor.add(construirPanelMenu(), "menu");
        add(contenedor, BorderLayout.CENTER);

        mostrarMenu();
    }

    private void mostrarMenu() {
        cardLayout.show(contenedor, "menu");
    }

    // ======================= PANEL MENU =======================

    private JPanel construirPanelMenu() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titulo = new JLabel("¿A qué querés jugar?", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(22f));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(4, 1, 10, 10));
        botones.setBorder(BorderFactory.createEmptyBorder(40, 150, 40, 150));

        JButton btnM1 = new JButton("Jugador vs Máquina 1 (Greedy Minimax)");
        btnM1.addActionListener((ActionEvent e) -> iniciarSeleccionDePersonaje(MotorJuego.Modo.JUGADOR_VS_MAQUINA1));

        JButton btnM2 = new JButton("Jugador vs Máquina 2 (Entropía)");
        btnM2.addActionListener((ActionEvent e) -> iniciarSeleccionDePersonaje(MotorJuego.Modo.JUGADOR_VS_MAQUINA2));

        JButton btnDuelo = new JButton("Máquina 1 vs Máquina 2");
        btnDuelo.addActionListener((ActionEvent e) -> iniciarDueloDeMaquinas());

        JButton btnBenchmark = new JButton("Correr benchmark MergeSort vs InsertionSort (consola)");
        btnBenchmark.addActionListener((ActionEvent e) -> new Thread(Benchmark::ejecutar).start());

        botones.add(btnM1);
        botones.add(btnM2);
        botones.add(btnDuelo);
        botones.add(btnBenchmark);

        panel.add(botones, BorderLayout.CENTER);
        return panel;
    }

    // ======================= SELECCION DE PERSONAJE =======================

    private void iniciarSeleccionDePersonaje(MotorJuego.Modo modo) {
        this.modoElegido = modo;
        this.personajeElegido = null;

        JPanel panelSeleccion = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Elegí tu personaje secreto (click en una carta)", SwingConstants.CENTER);
        panelSeleccion.add(label, BorderLayout.NORTH);

        tableroSeleccion = new PanelTablero("Elegí tu personaje", motor.getMazo().getPersonajes());
        tableroSeleccion.actualizarCandidatos(motor.getMazo().getPersonajes()); // todos vivos, ninguno descartado

        JButton confirmar = new JButton("Confirmar y arrancar");
        confirmar.setEnabled(false);

        tableroSeleccion.setAlHacerClick(p -> {
            personajeElegido = p;
            tableroSeleccion.marcarSeleccionado(p);
            confirmar.setEnabled(true);
        });

        confirmar.addActionListener(e -> {
            // Precondición del enunciado: no se puede arrancar sin personaje elegido.
            // El botón ya viene deshabilitado hasta elegir, pero igual lo validamos acá.
            if (personajeElegido == null) {
                JOptionPane.showMessageDialog(this, "Tenés que elegir un personaje primero.");
                return;
            }
            arrancarPartidaConJugador();
        });

        JPanel sur = new JPanel();
        sur.add(confirmar);
        panelSeleccion.add(tableroSeleccion, BorderLayout.CENTER);
        panelSeleccion.add(sur, BorderLayout.SOUTH);

        contenedor.add(panelSeleccion, "seleccion");
        cardLayout.show(contenedor, "seleccion");
    }

    // ======================= PARTIDA JUGADOR VS MAQUINA =======================

    private void arrancarPartidaConJugador() {
        motor.iniciarPartida(modoElegido, personajeElegido);
        construirPanelDeJuegoConJugador();
        cardLayout.show(contenedor, "juego");
    }

    private void construirPanelDeJuegoConJugador() {
        JPanel panelJuego = new JPanel(new BorderLayout());

        JPanel tableros = new JPanel(new GridLayout(1, 2, 10, 10));
        tableroPropio = new PanelTablero("Lo que sabés de la máquina", motor.getMazo().getPersonajes());
        tableroRival = new PanelTablero("Lo que sabe la máquina de vos", motor.getMazo().getPersonajes());
        tableros.add(tableroPropio);
        tableros.add(tableroRival);

        panelEstado = new PanelEstado();
        panelPreguntas = new PanelPreguntas();

        panelPreguntas.setAlPreguntar(pregunta -> {
            try {
                motor.jugadorPregunta(pregunta);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        panelPreguntas.setAlArriesgar(apuesta -> {
            try {
                motor.jugadorArriesga(apuesta);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        panelJuego.add(tableros, BorderLayout.CENTER);
        panelJuego.add(panelEstado, BorderLayout.EAST);
        panelJuego.add(panelPreguntas, BorderLayout.SOUTH);

        reemplazarPanelDeJuego(panelJuego);
        actualizarVistaJuegoConJugador();
    }

    // Sacamos el panel de juego viejo si existía (por si se juega una segunda
    // partida en la misma corrida del programa) y ponemos el nuevo en su lugar.
    private void reemplazarPanelDeJuego(JPanel panelNuevo) {
        if (panelJuegoActual != null) {
            contenedor.remove(panelJuegoActual);
        }
        panelJuegoActual = panelNuevo;
        contenedor.add(panelJuegoActual, "juego");
    }

    private void actualizarVistaJuegoConJugador() {
        tableroPropio.actualizarCandidatos(motor.getCandidatosJugadorSobreMaquina());
        tableroRival.actualizarCandidatos(motor.getCandidatosMaquinaSobreJugador());

        panelEstado.actualizar(
                motor.isTurnoDelJugador() ? "tuyo" : motor.getNombreMaquinaActual(),
                motor.getCandidatosJugadorSobreMaquina().size(),
                motor.getCandidatosMaquinaSobreJugador().size(),
                motor.getHistorialPreguntasJugador(),
                motor.getHistorialPreguntasMaquina());

        boolean turnoDelJugador = motor.isTurnoDelJugador() && !motor.isPartidaTerminada();
        panelPreguntas.setHabilitado(turnoDelJugador);
        panelPreguntas.setPreguntasDisponibles(motor.getPreguntasDisponiblesParaJugador());
        panelPreguntas.setCandidatosParaArriesgar(motor.getCandidatosJugadorSobreMaquina());

        // Si el jugador se quedó sin preguntas para hacer, lo avisamos: solo le queda arriesgar.
        if (turnoDelJugador && motor.debeForzarArriesgue()) {
            panelEstado.setTurno("tuyo (sin preguntas disponibles, ¡tenés que arriesgar!)");
        }

        // Si es el turno de la máquina, la disparamos en un hilo aparte (tiene sleeps
        // adentro por el Razonador) para no congelar la interfaz.
        if (!motor.isTurnoDelJugador() && !motor.isPartidaTerminada() && !turnoMaquinaEnCurso) {
            turnoMaquinaEnCurso = true;
            new Thread(() -> {
                motor.ejecutarTurnoMaquina();
                turnoMaquinaEnCurso = false;
            }).start();
        }
    }

    // ======================= MAQUINA 1 VS MAQUINA 2 =======================

    private void iniciarDueloDeMaquinas() {
        JPanel panelJuego = new JPanel(new BorderLayout());

        JPanel tableros = new JPanel(new GridLayout(1, 2, 10, 10));
        tableroPropio = new PanelTablero("Lo que sabe MAQUINA 1 de MAQUINA 2", motor.getMazo().getPersonajes());
        tableroRival = new PanelTablero("Lo que sabe MAQUINA 2 de MAQUINA 1", motor.getMazo().getPersonajes());
        tableros.add(tableroPropio);
        tableros.add(tableroRival);

        panelEstado = new PanelEstado();

        panelJuego.add(tableros, BorderLayout.CENTER);
        panelJuego.add(panelEstado, BorderLayout.EAST);

        reemplazarPanelDeJuego(panelJuego);
        cardLayout.show(contenedor, "juego");

        new Thread(motor::iniciarDueloDeMaquinas).start();
    }

    private void actualizarVistaDuelo() {
        tableroPropio.actualizarCandidatos(motor.getCandidatosM1SobreM2());
        tableroRival.actualizarCandidatos(motor.getCandidatosM2SobreM1());
        panelEstado.setTurno("Maquina 1 lleva " + motor.getTurnosMaquina1() + " turnos, Maquina 2 lleva " + motor.getTurnosMaquina2());
        panelEstado.setCandidatosPropios(motor.getCandidatosM1SobreM2().size());
        panelEstado.setCandidatosRivales(motor.getCandidatosM2SobreM1().size());
    }

    // ======================= OBSERVADOR =======================

    @Override
    public void onEstadoActualizado() {
        // El motor puede llamar esto desde un hilo de fondo (los turnos de máquina
        // tienen sleeps); toda actualización visual tiene que pasar por el EDT.
        SwingUtilities.invokeLater(() -> {
            if (motor.getModo() == MotorJuego.Modo.MAQUINA1_VS_MAQUINA2) {
                actualizarVistaDuelo();
            } else {
                actualizarVistaJuegoConJugador();
            }
        });
    }

    @Override
    public void onFinDePartida(String mensajeResultado) {
        SwingUtilities.invokeLater(() -> {
            if (motor.getModo() == MotorJuego.Modo.MAQUINA1_VS_MAQUINA2) {
                actualizarVistaDuelo();
            } else {
                actualizarVistaJuegoConJugador();
            }
            JOptionPane.showMessageDialog(this, mensajeResultado, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
            mostrarMenu();
        });
    }
}
