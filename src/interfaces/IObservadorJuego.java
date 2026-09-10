package interfaces;

// Patrón Observer bien simple: el MotorJuego (que vive en funcionalidades y
// NO puede importar Swing) avisa por acá cada vez que pasa algo importante.
// La GUI se registra como observadora y así se entera de los cambios sin que
// la lógica del juego dependa para nada de la parte visual.
public interface IObservadorJuego {

    // Se llama cada vez que cambia el estado de la partida (se hizo una pregunta,
    // se filtraron candidatos, cambió el turno, etc). La GUI, al recibir este
    // aviso, relee el estado actual desde el MotorJuego y repinta.
    void onEstadoActualizado();

    // Se llama una sola vez, cuando la partida terminó (gano alguien, perdió
    // por arriesgar mal, o se acabaron las preguntas).
    void onFinDePartida(String mensajeResultado);
}
