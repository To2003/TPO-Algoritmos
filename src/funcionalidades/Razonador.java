package funcionalidades;

import java.util.List;

import datos.Personaje;
import defaults.Constantes;

// Se encarga de imprimir por consola el "razonamiento" de la máquina turno a
// turno, para que en la demo se vea en vivo por qué elige cada pregunta.
// Es una clase de puro logging: no decide nada, solo muestra lo que las
// estrategias ya decidieron.
public class Razonador {

    // Guardamos si el sleep está habilitado en una variable de instancia (y no
    // directamente la constante) para poder desactivarlo desde la GUI en modo
    // "Maquina1 vs Maquina2" si el usuario quiere ver el resultado más rápido.
    private boolean sleepHabilitado = Constantes.SLEEP_HABILITADO;

    public void setSleepHabilitado(boolean sleepHabilitado) {
        this.sleepHabilitado = sleepHabilitado;
    }

    public void iniciarTurno(String nombreMaquina, int numeroTurno, int cantidadCandidatos) {
        imprimir("──────────────────────────────────────────────");
        imprimir("[" + nombreMaquina + "] Turno " + numeroTurno + " — candidatos restantes: " + cantidadCandidatos);
    }

    public void mostrarInicioEvaluacion() {
        imprimir("  Evaluando filtros posibles...");
    }

    // Muestra una fila de la tabla de evaluación de una pregunta candidata.
    // "esLaMejor" pinta la marca de "mejor hasta ahora" para que se note en vivo
    // cuál va ganando mientras se evalúan todas las opciones.
    public void mostrarEvaluacion(String descripcionPregunta, int cumplen, int noCumplen, String metrica, boolean esLaMejor) {
        String marca = esLaMejor ? "   <-- mejor hasta ahora" : "";
        imprimir(String.format("    %-24s -> si: %-3d | no: %-3d | %s%s",
                descripcionPregunta, cumplen, noCumplen, metrica, marca));
    }

    public void mostrarDecision(String descripcionPregunta, String justificacion) {
        imprimir("  DECISION: \"" + descripcionPregunta + "\" (" + justificacion + ")");
    }

    public void mostrarRespuesta(boolean respuestaEsSi) {
        imprimir("  Respuesta del rival: " + (respuestaEsSi ? "SI" : "NO"));
    }

    public void mostrarDescarte(int cantidadDescartada, List<Personaje> quedan) {
        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < quedan.size(); i++) {
            if (i > 0) nombres.append(", ");
            nombres.append(quedan.get(i).getNombre());
            if (i >= 5 && quedan.size() > 6) {
                nombres.append(", ... (+").append(quedan.size() - i - 1).append(" mas)");
                break;
            }
        }
        imprimir("  Descarto " + cantidadDescartada + " candidatos -> quedan: " + nombres);
    }

    public void mostrarArriesgue(String nombreMaquina, Personaje apuesta) {
        imprimir("  [" + nombreMaquina + "] Se quedó con un solo candidato posible: arriesga -> " + apuesta.getNombre());
    }

    public void mostrarResultadoFinal(String mensaje) {
        imprimir("──────────────────────────────────────────────");
        imprimir(">> " + mensaje);
        imprimir("──────────────────────────────────────────────");
    }

    private void imprimir(String linea) {
        System.out.println(linea);
        if (sleepHabilitado) {
            try {
                Thread.sleep(Constantes.DELAY_RAZONAMIENTO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
