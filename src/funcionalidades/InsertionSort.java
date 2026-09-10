package funcionalidades;

import java.util.ArrayList;
import java.util.List;

import datos.Personaje;
import interfaces.IOrdenador;

// Implementación a mano de InsertionSort. La usamos solo para comparar contra
// MergeSort en el Benchmark, en el juego real siempre ordenamos con MergeSort.
public class InsertionSort implements IOrdenador {

    // OJO: esto es O(n^2) en el peor caso (lista al revés), porque para cada
    // elemento puede haber que correrlo hasta el principio de la lista ya ordenada.
    // Con n=23 igual anda rapidísimo, pero no escala como MergeSort.
    @Override
    public List<Personaje> ordenar(List<Personaje> lista) {
        List<Personaje> resultado = new ArrayList<>(lista);

        for (int i = 1; i < resultado.size(); i++) {
            Personaje actual = resultado.get(i);
            int j = i - 1;

            // Corremos el elemento "actual" hacia atrás mientras sea menor que el anterior.
            while (j >= 0 && ComparadorClave.comparar(resultado.get(j), actual) > 0) {
                resultado.set(j + 1, resultado.get(j));
                j--;
            }
            resultado.set(j + 1, actual);
        }

        return resultado;
    }
}
