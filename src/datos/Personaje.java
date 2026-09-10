package datos;

// Representa una de las 23 cartas del mazo. Es un objeto bien simple, tipo
// "record casero" (no usamos record de Java porque queremos setId mutable
// para la renumeracion que hace la maquina despues de ordenar).
public class Personaje {

    private int id;
    private final String nombre;
    private final Genero genero;
    private final boolean calvo;
    private final boolean lentes;
    private final ColorPelo colorPelo;

    public Personaje(int id, String nombre, Genero genero, boolean calvo, boolean lentes, ColorPelo colorPelo) {
        this.id = id;
        this.nombre = nombre;
        this.genero = genero;
        this.calvo = calvo;
        this.lentes = lentes;
        this.colorPelo = colorPelo;
    }

    public int getId() {
        return id;
    }

    // La maquina usa esto para renumerar las cartas una vez que ordeno el mazo con MergeSort.
    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public Genero getGenero() {
        return genero;
    }

    public boolean isCalvo() {
        return calvo;
    }

    public boolean usaLentes() {
        return lentes;
    }

    // Si el personaje es calvo, este color corresponde a cejas/barba/pelo residual,
    // no a una cabellera. Lo mantenemos como atributo igual para que las 24 combinaciones
    // de genero x calvicie x lentes x colorPelo sigan siendo validas y distinguibles.
    public ColorPelo getColorPelo() {
        return colorPelo;
    }

    @Override
    public String toString() {
        return nombre + " [#" + id + "] (" + genero + ", "
                + (calvo ? "calvo" : "con pelo") + ", "
                + (lentes ? "con lentes" : "sin lentes") + ", pelo " + colorPelo + ")";
    }
}
