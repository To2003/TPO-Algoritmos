package interfaces;

import java.util.List;

import datos.Personaje;
import datos.Pregunta;

// Contrato que tiene que cumplir cualquier "cerebro" de máquina.
// Las dos implementaciones (Greedy Minimax y Entropía) difieren en el CRITERIO
// que usan para elegir pregunta y para decidir si arriesgan, pero de afuera
// (MotorJuego) se las usa exactamente igual gracias a esta interfaz.
public interface IEstrategiaIA {

    // Dado el conjunto de candidatos que todavía podrían ser el personaje rival,
    // decide cuál es la mejor pregunta para hacer a continuación.
    Pregunta elegirPregunta(List<Personaje> candidatos);

    // Decide si, en el estado actual, conviene arriesgar la suposición final
    // en vez de seguir preguntando.
    boolean debeArriesgar(List<Personaje> candidatos);
}
