package gui;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import datos.Pregunta;

// Muestra el turno actual, cuántos candidatos le quedan a cada lado y el
// historial de preguntas hechas. Es puramente informativo, no toma decisiones.
public class PanelEstado extends JPanel {

    private final JLabel labelTurno = new JLabel();
    private final JLabel labelCandidatosPropios = new JLabel();
    private final JLabel labelCandidatosRivales = new JLabel();
    private final JTextArea historial = new JTextArea(6, 20);

    public PanelEstado() {
        setLayout(new java.awt.BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Estado de la partida"));

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.add(labelTurno);
        info.add(labelCandidatosPropios);
        info.add(labelCandidatosRivales);
        add(info, java.awt.BorderLayout.NORTH);

        historial.setEditable(false);
        add(new JScrollPane(historial), java.awt.BorderLayout.CENTER);
    }

    public void actualizar(String textoTurno, int candidatosPropios, int candidatosRivales,
                            List<Pregunta> historialPropio, List<Pregunta> historialRival) {
        labelTurno.setText("Turno: " + textoTurno);
        labelCandidatosPropios.setText("Candidatos que le quedan al jugador sobre el rival: " + candidatosPropios);
        labelCandidatosRivales.setText("Candidatos que le quedan a la máquina sobre el jugador: " + candidatosRivales);

        StringBuilder texto = new StringBuilder();
        texto.append("-- Preguntas del jugador --\n");
        for (Pregunta p : historialPropio) {
            texto.append("  ").append(p).append("\n");
        }
        texto.append("-- Preguntas de la máquina --\n");
        for (Pregunta p : historialRival) {
            texto.append("  ").append(p).append("\n");
        }
        historial.setText(texto.toString());
    }

    public void actualizarSoloTexto(String texto) {
        historial.setText(texto);
    }

    public void setTurno(String texto) {
        labelTurno.setText("Turno: " + texto);
    }

    public void setCandidatosPropios(int cantidad) {
        labelCandidatosPropios.setText("Candidatos propios restantes: " + cantidad);
    }

    public void setCandidatosRivales(int cantidad) {
        labelCandidatosRivales.setText("Candidatos rivales restantes: " + cantidad);
    }
}
