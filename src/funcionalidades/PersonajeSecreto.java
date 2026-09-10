package funcionalidades;

import datos.Personaje;
import datos.Pregunta;

// Esta clase es LA dependencia lógica que pide el enunciado: envuelve al
// Personaje elegido como secreto (sea del jugador o de una máquina) y no
// deja que nadie de afuera lo lea directo. Ni la GUI ni las estrategias de
// IA reciben nunca un Personaje "crudo" que sea el secreto de alguien:
// la única forma de sacarle información es preguntándole por SÍ/NO con
// responder(Pregunta), tal como pasa en el juego de mesa real.
public class PersonajeSecreto {

    private final Personaje personaje; // privado a propósito: no hay getter que lo devuelva

    public PersonajeSecreto(Personaje personaje) {
        this.personaje = personaje;
    }

    // O(1): delega en Pregunta.cumple, que solo mira un atributo del personaje.
    public boolean responder(Pregunta pregunta) {
        return pregunta.cumple(personaje);
    }

    // Se usa solo para poder resolver un "arriesgue" (adivinar directamente quién es).
    // Comparamos por id en vez de devolver el objeto entero: es el mínimo dato
    // necesario para decir "pegaste" o "no pegaste".
    public boolean esIgualA(Personaje apuesta) {
        return apuesta != null && apuesta.getId() == personaje.getId();
    }

    // Se usa únicamente al final de la partida, para revelar quién era.
    public String getNombreParaMostrar() {
        return personaje.getNombre();
    }
}
