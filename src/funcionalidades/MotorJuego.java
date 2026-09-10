package funcionalidades;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import datos.Mazo;
import datos.Personaje;
import datos.Pregunta;
import interfaces.IEstrategiaIA;
import interfaces.IObservadorJuego;

// El coordinador central del juego. Acá viven las reglas: de quién es el
// turno, qué se puede preguntar, cuándo se termina la partida y cómo se
// encadenan las máquinas. Esta clase (y todo el paquete funcionalidades)
// NO importa nada de javax.swing: la GUI se entera de los cambios via
// IObservadorJuego, nunca al revés.
public class MotorJuego {

    // Los 3 modos de juego que pide el enunciado.
    public enum Modo {
        JUGADOR_VS_MAQUINA1,
        JUGADOR_VS_MAQUINA2,
        MAQUINA1_VS_MAQUINA2
    }

    private final Mazo mazo;
    private final Razonador razonador;
    private final List<IObservadorJuego> observadores = new ArrayList<>();
    private final Random random = new Random();

    private Modo modo;

    // --- Estado de la partida Jugador vs Máquina ---
    private PersonajeSecreto secretoJugador;
    private PersonajeSecreto secretoMaquina;
    private List<Personaje> candidatosMaquinaSobreJugador; // lo que la maquina activa sabe del jugador
    private List<Personaje> candidatosJugadorSobreMaquina;  // lo que el jugador sabe de la maquina activa
    private final List<Pregunta> historialPreguntasMaquina = new ArrayList<>();
    private List<Pregunta> historialPreguntasJugador = new ArrayList<>();
    private IEstrategiaIA estrategiaActual;
    private String nombreMaquinaActual;
    private int turnoNumero;
    private boolean esTurnoDelJugador;
    private boolean partidaTerminada;
    private boolean yaHuboTransicionDeMaquina;

    // --- Estado de la partida Máquina 1 vs Máquina 2 (independiente del anterior) ---
    private int turnosMaquina1;
    private int turnosMaquina2;
    private List<Personaje> candidatosM1SobreM2;
    private List<Personaje> candidatosM2SobreM1;
    private String ganadorDuelo;

    public MotorJuego(Razonador razonador) {
        this.razonador = razonador;
        this.mazo = new Mazo();
        // La máquina ordena el mazo (que arranca agrupado solo por género) con MergeSort
        // y lo renumera según la posición final. Recién ahí el mazo queda "listo para jugar".
        this.mazo.ordenarYNumerar(new MergeSort());
    }

    public void agregarObservador(IObservadorJuego observador) {
        observadores.add(observador);
    }

    private void notificarEstado() {
        for (IObservadorJuego o : observadores) {
            o.onEstadoActualizado();
        }
    }

    private void notificarFin(String mensaje) {
        for (IObservadorJuego o : observadores) {
            o.onFinDePartida(mensaje);
        }
    }

    // ======================= MODOS JUGADOR VS MAQUINA =======================

    // Precondición del enunciado: no se puede arrancar sin personaje elegido.
    public void iniciarPartida(Modo modo, Personaje personajeElegidoPorJugador) {
        if (personajeElegidoPorJugador == null) {
            throw new IllegalArgumentException("Tenés que elegir tu personaje antes de arrancar la partida.");
        }
        if (modo == Modo.MAQUINA1_VS_MAQUINA2) {
            throw new IllegalArgumentException("Para Maquina1 vs Maquina2 hay que usar iniciarDueloDeMaquinas().");
        }

        this.modo = modo;
        this.secretoJugador = new PersonajeSecreto(personajeElegidoPorJugador);
        this.secretoMaquina = new PersonajeSecreto(elegirPersonajeAlAzar());

        this.candidatosMaquinaSobreJugador = new ArrayList<>(mazo.getPersonajes());
        this.candidatosJugadorSobreMaquina = new ArrayList<>(mazo.getPersonajes());
        this.historialPreguntasMaquina.clear();
        this.historialPreguntasJugador = new ArrayList<>();

        cambiarEstrategia(modo == Modo.JUGADOR_VS_MAQUINA1 ? 1 : 2);

        this.turnoNumero = 1;
        this.esTurnoDelJugador = true; // el jugador arranca preguntando
        this.partidaTerminada = false;
        this.yaHuboTransicionDeMaquina = false;

        notificarEstado();
    }

    private Personaje elegirPersonajeAlAzar() {
        List<Personaje> personajes = mazo.getPersonajes();
        return personajes.get(random.nextInt(personajes.size()));
    }

    private void cambiarEstrategia(int numeroMaquina) {
        if (numeroMaquina == 1) {
            estrategiaActual = new EstrategiaGreedyMinimax(razonador);
            nombreMaquinaActual = "MAQUINA 1";
        } else {
            estrategiaActual = new EstrategiaEntropia(razonador);
            nombreMaquinaActual = "MAQUINA 2";
        }
    }

    // Preguntas que el jugador todavía puede hacer (le sacamos las que ya usó
    // esta partida, así la GUI no le deja repetir una pregunta).
    public List<Pregunta> getPreguntasDisponiblesParaJugador() {
        List<Pregunta> todas = GeneradorDePreguntas.generarTodasLasPreguntasPosibles();
        List<Pregunta> disponibles = new ArrayList<>();
        for (Pregunta p : todas) {
            if (!historialPreguntasJugador.contains(p)) {
                disponibles.add(p);
            }
        }
        return disponibles;
    }

    public void jugadorPregunta(Pregunta pregunta) {
        validarPartidaEnCurso();
        validarTurno(true);
        if (historialPreguntasJugador.contains(pregunta)) {
            throw new IllegalStateException("Ya hiciste esa pregunta en esta partida, elegí otra.");
        }

        boolean respuesta = secretoMaquina.responder(pregunta);
        historialPreguntasJugador.add(pregunta);
        candidatosJugadorSobreMaquina = Mazo.filtrar(candidatosJugadorSobreMaquina, pregunta, respuesta);
        validarCandidatosNoVacios(candidatosJugadorSobreMaquina, "jugador sobre maquina");

        esTurnoDelJugador = false;
        notificarEstado();
    }

    public void jugadorArriesga(Personaje apuesta) {
        validarPartidaEnCurso();
        validarTurno(true);

        boolean ganoJugador = secretoMaquina.esIgualA(apuesta);
        String mensaje = ganoJugador
                ? "¡Ganaste! Adivinaste que era " + apuesta.getNombre() + "."
                : "Perdiste: arriesgaste \"" + apuesta.getNombre() + "\" pero era " + secretoMaquina.getNombreParaMostrar() + ".";

        resolverFinDeRonda(mensaje, ganoJugador);
    }

    // Ejecuta un turno completo de la máquina activa: evalúa si conviene arriesgar
    // o, si no, elige la mejor pregunta según su estrategia y la "hace".
    public void ejecutarTurnoMaquina() {
        validarPartidaEnCurso();
        validarTurno(false);

        razonador.iniciarTurno(nombreMaquinaActual, turnoNumero, candidatosMaquinaSobreJugador.size());

        if (estrategiaActual.debeArriesgar(candidatosMaquinaSobreJugador)) {
            Personaje apuesta = candidatosMaquinaSobreJugador.get(0);
            razonador.mostrarArriesgue(nombreMaquinaActual, apuesta);

            boolean ganoMaquina = secretoJugador.esIgualA(apuesta);
            boolean ganoJugador = !ganoMaquina;
            String mensaje = ganoMaquina
                    ? nombreMaquinaActual + " arriesgó y GANÓ: era " + apuesta.getNombre() + "."
                    : nombreMaquinaActual + " arriesgó \"" + apuesta.getNombre() + "\" y PERDIÓ: era " + secretoJugador.getNombreParaMostrar() + ".";

            resolverFinDeRonda(mensaje, ganoJugador);
            return;
        }

        Pregunta pregunta = estrategiaActual.elegirPregunta(candidatosMaquinaSobreJugador);
        boolean respuesta = secretoJugador.responder(pregunta);
        razonador.mostrarRespuesta(respuesta);

        historialPreguntasMaquina.add(pregunta);
        int antes = candidatosMaquinaSobreJugador.size();
        candidatosMaquinaSobreJugador = Mazo.filtrar(candidatosMaquinaSobreJugador, pregunta, respuesta);
        validarCandidatosNoVacios(candidatosMaquinaSobreJugador, "maquina sobre jugador");
        razonador.mostrarDescarte(antes - candidatosMaquinaSobreJugador.size(), candidatosMaquinaSobreJugador);

        turnoNumero++;
        esTurnoDelJugador = true;
        notificarEstado();
    }

    // Si ganó el jugador y todavía no pasamos por la otra máquina, en vez de terminar
    // la partida entera encadenamos automáticamente con ella (con ventaja heredada).
    // En cualquier otro caso (ganó la máquina, o ya hicimos la segunda ronda), ahí sí
    // se termina la partida de verdad.
    private void resolverFinDeRonda(String mensaje, boolean ganoJugador) {
        if (ganoJugador && !yaHuboTransicionDeMaquina) {
            razonador.mostrarResultadoFinal(mensaje + " Arranca la otra máquina, con lo que ya averiguó la anterior.");
            transicionarASegundaMaquina();
            return;
        }
        partidaTerminada = true;
        razonador.mostrarResultadoFinal(mensaje);
        notificarFin(mensaje);
    }

    private void transicionarASegundaMaquina() {
        yaHuboTransicionDeMaquina = true;

        boolean maquinaActualEsLaUno = estrategiaActual instanceof EstrategiaGreedyMinimax;
        cambiarEstrategia(maquinaActualEsLaUno ? 2 : 1);

        // La ventaja: candidatosMaquinaSobreJugador e historialPreguntasMaquina NO se
        // resetean, siguen tal cual quedaron. La máquina nueva arranca sabiendo ya
        // todo lo que la anterior le sacó al jugador.
        // Lo que sí se resetea es todo lo relacionado a adivinar a la máquina nueva,
        // porque es un personaje recién elegido que el jugador todavía no vio.
        this.secretoMaquina = new PersonajeSecreto(elegirPersonajeAlAzar());
        this.candidatosJugadorSobreMaquina = new ArrayList<>(mazo.getPersonajes());
        this.historialPreguntasJugador = new ArrayList<>();

        this.turnoNumero = 1;
        this.esTurnoDelJugador = true;
        this.partidaTerminada = false;

        notificarEstado();
    }

    // ======================= VALIDACIONES =======================

    private void validarPartidaEnCurso() {
        if (partidaTerminada) {
            throw new IllegalStateException("La partida ya terminó.");
        }
    }

    private void validarTurno(boolean seEsperaTurnoDelJugador) {
        if (esTurnoDelJugador != seEsperaTurnoDelJugador) {
            throw new IllegalStateException("No es tu turno.");
        }
    }

    // Esto, en teoría, nunca debería pasar: los 23 personajes son todos distintos entre
    // sí (Mazo los valida al armarse), así que siempre hay algún filtro que los separa.
    // Si en algún momento se llega a vaciar la lista, es un bug de filtrado real y
    // preferimos frenar fuerte (con log) en vez de seguir jugando con un estado roto.
    private void validarCandidatosNoVacios(List<Personaje> candidatos, String contexto) {
        if (candidatos.isEmpty()) {
            System.err.println("[BUG] Los candidatos (" + contexto + ") quedaron en 0. Revisar el filtrado.");
            throw new IllegalStateException("Error interno: no quedan candidatos posibles (" + contexto + ").");
        }
    }

    // ======================= MODO MAQUINA 1 VS MAQUINA 2 =======================

    // Corre una partida completa entre las dos máquinas, sin intervención humana.
    // Cada una tiene su propio personaje secreto y su propia lista de candidatos
    // sobre la otra. Se van turnando hasta que alguna arriesga (bien o mal).
    // Pensada para correrse en un hilo aparte (tiene sleeps del Razonador adentro).
    public void iniciarDueloDeMaquinas() {
        this.modo = Modo.MAQUINA1_VS_MAQUINA2;

        PersonajeSecreto secretoM1 = new PersonajeSecreto(elegirPersonajeAlAzar());
        PersonajeSecreto secretoM2 = new PersonajeSecreto(elegirPersonajeAlAzar());

        IEstrategiaIA estrategiaM1 = new EstrategiaGreedyMinimax(razonador);
        IEstrategiaIA estrategiaM2 = new EstrategiaEntropia(razonador);

        candidatosM1SobreM2 = new ArrayList<>(mazo.getPersonajes());
        candidatosM2SobreM1 = new ArrayList<>(mazo.getPersonajes());
        turnosMaquina1 = 0;
        turnosMaquina2 = 0;
        ganadorDuelo = null;
        partidaTerminada = false;

        boolean jugoMaquina1 = true;
        notificarEstado();

        while (ganadorDuelo == null) {
            if (jugoMaquina1) {
                turnosMaquina1++;
                razonador.iniciarTurno("MAQUINA 1", turnosMaquina1, candidatosM1SobreM2.size());

                if (estrategiaM1.debeArriesgar(candidatosM1SobreM2)) {
                    Personaje apuesta = candidatosM1SobreM2.get(0);
                    razonador.mostrarArriesgue("MAQUINA 1", apuesta);
                    ganadorDuelo = secretoM2.esIgualA(apuesta) ? "MAQUINA 1" : "MAQUINA 2";
                } else {
                    Pregunta pregunta = estrategiaM1.elegirPregunta(candidatosM1SobreM2);
                    boolean respuesta = secretoM2.responder(pregunta);
                    razonador.mostrarRespuesta(respuesta);
                    int antes = candidatosM1SobreM2.size();
                    candidatosM1SobreM2 = Mazo.filtrar(candidatosM1SobreM2, pregunta, respuesta);
                    validarCandidatosNoVacios(candidatosM1SobreM2, "maquina1 sobre maquina2");
                    razonador.mostrarDescarte(antes - candidatosM1SobreM2.size(), candidatosM1SobreM2);
                }
            } else {
                turnosMaquina2++;
                razonador.iniciarTurno("MAQUINA 2", turnosMaquina2, candidatosM2SobreM1.size());

                if (estrategiaM2.debeArriesgar(candidatosM2SobreM1)) {
                    Personaje apuesta = candidatosM2SobreM1.get(0);
                    razonador.mostrarArriesgue("MAQUINA 2", apuesta);
                    ganadorDuelo = secretoM1.esIgualA(apuesta) ? "MAQUINA 2" : "MAQUINA 1";
                } else {
                    Pregunta pregunta = estrategiaM2.elegirPregunta(candidatosM2SobreM1);
                    boolean respuesta = secretoM1.responder(pregunta);
                    razonador.mostrarRespuesta(respuesta);
                    int antes = candidatosM2SobreM1.size();
                    candidatosM2SobreM1 = Mazo.filtrar(candidatosM2SobreM1, pregunta, respuesta);
                    validarCandidatosNoVacios(candidatosM2SobreM1, "maquina2 sobre maquina1");
                    razonador.mostrarDescarte(antes - candidatosM2SobreM1.size(), candidatosM2SobreM1);
                }
            }

            jugoMaquina1 = !jugoMaquina1;
            notificarEstado();
        }

        String mensaje = "Ganó " + ganadorDuelo + ". Turnos usados -> MAQUINA 1: " + turnosMaquina1
                + " | MAQUINA 2: " + turnosMaquina2;
        partidaTerminada = true;
        razonador.mostrarResultadoFinal(mensaje);
        notificarFin(mensaje);
    }

    // ======================= GETTERS PARA LA GUI =======================

    public Mazo getMazo() {
        return mazo;
    }

    public Modo getModo() {
        return modo;
    }

    public List<Personaje> getCandidatosMaquinaSobreJugador() {
        return candidatosMaquinaSobreJugador;
    }

    public List<Personaje> getCandidatosJugadorSobreMaquina() {
        return candidatosJugadorSobreMaquina;
    }

    public List<Pregunta> getHistorialPreguntasMaquina() {
        return historialPreguntasMaquina;
    }

    public List<Pregunta> getHistorialPreguntasJugador() {
        return historialPreguntasJugador;
    }

    public int getTurnoNumero() {
        return turnoNumero;
    }

    public boolean isTurnoDelJugador() {
        return esTurnoDelJugador;
    }

    public boolean isPartidaTerminada() {
        return partidaTerminada;
    }

    public String getNombreMaquinaActual() {
        return nombreMaquinaActual;
    }

    public boolean debeForzarArriesgue() {
        return getPreguntasDisponiblesParaJugador().isEmpty();
    }

    public List<Personaje> getCandidatosM1SobreM2() {
        return candidatosM1SobreM2;
    }

    public List<Personaje> getCandidatosM2SobreM1() {
        return candidatosM2SobreM1;
    }

    public int getTurnosMaquina1() {
        return turnosMaquina1;
    }

    public int getTurnosMaquina2() {
        return turnosMaquina2;
    }
}
