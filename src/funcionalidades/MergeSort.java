package funcionalidades;

import java.util.ArrayList;
import java.util.List;

import datos.Personaje;
import interfaces.IOrdenador;

// Implementación a mano de MergeSort (divide y conquista), sin usar
// Collections.sort ni nada de java.util que ordene por nosotros.
public class MergeSort implements IOrdenador {

    // OJO: esto es O(n log n) porque partimos la lista en log2(n) niveles de
    // recursión, y en cada nivel el merge recorre todos los elementos una sola vez.
    @Override
    public List<Personaje> ordenar(List<Personaje> lista) {
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        }

        int mitad = lista.size() / 2;
        List<Personaje> izquierda = new ArrayList<>(lista.subList(0, mitad));
        List<Personaje> derecha = new ArrayList<>(lista.subList(mitad, lista.size()));

        // Divide: ordenamos cada mitad por separado (esto es la recursión).
        izquierda = ordenar(izquierda);
        derecha = ordenar(derecha);

        // Conquista: mezclamos las dos mitades ya ordenadas en una sola lista ordenada.
        return mezclar(izquierda, derecha);
    }

    // O(n): recorre las dos mitades una sola vez, tomando siempre el menor de los
    // dos "punteros" (i y j) hasta agotar ambas listas.
    private List<Personaje> mezclar(List<Personaje> izquierda, List<Personaje> derecha) {
        List<Personaje> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0;
        int j = 0;

        while (i < izquierda.size() && j < derecha.size()) {
            if (ComparadorClave.comparar(izquierda.get(i), derecha.get(j)) <= 0) {
                resultado.add(izquierda.get(i));
                i++;
            } else {
                resultado.add(derecha.get(j));
                j++;
            }
        }

        // A esta altura una de las dos listas ya se vació; volcamos lo que queda de la otra.
        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i));
            i++;
        }
        while (j < derecha.size()) {
            resultado.add(derecha.get(j));
            j++;
        }

        return resultado;
    }
}
