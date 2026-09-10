package funcionalidades;

import java.util.List;

import datos.Mazo;
import datos.Personaje;
import datos.Pregunta;
import interfaces.IEstrategiaIA;

// Máquina 1. Estrategia greedy tipo minimax: para cada pregunta posible,
// mira en qué dos grupos partiría a los candidatos (los que cumplen y los
// que no) y elige la que deja el PEOR CASO más chico posible. En otras
// palabras, busca la partición más parecida a un 50/50: así, sea cual sea
// la respuesta, el conjunto de candidatos se reduce lo más posible.
public class EstrategiaGreedyMinimax implements IEstrategiaIA {

    private final Razonador razonador;

    public EstrategiaGreedyMinimax(Razonador razonador) {
        this.razonador = razonador;
    }

    // O(p * n) donde p es la cantidad de preguntas posibles (siempre 9) y n la
    // cantidad de candidatos actuales: para cada pregunta recorremos una vez
    // todos los candidatos para partirlos en cumplen/no-cumplen.
    @Override
    public Pregunta elegirPregunta(List<Personaje> candidatos) {
        List<Pregunta> preguntasPosibles = GeneradorDePreguntas.generarTodasLasPreguntasPosibles();

        Pregunta mejorPregunta = null;
        int mejorPeorCaso = Integer.MAX_VALUE;

        if (razonador != null) {
            razonador.mostrarInicioEvaluacion();
        }

        for (Pregunta pregunta : preguntasPosibles) {
            List<Personaje> cumplen = Mazo.filtrar(candidatos, pregunta, true);
            List<Personaje> noCumplen = Mazo.filtrar(candidatos, pregunta, false);

            // Si una pregunta deja a todos de un mismo lado, no separa nada: no aporta
            // información y la ignoramos (no tiene sentido gastar un turno en eso).
            if (cumplen.isEmpty() || noCumplen.isEmpty()) {
                continue;
            }

            int peorCaso = Math.max(cumplen.size(), noCumplen.size());
            boolean esLaMejorHastaAhora = peorCaso < mejorPeorCaso;

            if (razonador != null) {
                razonador.mostrarEvaluacion(pregunta.toString(), cumplen.size(), noCumplen.size(),
                        "peor caso: " + peorCaso, esLaMejorHastaAhora);
            }

            if (esLaMejorHastaAhora) {
                mejorPeorCaso = peorCaso;
                mejorPregunta = pregunta;
            }
        }

        if (mejorPregunta != null && razonador != null) {
            razonador.mostrarDecision(mejorPregunta.toString(),
                    "garantiza dejar como mucho " + mejorPeorCaso + " de " + candidatos.size());
        }

        return mejorPregunta;
    }

    // O(1): solo mira el tamaño de la lista.
    @Override
    public boolean debeArriesgar(List<Personaje> candidatos) {
        return candidatos.size() == 1;
    }
}
