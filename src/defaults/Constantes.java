package defaults;

import datos.ColorPelo;
import datos.Genero;

// Configuración global del juego. La dejamos toda en un solo lugar para no
// andar buscando "numeros magicos" repartidos por el código.
public class Constantes {

    // El espacio de atributos es 2 (genero) x 2 (calvicie) x 2 (lentes) x 3 (color de pelo) = 24
    // combinaciones posibles, pero el TP pide 23 personajes. Entonces generamos las 24 y
    // descartamos exactamente UNA, la que definimos acá abajo. Así los 23 que quedan son
    // todos únicos e inconfundibles (nunca dos personajes con los mismos 4 atributos),
    // lo que garantiza que el juego siempre tiene solución determinística.
    public static final Genero COMBINACION_DESCARTADA_GENERO = Genero.MASCULINO;
    public static final boolean COMBINACION_DESCARTADA_CALVO = true;
    public static final boolean COMBINACION_DESCARTADA_LENTES = true;
    public static final ColorPelo COMBINACION_DESCARTADA_COLOR_PELO = ColorPelo.COLORADO;

    public static final int CANTIDAD_PERSONAJES = 23;

    // Tiempo (en milisegundos) que el Razonador espera entre cada línea que imprime,
    // para que en la demo se pueda ir leyendo el razonamiento de la IA en vivo
    // en vez de que se imprima todo de golpe. Se puede desactivar con SLEEP_HABILITADO.
    public static final int DELAY_RAZONAMIENTO_MS = 350;
    public static final boolean SLEEP_HABILITADO = true;

    // Parámetros del benchmark MergeSort vs InsertionSort.
    // El warm-up es necesario porque la JVM al principio interpreta el bytecode
    // y recién después de varias corridas el JIT compila el código a nativo;
    // si midiéramos desde la primera corrida estaríamos midiendo también ese
    // "calentamiento" y no el algoritmo en sí.
    public static final int BENCHMARK_WARMUP_ITERACIONES = 10_000;
    // A n=23 una sola corrida tarda nanosegundos y el ruido del sistema operativo
    // (context switches, GC, etc) pesa más que el algoritmo mismo. Por eso repetimos
    // muchas veces y promediamos.
    public static final int BENCHMARK_REPETICIONES = 1_000;
}
