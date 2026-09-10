package funcionalidades;

import java.util.List;

import datos.Mazo;
import datos.Personaje;
import datos.Pregunta;
import interfaces.IEstrategiaIA;

// Máquina 2. Elige la pregunta que maximiza la ganancia de información de
// Shannon: cuánto "desorden" (entropía) le sacamos al conjunto de candidatos
// al hacer esa pregunta. Además tiene una heurística de riesgo propia:
// si quedan 2 candidatos, prefiere arriesgar directo (50% de acierto) en vez
// de gastar un turno preguntando, cosa que la Máquina 1 no hace (ella espera
// a tener 1 solo candidato). Esto da lugar a comparar ambos estilos de juego.
public class EstrategiaEntropia implements IEstrategiaIA {

    private final Razonador razonador;

    public EstrategiaEntropia(Razonador razonador) {
        this.razonador = razonador;
    }

    // O(p * n): igual que la Greedy, para cada una de las p preguntas posibles
    // recorremos los n candidatos para partirlos y calcular la entropía de cada lado.
    @Override
    public Pregunta elegirPregunta(List<Personaje> candidatos) {
        List<Pregunta> preguntasPosibles = GeneradorDePreguntas.generarTodasLasPreguntasPosibles();

        // H(S) sin haber preguntado nada todavía: como los n candidatos son igual de
        // probables, se simplifica a log2(n) (ver el comentario del método log2).
        double entropiaActual = log2(candidatos.size());

        Pregunta mejorPregunta = null;
        double mejorGanancia = -1;

        if (razonador != null) {
            razonador.mostrarInicioEvaluacion();
        }

        for (Pregunta pregunta : preguntasPosibles) {
            List<Personaje> cumplen = Mazo.filtrar(candidatos, pregunta, true);
            List<Personaje> noCumplen = Mazo.filtrar(candidatos, pregunta, false);

            if (cumplen.isEmpty() || noCumplen.isEmpty()) {
                continue;
            }

            double pSi = (double) cumplen.size() / candidatos.size();
            double pNo = (double) noCumplen.size() / candidatos.size();

            // H(Sí) y H(No) son log2 de la cantidad de elementos de cada grupo, porque
            // dentro de cada grupo seguimos sin tener ninguna otra pista (distribución uniforme).
            double entropiaCondicional = pSi * log2(cumplen.size()) + pNo * log2(noCumplen.size());
            double ganancia = entropiaActual - entropiaCondicional;

            boolean esLaMejorHastaAhora = ganancia > mejorGanancia;

            if (razonador != null) {
                razonador.mostrarEvaluacion(pregunta.toString(), cumplen.size(), noCumplen.size(),
                        String.format("ganancia: %.3f bits", ganancia), esLaMejorHastaAhora);
            }

            if (esLaMejorHastaAhora) {
                mejorGanancia = ganancia;
                mejorPregunta = pregunta;
            }
        }

        if (mejorPregunta != null && razonador != null) {
            razonador.mostrarDecision(mejorPregunta.toString(),
                    String.format("maximiza la ganancia de información (%.3f bits)", mejorGanancia));
        }

        return mejorPregunta;
    }

    // H(S) = -Σ p·log2(p). Como acá cada candidato es igual de probable (1/n cada uno),
    // se simplifica a H(S) = log2(n). O(1) porque Math.log ya viene calculado en tiempo constante.
    private double log2(int cantidad) {
        if (cantidad <= 1) return 0;
        return Math.log(cantidad) / Math.log(2);
    }

    // Heurística propia de la Máquina 2: con 2 candidatos, la mitad de las veces
    // el próximo "no" te deja con 1 solo candidato igual (mismo resultado que arriesgar
    // ahora), así que no vale la pena gastar el turno: arriesga directo (50% de pegarla).
    @Override
    public boolean debeArriesgar(List<Personaje> candidatos) {
        return candidatos.size() <= 2;
    }
}
