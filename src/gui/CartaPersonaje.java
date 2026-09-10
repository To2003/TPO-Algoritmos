package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JPanel;

import datos.ColorPelo;
import datos.Personaje;

// Una "carta" del tablero: dibuja una carita simple con Graphics2D (nada de
// imágenes externas, como pide el enunciado) y el nombre abajo. Puede estar
// "viva" (el personaje todavía es candidato), "descartada" (se pintó gris,
// como si estuviera dada vuelta) o "seleccionada" (el jugador la eligió).
public class CartaPersonaje extends JPanel {

    private final Personaje personaje;
    private boolean descartada = false;
    private boolean seleccionada = false;
    private boolean clickeable = false;

    public CartaPersonaje(Personaje personaje) {
        this.personaje = personaje;
        setPreferredSize(new java.awt.Dimension(90, 110));
        setToolTipText(personaje.toString());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickeable && alHacerClickListener != null) {
                    alHacerClickListener.accept(personaje);
                }
            }
        });
    }

    private Consumer<Personaje> alHacerClickListener;

    public void setAlHacerClick(Consumer<Personaje> listener) {
        this.alHacerClickListener = listener;
        this.clickeable = listener != null;
    }

    public void setDescartada(boolean descartada) {
        this.descartada = descartada;
        repaint();
    }

    public void setSeleccionada(boolean seleccionada) {
        this.seleccionada = seleccionada;
        repaint();
    }

    public Personaje getPersonaje() {
        return personaje;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        // Fondo de la carta.
        g2.setColor(descartada ? new Color(60, 60, 60) : Color.WHITE);
        g2.fillRoundRect(2, 2, ancho - 4, alto - 4, 10, 10);

        g2.setColor(seleccionada ? new Color(40, 140, 220) : Color.GRAY);
        g2.setStroke(new java.awt.BasicStroke(seleccionada ? 3f : 1f));
        g2.drawRoundRect(2, 2, ancho - 4, alto - 4, 10, 10);

        if (descartada) {
            // Personaje descartado: se muestra "dado vuelta" (gris, sin cara).
            g2.setColor(Color.LIGHT_GRAY);
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            dibujarTextoCentrado(g2, "✕", ancho / 2, alto / 2 - 10);
        } else {
            dibujarCarita(g2, ancho / 2, 38);
        }

        g2.setColor(descartada ? Color.LIGHT_GRAY : Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dibujarTextoCentrado(g2, personaje.getNombre(), ancho / 2, alto - 12);
    }

    // Dibuja una carita bien simple: círculo de piel, "pelo" (o cejas/barba si es calvo)
    // del color que corresponda, y lentes si el personaje usa.
    private void dibujarCarita(Graphics2D g2, int centroX, int centroY) {
        int radio = 24;

        // Cabeza.
        g2.setColor(new Color(255, 224, 189));
        g2.fillOval(centroX - radio, centroY - radio, radio * 2, radio * 2);
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval(centroX - radio, centroY - radio, radio * 2, radio * 2);

        Color colorPelo = colorAwt(personaje.getColorPelo());

        if (personaje.isCalvo()) {
            // Calvo: no dibujamos pelo arriba. El colorPelo se usa para las cejas,
            // como aclara el comentario en Personaje.java.
            g2.setColor(colorPelo);
            g2.fillRect(centroX - 12, centroY - 8, 8, 3);
            g2.fillRect(centroX + 4, centroY - 8, 8, 3);
        } else {
            // Con pelo: un arco arriba de la cabeza.
            g2.setColor(colorPelo);
            g2.fillArc(centroX - radio, centroY - radio - 4, radio * 2, radio * 2, 0, 180);
        }

        // Ojos.
        g2.setColor(Color.BLACK);
        g2.fillOval(centroX - 10, centroY - 2, 4, 4);
        g2.fillOval(centroX + 6, centroY - 2, 4, 4);

        // Sonrisa.
        g2.drawArc(centroX - 10, centroY + 2, 20, 12, 200, 140);

        if (personaje.usaLentes()) {
            g2.setColor(Color.BLACK);
            g2.drawOval(centroX - 14, centroY - 4, 12, 10);
            g2.drawOval(centroX + 2, centroY - 4, 12, 10);
            g2.drawLine(centroX - 2, centroY, centroX + 2, centroY);
        }
    }

    private Color colorAwt(ColorPelo colorPelo) {
        switch (colorPelo) {
            case COLORADO: return new Color(200, 60, 40);
            case NEGRO: return new Color(35, 35, 35);
            case AMARILLO: return new Color(230, 190, 40);
            default: return Color.GRAY;
        }
    }

    private void dibujarTextoCentrado(Graphics2D g2, String texto, int centroX, int y) {
        int anchoTexto = g2.getFontMetrics().stringWidth(texto);
        g2.drawString(texto, centroX - anchoTexto / 2, y);
    }
}
