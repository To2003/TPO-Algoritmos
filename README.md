Adivina Quién - TP de Diseño y Análisis de Algoritmos

Che, esto es un "Adivina Quién" hecho en Java puro (Swing para la interfaz, nada de librerías externas) para el TP de la facu. La idea de este README es que te sirva para entender el código de punta a punta y poder defenderlo sin que te agarren en curva.

## 1. Qué es y cómo se corre

Es el juego de mesa de toda la vida: elegís un personaje secreto, y la máquina (o vos) van hacer preguntas de sí/no para tratar de adivinar quién es, descartando candidatos hasta quedarse con uno solo.

No usa Maven ni Gradle, es un proyecto plano: se compila directo con `javac`.

### Compilar

Parado en la raíz del proyecto (`adivina-quien/`):

```
javac -d bin -encoding UTF-8 $(find src -name "*.java")
```

Esto tira todos los `.class` compilados adentro de `bin/` (esa carpeta está en el `.gitignore`, no se sube).

### Correr el juego (GUI)

```
java -cp bin main.Main
```

Te abre la ventana con el menú para elegir el modo de juego.

### Correr solo el benchmark (por consola, sin GUI)

```
java -cp bin main.Main --benchmark
```

O también lo podés disparar desde el botón que está en el menú principal de la GUI ("Correr benchmark..."), que lo corre en un hilo aparte y te tira los resultados por consola.

## 2. Mapa de carpetas

```
src/
├── defaults/          Constantes, configuración y el armado "crudo" del mazo base.
├── main/              Main.java, el punto de entrada.
├── gui/               Todo lo visual: JFrame, JPanel, los dibujos con Graphics2D.
├── funcionalidades/    La lógica de verdad: motor del juego, IA, ordenamiento, logs.
├── datos/             Los objetos del dominio: Personaje, Mazo, Pregunta, enums.
└── interfaces/        Los contratos (IOrdenador, IEstrategiaIA, IObservadorJuego).
```

Por qué está separado así:

- **`datos/`** son las "cosas" del juego, sin comportamiento complejo. `Personaje` es prácticamente un DTO. `Mazo` sí tiene algo de lógica (armar, ordenar, filtrar) pero porque es el dueño natural de la lista de personajes.
- **`defaults/`** es todo lo que es configuración/constantes/armado inicial. La idea es que si mañana querés cambiar cuántos ms dura el delay del razonamiento, o qué combinación se descarta del mazo, vas a un solo lugar.
- **`funcionalidades/`** es donde está el "cerebro" del TP: los algoritmos de ordenamiento, las dos estrategias de IA, el motor que coordina todo, y el logger de razonamiento. **Esta carpeta no importa nada de `javax.swing`** a propósito: la lógica del juego tiene que poder correr sola, sin ventana, y de hecho así la probamos (ver la sección de preguntas de defensa).
- **`interfaces/`** son los contratos que le permiten a `funcionalidades` no depender de Swing: la GUI se entera de los cambios implementando `IObservadorJuego`, no al revés.
- **`gui/`** es pura presentación. Ningún panel decide reglas del juego, solo le pide cosas al `MotorJuego` y pinta lo que este le devuelve.

## 3. Recorrido de una partida, clase por clase

Ejemplo con el modo "Jugador vs Máquina 1":

1. `Main.main()` arranca `VentanaPrincipal` en el Event Dispatch Thread (Swing es así, todo lo visual tiene que crearse ahí).
2. `VentanaPrincipal` crea un `MotorJuego` una sola vez. Adentro del constructor de `MotorJuego` se crea un `Mazo`, que en su propio constructor llama a `ArmadorDeMazo.generarMazoBase()` (arma las 23 combinaciones únicas) y se valida a sí mismo.
3. El usuario aprieta "Jugador vs Máquina 1" → `VentanaPrincipal.iniciarSeleccionDePersonaje()` muestra un `PanelTablero` clickeable con las 23 cartas (`CartaPersonaje`, que dibuja la carita con `Graphics2D`).
4. El usuario clickea una carta → se guarda como `personajeElegido` y se habilita "Confirmar y arrancar".
5. Al confirmar → `motor.iniciarPartida(modo, personajeElegido)`. Ahí el `MotorJuego`:
   - Envuelve el personaje del jugador en un `PersonajeSecreto` (la única forma de consultarlo después es con `responder(Pregunta)`).
   - Elige un personaje al azar para la máquina, también envuelto en `PersonajeSecreto`.
   - Arma las listas de candidatos (empiezan siendo los 23 personajes para los dos lados).
   - Elige la estrategia: `EstrategiaGreedyMinimax` si es Máquina 1, `EstrategiaEntropia` si es Máquina 2.
6. `VentanaPrincipal` pinta el tablero de juego (dos `PanelTablero`, un `PanelEstado`, un `PanelPreguntas`) y llama a `actualizarVistaJuegoConJugador()`.
7. Es el turno del jugador: elige una pregunta del combo (que sale de `motor.getPreguntasDisponiblesParaJugador()`, ya sin las que repitió) y aprieta "Preguntar" → `PanelPreguntas` llama a `motor.jugadorPregunta(pregunta)`.
8. `MotorJuego.jugadorPregunta()` le pregunta al `PersonajeSecreto` de la máquina (`secretoMaquina.responder(pregunta)`), filtra los candidatos con `Mazo.filtrar()`, y notifica a los observadores (`IObservadorJuego.onEstadoActualizado()`).
9. `VentanaPrincipal.onEstadoActualizado()` ve que ahora es el turno de la máquina y dispara un hilo aparte que llama a `motor.ejecutarTurnoMaquina()` (aparte para no congelar la GUI, porque el `Razonador` tiene sleeps entre líneas).
10. `ejecutarTurnoMaquina()` le pregunta a la `estrategiaActual` (`IEstrategiaIA.debeArriesgar()` y `elegirPregunta()`), imprime todo el razonamiento por consola vía `Razonador`, le pregunta al `PersonajeSecreto` del jugador, filtra candidatos, y vuelve a notificar.
11. Esto se repite, turno por turno, hasta que alguien arriesga bien o mal. Si el jugador gana, `MotorJuego.resolverFinDeRonda()` detecta que todavía no pasó por la otra máquina y llama a `transicionarASegundaMaquina()`: cambia de estrategia, mantiene los candidatos que ya tenía la máquina sobre el jugador (la ventaja heredada) y resetea todo lo relacionado a adivinar a la máquina nueva.
12. Cuando la partida termina de verdad, `MotorJuego` llama a `IObservadorJuego.onFinDePartida()`, la ventana muestra un cartel con el resultado y vuelve al menú.

Para "Máquina 1 vs Máquina 2" el flujo es más directo: `VentanaPrincipal.iniciarDueloDeMaquinas()` dispara un hilo que corre `motor.iniciarDueloDeMaquinas()` de punta a punta (las dos máquinas se turnan solas, sin esperar a nadie), notificando después de cada turno para que la GUI se vaya actualizando en vivo.

## 4. Los algoritmos, explicados en criollo

### MergeSort (ordenamiento del mazo) — O(n log n)

Divide y conquista de manual: parte la lista al medio, ordena cada mitad por separado (ahí está la recursión) y después las mezcla (`mezclar()`) recorriendo las dos mitades una sola vez, siempre agarrando el menor de los dos "punteros".

¿Por qué es O(n log n)? Porque el árbol de la recursión tiene log₂(n) niveles (cada vez partimos a la mitad), y en cada nivel el trabajo total de mezclar es O(n) (se recorre cada elemento una sola vez por nivel). n niveles... perdón, log(n) niveles × O(n) por nivel = O(n log n).

El criterio de orden es compuesto: primero género, después color de pelo, después calvicie, y por último lentes (`ComparadorClave.comparar()`). Una vez ordenado, se renumeran los `id` del 1 al 23 según la posición final: así la máquina "arma y numera" el mazo, tal como pide el enunciado.

### InsertionSort (solo para el benchmark) — O(n²) peor caso

Para cada elemento, lo va "insertando" en su lugar corriendo hacia atrás a los que son mayores. Si la lista viene muy desordenada (peor caso), cada elemento puede tener que recorrer toda la parte ya ordenada, de ahí el O(n²). Con n chico (como nuestros 23 personajes) esto no se nota tanto en la práctica — de hecho mirá los resultados del benchmark más abajo.

### Estrategia Greedy Minimax (Máquina 1)

Para cada una de las 9 preguntas posibles, la separa en dos grupos (los que cumplen y los que no) y calcula el "peor caso": `max(cumplen, no cumplen)`. Se queda con la pregunta que **minimiza ese peor caso**, o sea la partición más parecida a un 50/50. La lógica es: no importa qué te respondan, siempre querés haber descartado la mayor cantidad posible de candidatos.

Es O(p × n): p son las preguntas posibles (siempre 9, fijo) y n los candidatos actuales — para cada pregunta hay que recorrer los candidatos una vez para partirlos.

Arriesga recién cuando le queda **1 solo candidato** (o sea, cuando ya está 100% segura).

### Estrategia por Entropía (Máquina 2)

En vez de mirar el peor caso, calcula cuánta información (en bits) le da cada pregunta, usando la fórmula de Shannon: `H(S) = -Σ p·log2(p)`. Como al principio todos los candidatos son igual de probables, `H(S)` se simplifica a `log2(n)`. Para cada pregunta calcula la "ganancia": cuánto baja la entropía promedio después de la respuesta (`H(S) - [pSí·H(Sí) + pNo·H(No)]`), y se queda con la que más ganancia le da.

En la práctica, para nuestro mazo (candidatos con probabilidad uniforme), maximizar la ganancia de información y minimizar el peor caso terminan casi siempre eligiendo la misma pregunta — la diferencia real entre las dos máquinas está en la heurística de riesgo: la Máquina 2 arriesga apenas le quedan **2 candidatos** (en vez de esperar a tener 1 como la Máquina 1), porque calcula que ya tiene un 50% de pegarla y no vale la pena gastar un turno más.

También es O(p × n) por la misma razón que la Greedy.

### Resultados reales del benchmark

Corrida con n=23, warm-up de 10.000 iteraciones descartadas y 1.000 repeticiones medidas con `System.nanoTime()`:

```
Algoritmo         Promedio (µs)   Promedio (ms)
MergeSort                 3.905        0.003905
InsertionSort             0.271        0.000271
```

Como se ve, con n tan chico **InsertionSort le gana a MergeSort en tiempo real**, a pesar de tener peor complejidad teórica (O(n²) contra O(n log n)). Esto pasa porque MergeSort tiene overhead de recursión y de crear sublistas nuevas en cada partición, que a esta escala pesa más que el ahorro algorítmico. Si n fuera mucho más grande (miles de elementos), la cosa se daría vuelta y MergeSort iba a ganar claramente. Es un buen punto para la defensa: la notación Big O habla de cómo escala el algoritmo, no de qué tan rápido es en un caso puntual y chico.

## 5. Preguntas que te pueden hacer en la defensa

1. **¿Por qué el mazo tiene 23 personajes y no 24?**
   Porque el espacio de atributos (género × calvicie × lentes × color de pelo) da exactamente 2×2×2×3 = 24 combinaciones posibles, pero el enunciado pide 23. Generamos las 24 y descartamos una sola (fijada en `Constantes.java`), así los 23 que quedan son todos únicos.

2. **¿Qué significa el color de pelo en un personaje calvo?**
   Se interpreta como el color de cejas/barba/pelo residual, no de una cabellera. Lo dejamos así para que las 24 combinaciones sigan siendo válidas y cada personaje sea distinguible por sus 4 atributos.

3. **¿Por qué eligieron MergeSort y no `Collections.sort()`?**
   Porque el TP pide implementar un algoritmo de ordenamiento por divide y conquista a mano, para demostrar que entendemos cómo funciona por dentro, no para usar el que ya trae el lenguaje.

4. **¿Por qué el benchmark necesita warm-up?**
   Porque la JVM al principio interpreta el bytecode, y recién después de varias corridas el JIT lo compila a código nativo. Si midiéramos desde la primera ejecución, estaríamos midiendo ese "arranque en frío" mezclado con el algoritmo real.

5. **¿Por qué promedian 1.000 corridas en vez de medir una sola vez?**
   Porque con n=23 cada ordenamiento tarda microsegundos, y a esa escala el ruido del sistema operativo (context switches, garbage collector, etc.) pesa más que el algoritmo en sí. Promediando muchas corridas ese ruido se cancela.

6. **¿En qué se diferencian las dos estrategias de IA?**
   La Máquina 1 (Greedy Minimax) elige la pregunta que minimiza el peor caso posible. La Máquina 2 (Entropía) elige la que maximiza la ganancia de información de Shannon. Con candidatos igual de probables casi siempre eligen la misma pregunta, pero se diferencian en cuándo arriesgan: la Máquina 1 espera a tener 1 solo candidato, la Máquina 2 arriesga ya con 2.

7. **¿Cómo se garantiza que la máquina nunca "hace trampa" mirando el personaje del jugador?**
   El personaje del jugador se envuelve en un `PersonajeSecreto` (`funcionalidades/PersonajeSecreto.java`), que guarda el `Personaje` como campo `private final` sin ningún getter que lo devuelva. La única forma de sacarle información es con `responder(Pregunta)`, que contesta sí/no sobre UN atributo. Ni las estrategias de IA ni la GUI reciben nunca el objeto `Personaje` secreto en crudo.

8. **¿Qué pasa si el jugador gana contra la Máquina 1?**
   Arranca automáticamente una ronda contra la Máquina 2 (`MotorJuego.transicionarASegundaMaquina()`). El personaje del jugador es el mismo de siempre, pero la Máquina 2 hereda los candidatos que ya había descartado la Máquina 1 sobre el jugador — arranca con ventaja. Lo que sí se resetea es la información del jugador sobre la máquina, porque la Máquina 2 juega con un personaje propio nuevo.

9. **¿Qué pasa si se repite una pregunta?**
   El motor guarda un historial de preguntas hechas por el jugador y no deja repetirlas (`MotorJuego.jugadorPregunta()` tira una excepción si ya está en el historial); además la GUI ni siquiera se la ofrece en el combo (`getPreguntasDisponiblesParaJugador()` ya viene filtrado). Para las máquinas no hace falta filtrar explícitamente: como preguntar de nuevo algo ya sabido no separa nada (0 de un lado), la propia estrategia la descarta sola.

10. **¿Por qué la GUI no depende de la lógica del juego, y viceversa?**
    Para que se pueda probar y usar la lógica del juego sin ventana (como hicimos por consola durante el desarrollo) y para que un cambio visual no obligue a tocar las reglas del juego. Se logra con el patrón Observer: `funcionalidades/` nunca importa `javax.swing`, y la GUI se entera de los cambios implementando `IObservadorJuego`.

11. **¿Puede quedar el juego sin solución (dos personajes que nunca se puedan distinguir)?**
    No, porque `Mazo` valida al armarse que los 23 personajes sean todos combinaciones distintas de sus 4 atributos (si no, tira una excepción). Como siempre son distintos, siempre existe alguna pregunta que los separa.

12. **¿Por qué el tablero le muestra al jugador lo que la máquina va descartando de él, si en el juego real esa info sería "secreta"?**
    Es una decisión de diseño pensada para la demo/defensa: la idea del TP es que se vea el razonamiento de la IA en vivo (por eso también existe el `Razonador` imprimiendo por consola), no simular un juego competitivo estricto donde hay que ocultarle información al jugador.
