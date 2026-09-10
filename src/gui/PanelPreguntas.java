package gui;

import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import datos.Personaje;
import datos.Pregunta;

// Los controles que usa el jugador humano en su turno: un combo con las
// preguntas todavía disponibles (no repetidas), otro combo con los
// candidatos posibles para poder arriesgar, y los botones correspondientes.
public class PanelPreguntas extends JPanel {

    private final JComboBox<Pregunta> comboPreguntas = new JComboBox<>();
    private final JComboBox<Personaje> comboArriesgar = new JComboBox<>();
    private final JButton botonPreguntar = new JButton("Preguntar");
    private final JButton botonArriesgar = new JButton("¡Arriesgar!");

    public PanelPreguntas() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Tu turno"));

        add(comboPreguntas);
        add(botonPreguntar);
        add(new javax.swing.JLabel("   |   Arriesgar a:"));
        add(comboArriesgar);
        add(botonArriesgar);
    }

    public void setPreguntasDisponibles(List<Pregunta> preguntas) {
        comboPreguntas.removeAllItems();
        for (Pregunta p : preguntas) {
            comboPreguntas.addItem(p);
        }
        botonPreguntar.setEnabled(!preguntas.isEmpty());
    }

    public void setCandidatosParaArriesgar(List<Personaje> candidatos) {
        comboArriesgar.removeAllItems();
        for (Personaje p : candidatos) {
            comboArriesgar.addItem(p);
        }
    }

    public void setAlPreguntar(Consumer<Pregunta> callback) {
        botonPreguntar.addActionListener(e -> {
            Pregunta seleccionada = (Pregunta) comboPreguntas.getSelectedItem();
            if (seleccionada != null) callback.accept(seleccionada);
        });
    }

    public void setAlArriesgar(Consumer<Personaje> callback) {
        botonArriesgar.addActionListener(e -> {
            Personaje seleccionado = (Personaje) comboArriesgar.getSelectedItem();
            if (seleccionado != null) callback.accept(seleccionado);
        });
    }

    public void setHabilitado(boolean habilitado) {
        comboPreguntas.setEnabled(habilitado);
        botonPreguntar.setEnabled(habilitado && comboPreguntas.getItemCount() > 0);
        comboArriesgar.setEnabled(habilitado);
        botonArriesgar.setEnabled(habilitado);
    }
}
