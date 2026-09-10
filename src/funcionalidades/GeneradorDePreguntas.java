package funcionalidades;

import java.util.ArrayList;
import java.util.List;

import datos.ColorPelo;
import datos.Genero;
import datos.Pregunta;
import datos.TipoAtributo;

// Genera el universo completo de preguntas posibles (una por cada valor de
// cada TipoAtributo). Son siempre las mismas 9 preguntas fijas:
// 2 de género + 2 de calvicie + 2 de lentes + 3 de color de pelo.
// Tanto la estrategia Greedy como la de Entropía arrancan de esta misma
// lista y después cada una filtra/puntúa a su manera.
public class GeneradorDePreguntas {

    // O(1): siempre son 9 preguntas, no depende de la cantidad de candidatos.
    public static List<Pregunta> generarTodasLasPreguntasPosibles() {
        List<Pregunta> preguntas = new ArrayList<>();

        for (Genero genero : Genero.values()) {
            preguntas.add(new Pregunta(TipoAtributo.GENERO, genero));
        }

        preguntas.add(new Pregunta(TipoAtributo.CALVICIE, Boolean.TRUE));
        preguntas.add(new Pregunta(TipoAtributo.CALVICIE, Boolean.FALSE));

        preguntas.add(new Pregunta(TipoAtributo.LENTES, Boolean.TRUE));
        preguntas.add(new Pregunta(TipoAtributo.LENTES, Boolean.FALSE));

        for (ColorPelo color : ColorPelo.values()) {
            preguntas.add(new Pregunta(TipoAtributo.COLOR_PELO, color));
        }

        return preguntas;
    }
}
