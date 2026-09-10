package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import datos.Personaje;

// Muestra la grilla con los 23 personajes. Los que no están en la lista de
// "candidatos vivos" que le pasan se pintan como descartados (grises).
// Se usa tanto para el tablero del jugador como para el de cada máquina.
public class PanelTablero extends JPanel {

    private final CartaPersonaje[] cartas;
    private Consumer<Personaje> alHacerClick;

    public PanelTablero(String titulo, List<Personaje> mazoCompleto) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(titulo));

        JLabel etiqueta = new JLabel(titulo, SwingConstants.CENTER);
        add(etiqueta, BorderLayout.NORTH);

        JPanel grilla = new JPanel(new GridLayout(0, 6, 4, 4));
        cartas = new CartaPersonaje[mazoCompleto.size()];
        for (int i = 0; i < mazoCompleto.size(); i++) {
            CartaPersonaje carta = new CartaPersonaje(mazoCompleto.get(i));
            carta.setAlHacerClick(p -> {
                if (alHacerClick != null) alHacerClick.accept(p);
            });
            cartas[i] = carta;
            grilla.add(carta);
        }
        add(grilla, BorderLayout.CENTER);
    }

    public void setAlHacerClick(Consumer<Personaje> listener) {
        this.alHacerClick = listener;
        for (CartaPersonaje carta : cartas) {
            carta.setAlHacerClick(listener);
        }
    }

    // Repinta el tablero marcando como descartado a todo el que NO esté en
    // "candidatosVivos". O(n) sobre las 23 cartas.
    public void actualizarCandidatos(List<Personaje> candidatosVivos) {
        for (CartaPersonaje carta : cartas) {
            boolean vivo = contiene(candidatosVivos, carta.getPersonaje());
            carta.setDescartada(!vivo);
        }
    }

    public void marcarSeleccionado(Personaje seleccionado) {
        for (CartaPersonaje carta : cartas) {
            carta.setSeleccionada(carta.getPersonaje().getId() == (seleccionado == null ? -1 : seleccionado.getId()));
        }
    }

    private boolean contiene(List<Personaje> lista, Personaje p) {
        for (Personaje otro : lista) {
            if (otro.getId() == p.getId()) return true;
        }
        return false;
    }
}
