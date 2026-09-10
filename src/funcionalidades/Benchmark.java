package funcionalidades;

import java.util.List;

import datos.Personaje;
import defaults.ArmadorDeMazo;
import defaults.Constantes;

// Compara el tiempo de MergeSort contra InsertionSort ordenando la misma
// lista de 23 personajes muchas veces, para juntar datos para el informe.
public class Benchmark {

    public static void ejecutar() {
        List<Personaje> listaBase = ArmadorDeMazo.generarMazoBase();

        MergeSort mergeSort = new MergeSort();
        InsertionSort insertionSort = new InsertionSort();

        System.out.println("Corriendo benchmark de MergeSort vs InsertionSort (n = " + listaBase.size() + ")");
        System.out.println("Warm-up: " + Constantes.BENCHMARK_WARMUP_ITERACIONES + " iteraciones descartadas...");

        // Warm-up: las primeras corridas de cualquier método en la JVM son lentas porque
        // el bytecode se está interpretando; recién después de un rato el JIT lo compila
        // a código nativo. Si no calentamos, estaríamos midiendo ese "arranque en frío"
        // mezclado con el algoritmo, y el número nos quedaría inflado y poco representativo.
        for (int i = 0; i < Constantes.BENCHMARK_WARMUP_ITERACIONES; i++) {
            mergeSort.ordenar(listaBase);
            insertionSort.ordenar(listaBase);
        }

        System.out.println("Midiendo " + Constantes.BENCHMARK_REPETICIONES + " repeticiones de cada algoritmo...");

        long totalNanosMerge = 0;
        for (int i = 0; i < Constantes.BENCHMARK_REPETICIONES; i++) {
            long inicio = System.nanoTime();
            mergeSort.ordenar(listaBase);
            totalNanosMerge += System.nanoTime() - inicio;
        }

        long totalNanosInsertion = 0;
        for (int i = 0; i < Constantes.BENCHMARK_REPETICIONES; i++) {
            long inicio = System.nanoTime();
            insertionSort.ordenar(listaBase);
            totalNanosInsertion += System.nanoTime() - inicio;
        }

        double promedioMergeNs = (double) totalNanosMerge / Constantes.BENCHMARK_REPETICIONES;
        double promedioInsertionNs = (double) totalNanosInsertion / Constantes.BENCHMARK_REPETICIONES;

        imprimirTabla(promedioMergeNs, promedioInsertionNs);
    }

    private static void imprimirTabla(double promedioMergeNs, double promedioInsertionNs) {
        System.out.println();
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.printf("%-15s %15s %15s%n", "Algoritmo", "Promedio (µs)", "Promedio (ms)");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.printf("%-15s %15.3f %15.6f%n", "MergeSort", promedioMergeNs / 1000.0, promedioMergeNs / 1_000_000.0);
        System.out.printf("%-15s %15.3f %15.6f%n", "InsertionSort", promedioInsertionNs / 1000.0, promedioInsertionNs / 1_000_000.0);
        System.out.println("──────────────────────────────────────────────────────────");

        // Con n=23 chiquito, InsertionSort puede llegar a ganarle a MergeSort en tiempo real
        // (tiene menos overhead de recursión y creación de sublistas), aunque en teoría
        // O(n^2) sea peor que O(n log n). Esto también da para discutir en la defensa.
        if (promedioMergeNs < promedioInsertionNs) {
            System.out.println("MergeSort fue mas rapido en este n.");
        } else {
            System.out.println("InsertionSort fue mas rapido en este n (esperable: n=23 es chico y MergeSort");
            System.out.println("tiene overhead de recursion/particionado que a esta escala no se paga solo).");
        }
        System.out.println();
    }
}
