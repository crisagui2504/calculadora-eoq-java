package view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/**
 * Ventana especializada para explicar visualmente un resultado EOQ.
 * Se reutiliza para Q*, numero de pedidos, tiempo entre pedidos y costo total.
 */
public class EOQResultWindow extends JFrame {

    public enum ChartType {
        Q_OPTIMO,
        PEDIDOS,
        TIEMPO,
        COSTO
    }

    private static final Color FONDO = new Color(229, 235, 243);
    private static final Color SUPERFICIE = new Color(252, 253, 255);
    private static final Color TEXTO = new Color(20, 31, 44);
    private static final Color TEXTO_SUAVE = new Color(82, 99, 117);
    private static final Color LINEA = new Color(184, 198, 214);

    public EOQResultWindow(String titulo,
                           String subtitulo,
                           String valorPrincipal,
                           String unidad,
                           String formula,
                           String interpretacion,
                           String datoA,
                           String datoB,
                           String datoC,
                           Color acento,
                           ChartType chartType,
                           double valor,
                           double referenciaA,
                           double referenciaB) {
        configurarVentana(titulo);
        construirInterfaz(
                titulo,
                subtitulo,
                valorPrincipal,
                unidad,
                formula,
                interpretacion,
                datoA,
                datoB,
                datoC,
                acento,
                chartType,
                valor,
                referenciaA,
                referenciaB
        );
    }

    private void configurarVentana(String titulo) {
        setTitle(titulo);
        setSize(1020, 680);
        setMinimumSize(new Dimension(940, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void construirInterfaz(String titulo,
                                   String subtitulo,
                                   String valorPrincipal,
                                   String unidad,
                                   String formula,
                                   String interpretacion,
                                   String datoA,
                                   String datoB,
                                   String datoC,
                                   Color acento,
                                   ChartType chartType,
                                   double valor,
                                   double referenciaA,
                                   double referenciaB) {
        JPanel raiz = new JPanel(new BorderLayout(18, 18));
        raiz.setBackground(FONDO);
        raiz.setBorder(new EmptyBorder(26, 28, 26, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXTO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setForeground(TEXTO_SUAVE);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblSubtitulo);

        JLabel valorGrande = new JLabel(" " + valorPrincipal + " ");
        valorGrande.setOpaque(true);
        valorGrande.setBackground(acento);
        valorGrande.setForeground(Color.WHITE);
        valorGrande.setBorder(new EmptyBorder(16, 26, 16, 26));
        valorGrande.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valorGrande.setPreferredSize(null);
        valorGrande.setMinimumSize(new Dimension(
                valorGrande.getFontMetrics(valorGrande.getFont()).stringWidth(" " + valorPrincipal + " ") + 52,
                64
        ));

        header.add(textos, BorderLayout.WEST);
        header.add(valorGrande, BorderLayout.EAST);

        JPanel cuerpo = new JPanel(new BorderLayout(18, 18));
        cuerpo.setOpaque(false);
        cuerpo.add(crearPanelGrafica(chartType, acento, valor, referenciaA, referenciaB, unidad, datoA, datoB, datoC), BorderLayout.CENTER);
        cuerpo.add(crearPanelLectura(formula, interpretacion, datoA, datoB, datoC), BorderLayout.EAST);

        raiz.add(header, BorderLayout.NORTH);
        raiz.add(cuerpo, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearPanelGrafica(ChartType chartType,
                                     Color acento,
                                     double valor,
                                     double referenciaA,
                                     double referenciaB,
                                     String unidad,
                                     String datoA,
                                     String datoB,
                                     String datoC) {
        JPanel panel = crearPanelBlanco();
        panel.setLayout(new BorderLayout(0, 14));

        JLabel titulo = new JLabel("Visualizacion del indicador");
        titulo.setForeground(TEXTO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 17));

        ChartPanel grafica = new ChartPanel(chartType, acento, valor, referenciaA, referenciaB, unidad);
        grafica.setPreferredSize(new Dimension(520, 300));

        JPanel miniTarjetas = new JPanel(new GridLayout(1, 3, 12, 0));
        miniTarjetas.setOpaque(false);
        miniTarjetas.add(crearMiniDato(datoA, acento, 0));
        miniTarjetas.add(crearMiniDato(datoB, acento, 1));
        miniTarjetas.add(crearMiniDato(datoC, acento, 2));

        JPanel wrapMini = new JPanel(new BorderLayout());
        wrapMini.setOpaque(false);
        wrapMini.setPreferredSize(new Dimension(100, 124));
        wrapMini.add(miniTarjetas, BorderLayout.CENTER);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(grafica, BorderLayout.CENTER);
        panel.add(wrapMini, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelLectura(String formula,
                                     String interpretacion,
                                     String datoA,
                                     String datoB,
                                     String datoC) {
        JPanel panel = crearPanelBlanco();
        panel.setPreferredSize(new Dimension(350, 420));
        panel.setLayout(new BorderLayout(0, 14));

        JPanel lectura = new JPanel();
        lectura.setOpaque(false);
        lectura.setLayout(new BoxLayout(lectura, BoxLayout.Y_AXIS));

        lectura.add(crearFormulaVisual(formula));
        lectura.add(Box.createVerticalStrut(16));
        lectura.add(crearBloque("Lectura ejecutiva", interpretacion));
        lectura.add(Box.createVerticalStrut(16));

        JPanel datos = new JPanel(new GridLayout(3, 1, 0, 10));
        datos.setOpaque(false);
        datos.add(crearDato(datoA));
        datos.add(crearDato(datoB));
        datos.add(crearDato(datoC));
        lectura.add(datos);

        panel.add(lectura, BorderLayout.NORTH);
        return panel;
    }

    private JPanel crearFormulaVisual(String formula) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(new Color(241, 246, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINEA),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblTitulo = new JLabel("Formula");
        lblTitulo.setForeground(TEXTO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblFormula = new JLabel(formulaBonita(formula), JLabel.CENTER);
        lblFormula.setForeground(new Color(37, 76, 112));
        lblFormula.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFormula.setBorder(new EmptyBorder(6, 4, 4, 4));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblFormula, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearBloque(String titulo, String texto) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXTO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextArea lblTexto = crearTextoMultilinea(texto, TEXTO_SUAVE, Font.PLAIN, 13);

        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblTexto);
        return panel;
    }

    private JTextArea crearDato(String texto) {
        JTextArea label = crearTextoMultilinea(texto, TEXTO, Font.BOLD, 12);
        label.setBackground(new Color(241, 246, 251));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINEA),
                new EmptyBorder(10, 10, 10, 10)
        ));
        return label;
    }

    private JPanel crearMiniDato(String texto, Color acento, int indice) {
        String[] partes = texto.split(":", 2);
        String titulo = partes.length > 1 ? partes[0].trim().toUpperCase() : "NOTA";
        String valor = partes.length > 1 ? partes[1].trim() : texto;

        Color fondo = mezclar(Color.WHITE, acento, indice == 0 ? 0.12 : indice == 1 ? 0.16 : 0.20);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(fondo);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mezclar(new Color(160, 174, 190), acento, 0.45)),
                new EmptyBorder(12, 12, 12, 12)
        ));
        panel.setMinimumSize(new Dimension(80, 112));
        panel.setPreferredSize(new Dimension(100, 112));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(new Color(
                Math.max(35, acento.getRed() - 35),
                Math.max(35, acento.getGreen() - 35),
                Math.max(35, acento.getBlue() - 35)
        ));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea lblValor = crearTextoMultilinea(valor, TEXTO, Font.BOLD, valor.length() > 38 ? 12 : 14);
        lblValor.setRows(3);
        lblValor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblValor);
        return panel;
    }

    private Color mezclar(Color base, Color acento, double proporcion) {
        int r = (int) (base.getRed() * (1 - proporcion) + acento.getRed() * proporcion);
        int g = (int) (base.getGreen() * (1 - proporcion) + acento.getGreen() * proporcion);
        int b = (int) (base.getBlue() * (1 - proporcion) + acento.getBlue() * proporcion);
        return new Color(r, g, b);
    }

    private JTextArea crearTextoMultilinea(String texto, Color color, int estilo, int tamanio) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(color);
        area.setFont(new Font("Segoe UI", estilo, tamanio));
        area.setBorder(null);
        return area;
    }

    private String formulaBonita(String formula) {
        return switch (formula) {
            case "Q* = sqrt((2 x D x Cp) / Cm)" ->
                    "<html><div style='text-align:center'>Q* = &radic;<span style='border-top:2px solid #254c70;'>&nbsp;(2 &times; D &times; Cp) / Cm&nbsp;</span></div></html>";
            case "N = D / Q*" ->
                    "<html><div style='text-align:center'>N = <sup>D</sup>&frasl;<sub>Q*</sub></div></html>";
            case "T = dias laborables / N" ->
                    "<html><div style='text-align:center'>T = <sup>Dias laborables</sup>&frasl;<sub>N</sub></div></html>";
            case "CT = Cp x (D / Q*) + Cm x (Q* / 2)" ->
                    "<html><div style='text-align:center'>CT = Cp &times; <sup>D</sup>&frasl;<sub>Q*</sub> + Cm &times; <sup>Q*</sup>&frasl;<sub>2</sub></div></html>";
            default -> "<html><div style='text-align:center'>" + formula + "</div></html>";
        };
    }

    private JPanel crearPanelBlanco() {
        JPanel panel = new JPanel();
        panel.setBackground(SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINEA),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    private static class ChartPanel extends JPanel {

        private final ChartType chartType;
        private final Color acento;
        private final double valor;
        private final double referenciaA;
        private final double referenciaB;
        private final String unidad;

        ChartPanel(ChartType chartType,
                   Color acento,
                   double valor,
                   double referenciaA,
                   double referenciaB,
                   String unidad) {
            this.chartType = chartType;
            this.acento = acento;
            this.valor = valor;
            this.referenciaA = referenciaA;
            this.referenciaB = referenciaB;
            this.unidad = unidad;
            setBackground(new Color(248, 251, 255));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            switch (chartType) {
                case Q_OPTIMO -> dibujarQOptimo(g2);
                case PEDIDOS -> dibujarPedidos(g2);
                case TIEMPO -> dibujarTiempo(g2);
                case COSTO -> dibujarCosto(g2);
            }

            g2.dispose();
        }

        private void dibujarQOptimo(Graphics2D g2) {
            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2 - 10;
            int radio = Math.min(w, h) / 3;

            g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(231, 237, 244));
            g2.drawArc(cx - radio, cy - radio, radio * 2, radio * 2, 200, 140);

            g2.setColor(acento);
            g2.drawArc(cx - radio, cy - radio, radio * 2, radio * 2, 200, 112);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(32, 44, 57));
            double angle = Math.toRadians(200 + 112);
            int x2 = cx + (int) (Math.cos(angle) * (radio - 8));
            int y2 = cy - (int) (Math.sin(angle) * (radio - 8));
            g2.drawLine(cx, cy, x2, y2);
            g2.fillOval(cx - 6, cy - 6, 12, 12);

            dibujarTextoCentrado(g2, "Cantidad optima por orden", cx, 32, 15, Font.BOLD, new Color(32, 44, 57));
            dibujarTextoCentrado(g2, formato(valor) + " " + unidad, cx, cy + radio + 32, 26, Font.BOLD, acento);
            dibujarTextoCentrado(g2, "Punto recomendado de compra", cx, cy + radio + 58, 12, Font.PLAIN, new Color(95, 111, 128));
        }

        private void dibujarPedidos(Graphics2D g2) {
            int w = getWidth();
            int h = getHeight();
            int left = 55;
            int bottom = h - 55;
            int barArea = h - 130;
            double max = Math.max(valor, Math.max(referenciaA, referenciaB));
            max = max <= 0 ? 1 : max;

            dibujarEjes(g2, left, 45, w - 35, bottom);
            dibujarBarra(g2, left + 45, bottom, 58, (int) (barArea * referenciaA / max), new Color(180, 195, 212), "Min", formato(referenciaA));
            dibujarBarra(g2, left + 145, bottom, 58, (int) (barArea * valor / max), acento, "EOQ", formato(valor));
            dibujarBarra(g2, left + 245, bottom, 58, (int) (barArea * referenciaB / max), new Color(180, 195, 212), "Max", formato(referenciaB));

            dibujarTextoCentrado(g2, "Comparacion de frecuencia anual", w / 2, 25, 16, Font.BOLD, new Color(32, 44, 57));
        }

        private void dibujarTiempo(Graphics2D g2) {
            int w = getWidth();
            int h = getHeight();
            int y = h / 2;
            int x1 = 55;
            int x2 = w - 55;

            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(218, 226, 236));
            g2.drawLine(x1, y, x2, y);

            int pasos = 6;
            for (int i = 0; i <= pasos; i++) {
                int x = x1 + (x2 - x1) * i / pasos;
                g2.setColor(i == 0 ? acento : new Color(180, 195, 212));
                g2.fillOval(x - 9, y - 9, 18, 18);
                dibujarTextoCentrado(g2, "Pedido", x, y - 28, 11, Font.BOLD, new Color(32, 44, 57));
                if (i < pasos) {
                    int medio = x + (x2 - x1) / pasos / 2;
                    dibujarTextoCentrado(g2, formato(valor) + " dias", medio, y + 34, 12, Font.PLAIN, new Color(95, 111, 128));
                }
            }

            dibujarTextoCentrado(g2, "Ritmo recomendado de reposicion", w / 2, 35, 16, Font.BOLD, new Color(32, 44, 57));
            dibujarTextoCentrado(g2, "Cada intervalo representa el tiempo entre una orden y la siguiente.", w / 2, h - 24, 12, Font.PLAIN, new Color(95, 111, 128));
        }

        private void dibujarCosto(Graphics2D g2) {
            int w = getWidth();
            int h = getHeight();
            int left = 55;
            int bottom = h - 58;
            int barArea = h - 135;
            double max = Math.max(valor, Math.max(referenciaA, referenciaB));
            max = max <= 0 ? 1 : max;

            dibujarEjes(g2, left, 45, w - 35, bottom);
            dibujarBarra(g2, left + 40, bottom, 70, (int) (barArea * referenciaA / max), new Color(50, 133, 117), "Pedir", "$" + formato(referenciaA));
            dibujarBarra(g2, left + 155, bottom, 70, (int) (barArea * referenciaB / max), new Color(146, 94, 25), "Mantener", "$" + formato(referenciaB));
            dibujarBarra(g2, left + 270, bottom, 70, (int) (barArea * valor / max), acento, "Total", "$" + formato(valor));

            dibujarTextoCentrado(g2, "Estructura del costo relevante anual", w / 2, 25, 16, Font.BOLD, new Color(32, 44, 57));
        }

        private void dibujarEjes(Graphics2D g2, int left, int top, int right, int bottom) {
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(218, 226, 236));
            g2.drawLine(left, top, left, bottom);
            g2.drawLine(left, bottom, right, bottom);
        }

        private void dibujarBarra(Graphics2D g2,
                                  int x,
                                  int bottom,
                                  int width,
                                  int height,
                                  Color color,
                                  String label,
                                  String value) {
            g2.setColor(color);
            g2.fillRoundRect(x, bottom - height, width, height, 10, 10);
            dibujarTextoCentrado(g2, value, x + width / 2, bottom - height - 10, 12, Font.BOLD, new Color(32, 44, 57));
            dibujarTextoCentrado(g2, label, x + width / 2, bottom + 24, 12, Font.PLAIN, new Color(95, 111, 128));
        }

        private void dibujarTextoCentrado(Graphics2D g2,
                                          String texto,
                                          int x,
                                          int y,
                                          int size,
                                          int style,
                                          Color color) {
            g2.setFont(new Font("Segoe UI", style, size));
            g2.setColor(color);
            int ancho = g2.getFontMetrics().stringWidth(texto);
            g2.drawString(texto, x - ancho / 2, y);
        }

        private String formato(double numero) {
            if (numero >= 1000) {
                return String.format("%,.2f", numero);
            }
            return String.format("%.2f", numero);
        }
    }
}
