package com.rodrigo;

import controller.EOQController;
import model.EOQModel;
import view.EOQView;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada alternativo para ejecutar la aplicacion desde el paquete
 * com.rodrigo, que es el paquete inicial creado por IntelliJ.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EOQView vista = new EOQView();
            EOQModel modelo = new EOQModel(0, 0, 0, 0);
            new EOQController(vista, modelo);
            vista.setVisible(true);
        });
    }
}
