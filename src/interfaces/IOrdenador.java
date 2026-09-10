package interfaces;

import java.util.List;

import datos.Personaje;

// Contrato para los algoritmos de ordenamiento del mazo. Lo implementan
// MergeSort e InsertionSort, así el Benchmark los puede comparar sin
// importarle cuál es cuál.
public interface IOrdenador {
    List<Personaje> ordenar(List<Personaje> lista);
}
