package funcionalidades;

import datos.Personaje;

// Clave de ordenamiento compartida por MergeSort e InsertionSort: primero género,
// después color de pelo, después calvicie, y por último lentes. La compartimos
// entre los dos algoritmos para que el benchmark sea justo (mismo criterio,
// solo cambia el algoritmo).
public class ComparadorClave {

    // O(1): compara dos personajes atributo por atributo hasta encontrar una diferencia.
    public static int comparar(Personaje a, Personaje b) {
        int porGenero = a.getGenero().compareTo(b.getGenero());
        if (porGenero != 0) return porGenero;

        int porColorPelo = a.getColorPelo().compareTo(b.getColorPelo());
        if (porColorPelo != 0) return porColorPelo;

        int porCalvicie = Boolean.compare(a.isCalvo(), b.isCalvo());
        if (porCalvicie != 0) return porCalvicie;

        return Boolean.compare(a.usaLentes(), b.usaLentes());
    }
}
