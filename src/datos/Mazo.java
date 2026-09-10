package datos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import defaults.ArmadorDeMazo;
import defaults.Constantes;
import interfaces.IOrdenador;

// Envuelve la lista de los 23 personajes y ofrece las operaciones básicas
// que se hacen sobre el mazo: armarlo, validarlo, ordenarlo/numerarlo y
// filtrarlo por una pregunta. La lógica "compleja" de CÓMO ordenar vive en
// funcionalidades (MergeSort/InsertionSort); acá solo se orquesta.
public class Mazo {

    private List<Personaje> personajes;

    public Mazo() {
        // Arrancamos con el mazo crudo (ordenado solo por el orden en que
        // ArmadorDeMazo generó las combinaciones, que arranca agrupado por género).
        this.personajes = ArmadorDeMazo.generarMazoBase();
        validar();
    }

    // O(n) con n=23 fijo: recorre la lista una vez para chequear tamaño y
    // otra (con un Set) para chequear que no haya dos personajes idénticos.
    private void validar() {
        if (personajes.size() != Constantes.CANTIDAD_PERSONAJES) {
            throw new IllegalStateException(
                    "El mazo tiene que tener exactamente " + Constantes.CANTIDAD_PERSONAJES
                            + " personajes, pero tiene " + personajes.size());
        }

        Set<String> combinacionesVistas = new HashSet<>();
        for (Personaje p : personajes) {
            String clave = p.getGenero() + "-" + p.isCalvo() + "-" + p.usaLentes() + "-" + p.getColorPelo();
            if (!combinacionesVistas.add(clave)) {
                throw new IllegalStateException("Hay dos personajes con los mismos atributos: " + p);
            }
        }
    }

    public List<Personaje> getPersonajes() {
        return personajes;
    }

    // Le pasamos el algoritmo de ordenamiento por parámetro (MergeSort en el juego real,
    // InsertionSort en el benchmark) para no acoplar el Mazo a una implementación puntual.
    // Después de ordenar, renumeramos los id del 1 al 23 según la posición final: así
    // se cumple lo que pide el enunciado de que la máquina "reordena y numera" el mazo.
    public void ordenarYNumerar(IOrdenador ordenador) {
        personajes = ordenador.ordenar(personajes);
        for (int i = 0; i < personajes.size(); i++) {
            personajes.get(i).setId(i + 1);
        }
    }

    // O(n): recorre los candidatos actuales y se queda solo con los que responden
    // a la pregunta igual que "respuestaEsSi". Esto es el corazón del filtrado del
    // juego: cada pregunta que se hace reduce el conjunto de candidatos.
    public static List<Personaje> filtrar(List<Personaje> candidatos, Pregunta pregunta, boolean respuestaEsSi) {
        List<Personaje> resultado = new ArrayList<>();
        for (Personaje p : candidatos) {
            if (pregunta.cumple(p) == respuestaEsSi) {
                resultado.add(p);
            }
        }
        return resultado;
    }
}
