package datos;

// Enumera los 4 "ejes" de atributos que tiene un personaje. Sirve para que
// la IA pueda iterar sobre todos los filtros posibles sin tener que hardcodear
// "preguntame por genero, despues por calvicie, etc" a mano en cada estrategia.
public enum TipoAtributo {
    GENERO,
    CALVICIE,
    LENTES,
    COLOR_PELO
}
