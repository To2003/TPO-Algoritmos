package main;

import javax.swing.SwingUtilities;

import funcionalidades.Benchmark;
import gui.VentanaPrincipal;

// Punto de entrada del programa. Si se lo corre con el flag "--benchmark"
// corre el benchmark de MergeSort vs InsertionSort por consola y no abre
// la GUI; si no, abre la ventana principal del juego.
public class Main {

    public static void main(String[] args) {
        boolean soloBenchmark = args.length > 0 && args[0].equals("--benchmark");

        if (soloBenchmark) {
            Benchmark.ejecutar();
            return;
        }

        // Swing no es thread-safe: toda la GUI se tiene que crear y modificar
        // desde el Event Dispatch Thread (EDT), por eso el invokeLater.
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
