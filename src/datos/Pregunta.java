package datos;

// Una Pregunta es un filtro concreto: "el atributo X, ¿vale Y?".
// Por ejemplo TipoAtributo.COLOR_PELO con valor ColorPelo.NEGRO representa
// la pregunta "¿tu personaje tiene el pelo negro?".
// Guardamos el valor como Object para no tener que hacer una subclase por
// cada tipo de atributo (serian 4 clases casi idénticas, no vale la pena).
public class Pregunta {

    private final TipoAtributo tipo;
    private final Object valor;

    public Pregunta(TipoAtributo tipo, Object valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public TipoAtributo getTipo() {
        return tipo;
    }

    public Object getValor() {
        return valor;
    }

    // O(1): compara un solo atributo del personaje contra el valor de la pregunta.
    public boolean cumple(Personaje p) {
        switch (tipo) {
            case GENERO:
                return p.getGenero() == valor;
            case CALVICIE:
                return p.isCalvo() == (Boolean) valor;
            case LENTES:
                return p.usaLentes() == (Boolean) valor;
            case COLOR_PELO:
                return p.getColorPelo() == valor;
            default:
                // No deberia pasar nunca: los 4 casos de arriba cubren todo TipoAtributo.
                throw new IllegalStateException("Tipo de atributo desconocido: " + tipo);
        }
    }

    // Dos preguntas son "la misma" si consultan el mismo atributo con el mismo valor.
    // Lo usamos para no dejar repetir una pregunta ya hecha en la misma partida.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pregunta)) return false;
        Pregunta otra = (Pregunta) o;
        return tipo == otra.tipo && valor.equals(otra.valor);
    }

    @Override
    public int hashCode() {
        return tipo.hashCode() * 31 + valor.hashCode();
    }

    @Override
    public String toString() {
        switch (tipo) {
            case GENERO:
                return valor == Genero.FEMENINO ? "¿Tu personaje es mujer?" : "¿Tu personaje es varón?";
            case CALVICIE:
                return (Boolean) valor ? "¿Tu personaje es calvo?" : "¿Tu personaje tiene pelo?";
            case LENTES:
                return (Boolean) valor ? "¿Tu personaje usa lentes?" : "¿Tu personaje NO usa lentes?";
            case COLOR_PELO:
                return "¿Tu personaje tiene el pelo " + valor + "?";
            default:
                return "¿Pregunta desconocida?";
        }
    }
}
