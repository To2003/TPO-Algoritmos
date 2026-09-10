package defaults;

import java.util.ArrayList;
import java.util.List;

import datos.ColorPelo;
import datos.Genero;
import datos.Personaje;

// Se encarga de generar el mazo "crudo" de 23 personajes recorriendo las
// 24 combinaciones posibles de (genero, calvicie, lentes, colorPelo) y
// descartando la que está marcada en Constantes.
public class ArmadorDeMazo {

    // O(1): son siempre 24 combinaciones fijas (2*2*2*3), no depende de ningún N externo.
    public static List<Personaje> generarMazoBase() {
        List<Personaje> personajes = new ArrayList<>();
        // Contamos por separado cuántos masculinos y cuántos femeninos ya generamos,
        // para irle asignando el nombre que corresponda de cada lista.
        int indiceNombreMasculino = 0;
        int indiceNombreFemenino = 0;
        int idProvisorio = 1;

        // Recorremos las 4 dimensiones del espacio de atributos con 4 for anidados.
        // Usamos Genero.values() y ColorPelo.values() en vez de hardcodear para que si
        // el día de mañana se agrega un color de pelo, esto siga generando bien todo.
        for (Genero genero : Genero.values()) {
            for (boolean calvo : new boolean[] { false, true }) {
                for (boolean lentes : new boolean[] { false, true }) {
                    for (ColorPelo colorPelo : ColorPelo.values()) {

                        boolean esLaCombinacionDescartada =
                                genero == Constantes.COMBINACION_DESCARTADA_GENERO
                                        && calvo == Constantes.COMBINACION_DESCARTADA_CALVO
                                        && lentes == Constantes.COMBINACION_DESCARTADA_LENTES
                                        && colorPelo == Constantes.COMBINACION_DESCARTADA_COLOR_PELO;

                        if (esLaCombinacionDescartada) {
                            continue;
                        }

                        String nombre;
                        if (genero == Genero.MASCULINO) {
                            nombre = NombresPersonajes.NOMBRES_MASCULINOS[indiceNombreMasculino];
                            indiceNombreMasculino++;
                        } else {
                            nombre = NombresPersonajes.NOMBRES_FEMENINOS[indiceNombreFemenino];
                            indiceNombreFemenino++;
                        }

                        personajes.add(new Personaje(idProvisorio, nombre, genero, calvo, lentes, colorPelo));
                        idProvisorio++;
                    }
                }
            }
        }

        return personajes;
    }
}
