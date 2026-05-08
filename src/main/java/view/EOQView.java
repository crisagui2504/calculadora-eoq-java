package view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

/**
 * Vista principal con apariencia de tablero empresarial.
 *
 * La pantalla separa claramente:
 * - Captura de datos en una barra lateral oscura.
 * - Indicadores EOQ en tarjetas amplias, contrastadas y faciles de leer.
 */
public class EOQView extends JFrame {

    private static final Color BG_DARK = new Color(14, 30, 48);
    private static final Color SIDEBAR = new Color(15, 33, 51);
    private static final Color CARD_BG = new Color(15, 33, 51);
    private static final Color BORDER = new Color(30, 58, 82);
    private static final Color GOLD = new Color(200, 149, 42);
    private static final Color TEXT_MAIN = new Color(232, 241, 248);
    private static final Color TEXT_MUTED = new Color(90, 126, 153);
    private static final Color FIELD = new Color(47, 48, 45);
    private static final Color BLUE = new Color(65, 151, 224);
    private static final Color GREEN = new Color(48, 177, 141);
    private static final Color PURPLE = new Color(137, 111, 226);
    private static final Color STATUS_BG = new Color(12, 27, 46);

    private JTextField tfDemanda;
    private JTextField tfCostoPedido;
    private JTextField tfCostoMantenimiento;
    private JTextField tfDias;

    private JLabel lblResultadoQ;
    private JLabel lblResultadoN;
    private JLabel lblResultadoT;
    private JLabel lblResultadoCT;

    private JButton btnCalcular;
    private JButton btnLimpiar;
    private JButton btnDetalleQ;
    private JButton btnDetalleN;
    private JButton btnDetalleT;
    private JButton btnDetalleCT;
    private JLabel lblEstado;

    public EOQView() {
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("EOQ Manager - Analisis de Inventario");
        setSize(1240, 820);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, Swing usara su apariencia por defecto.
        }
    }

    private void inicializarComponentes() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(BG_DARK);
        raiz.add(crearBarraLateral(), BorderLayout.WEST);
        raiz.add(crearAreaResultados(), BorderLayout.CENTER);
        raiz.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(raiz);
        setDetallesHabilitados(false);
    }

    private JPanel crearBarraLateral() {
        JPanel barra = new GradientPanel(SIDEBAR, new Color(13, 29, 46));
        barra.setPreferredSize(new Dimension(360, 720));
        barra.setLayout(new BorderLayout(0, 18));
        barra.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel superior = new JPanel();
        superior.setOpaque(false);
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("EOQ Manager");
        titulo.setForeground(TEXT_MAIN);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 31));

        JLabel subtitulo = new JLabel("<html>Planeacion de inventario, compras y costos operativos.</html>");
        subtitulo.setForeground(TEXT_MUTED);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setBorder(new EmptyBorder(6, 0, 18, 0));

        superior.add(titulo);
        superior.add(subtitulo);
        superior.add(crearEtiquetaLateral("Datos de entrada"));
        superior.add(Box.createVerticalStrut(14));

        tfDemanda = crearCampoEntrada("4800");
        tfCostoPedido = crearCampoEntrada("30");
        tfCostoMantenimiento = crearCampoEntrada("8");
        tfDias = crearCampoEntrada("240");

        superior.add(crearCampoConEtiqueta("Demanda anual (D)", "Unidades requeridas por anio", tfDemanda));
        superior.add(crearCampoConEtiqueta("Costo por pedido (Cp)", "Costo por emitir una orden", tfCostoPedido));
        superior.add(crearCampoConEtiqueta("Costo mantenimiento (Cm)", "Costo anual por unidad", tfCostoMantenimiento));
        superior.add(crearCampoConEtiqueta("Dias laborables", "Dias utiles de operacion", tfDias));

        JPanel inferior = new JPanel();
        inferior.setOpaque(false);
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));

        inferior.add(crearFormulaBox());
        inferior.add(Box.createVerticalStrut(18));

        btnCalcular = crearBoton("Calcular EOQ", GOLD, BG_DARK, true);
        btnLimpiar = crearBoton("Limpiar tablero", new Color(26, 54, 78), TEXT_MAIN, false);
        inferior.add(btnCalcular);
        inferior.add(Box.createVerticalStrut(10));
        inferior.add(btnLimpiar);

        barra.add(superior, BorderLayout.CENTER);
        barra.add(inferior, BorderLayout.SOUTH);
        return barra;
    }

    private JPanel crearAreaResultados() {
        JPanel area = new JPanel(new BorderLayout(0, 22));
        area.setBackground(BG_DARK);
        area.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Resultados EOQ");
        titulo.setForeground(TEXT_MAIN);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel subtitulo = new JLabel("Cada indicador abre una ventana dedicada con grafica, formula y lectura ejecutiva.");
        subtitulo.setForeground(TEXT_MUTED);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(5));
        textos.add(subtitulo);

        JLabel insignia = new JLabel("TECNO PARTES | INVENTARIO");
        insignia.setOpaque(true);
        insignia.setBackground(new Color(27, 66, 96));
        insignia.setForeground(TEXT_MAIN);
        insignia.setBorder(new EmptyBorder(10, 16, 10, 16));
        insignia.setFont(new Font("Segoe UI", Font.BOLD, 12));

        header.add(textos, BorderLayout.WEST);
        header.add(insignia, BorderLayout.EAST);

        JPanel tarjetas = new JPanel(new GridLayout(2, 2, 20, 20));
        tarjetas.setOpaque(false);

        lblResultadoQ = crearValorTarjeta();
        lblResultadoN = crearValorTarjeta();
        lblResultadoT = crearValorTarjeta();
        lblResultadoCT = crearValorTarjeta();

        btnDetalleQ = crearBotonDetalle("Abrir analisis");
        btnDetalleN = crearBotonDetalle("Abrir analisis");
        btnDetalleT = crearBotonDetalle("Abrir analisis");
        btnDetalleCT = crearBotonDetalle("Abrir analisis");

        tarjetas.add(crearTarjetaResultado("Lote economico Q*", "Unidades ideales por orden", lblResultadoQ, "unidades", btnDetalleQ, BLUE, "Q*"));
        tarjetas.add(crearTarjetaResultado("Pedidos al anio", "Frecuencia anual de reposicion", lblResultadoN, "pedidos", btnDetalleN, GREEN, "N"));
        tarjetas.add(crearTarjetaResultado("Tiempo entre pedidos", "Intervalo recomendado", lblResultadoT, "dias", btnDetalleT, GOLD, "T"));
        tarjetas.add(crearTarjetaResultado("Costo total anual", "Costo relevante minimo", lblResultadoCT, "MXN", btnDetalleCT, PURPLE, "$"));

        area.add(header, BorderLayout.NORTH);
        area.add(tarjetas, BorderLayout.CENTER);
        return area;
    }

    private JPanel crearTarjetaResultado(String titulo,
                                         String subtitulo,
                                         JLabel valor,
                                         String unidad,
                                         JButton boton,
                                         Color acento,
                                         String icono) {
        JPanel tarjeta = new MetricCard(acento, CARD_BG);
        tarjeta.setLayout(new BorderLayout(0, 16));
        tarjeta.setBorder(new EmptyBorder(32, 28, 24, 28));

        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));

        JPanel textoCabecera = new JPanel();
        textoCabecera.setOpaque(false);
        textoCabecera.setLayout(new BoxLayout(textoCabecera, BoxLayout.Y_AXIS));

        JLabel iconoLabel = crearIconoTarjeta(icono, acento);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXT_MUTED);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setForeground(TEXT_MUTED);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        textoCabecera.add(iconoLabel);
        textoCabecera.add(Box.createVerticalStrut(18));
        textoCabecera.add(lblTitulo);
        textoCabecera.add(Box.createVerticalStrut(8));
        textoCabecera.add(lblSubtitulo);

        cabecera.add(textoCabecera);

        JPanel valorPanel = new JPanel();
        valorPanel.setOpaque(false);
        valorPanel.setLayout(new BoxLayout(valorPanel, BoxLayout.Y_AXIS));
        valorPanel.setPreferredSize(new Dimension(250, 118));
        valorPanel.add(Box.createVerticalGlue());
        valor.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        valorPanel.add(valor);

        JLabel lblUnidad = new JLabel(unidad, JLabel.CENTER);
        lblUnidad.setForeground(TEXT_MUTED);
        lblUnidad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUnidad.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        valorPanel.add(Box.createVerticalStrut(6));
        valorPanel.add(lblUnidad);
        valorPanel.add(Box.createVerticalGlue());

        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(valorPanel, BorderLayout.CENTER);
        tarjeta.add(boton, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JLabel crearIconoTarjeta(String texto, Color acento) {
        JLabel label = new IconBadge(texto, acento);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setPreferredSize(new Dimension(64, 52));
        label.setMaximumSize(new Dimension(64, 52));
        return label;
    }

    private JLabel crearEtiquetaLateral(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(GOLD);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        return label;
    }

    private JPanel crearCampoConEtiqueta(String etiqueta, String ayuda, JTextField campo) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 0, 13, 0));

        JLabel label = new JLabel(etiqueta);
        label.setForeground(TEXT_MAIN);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel help = new JLabel(ayuda);
        help.setForeground(TEXT_MUTED);
        help.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(campo);
        panel.add(Box.createVerticalStrut(3));
        panel.add(help);
        return panel;
    }

    private JTextField crearCampoEntrada(String valor) {
        JTextField campo = new JTextField(valor);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        campo.setPreferredSize(new Dimension(260, 42));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        campo.setForeground(TEXT_MAIN);
        campo.setBackground(FIELD);
        campo.setCaretColor(GOLD);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 89, 81)),
                new EmptyBorder(8, 11, 8, 11)
        ));
        return campo;
    }

    private JPanel crearFormulaBox() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(18, 42, 64));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 111, 143)),
                new EmptyBorder(14, 14, 14, 14)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel titulo = new JLabel("Formula central");
        titulo.setForeground(TEXT_MUTED);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(titulo, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 0, 0);
        JLabel formula = new JLabel("<html>Q* = &radic;((2 &times; D &times; Cp) / Cm)</html>");
        formula.setForeground(GOLD);
        formula.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(formula, gbc);
        return panel;
    }

    private JLabel crearValorTarjeta() {
        JLabel label = new JLabel("--", JLabel.CENTER);
        label.setForeground(TEXT_MAIN);
        label.setFont(new Font("Segoe UI", Font.BOLD, 36));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        return label;
    }

    private JButton crearBoton(String texto, Color fondo, Color textoColor, boolean primario) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        boton.setPreferredSize(new Dimension(260, 44));
        boton.setBackground(fondo);
        boton.setForeground(textoColor);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setFont(new Font("Segoe UI", Font.BOLD, primario ? 15 : 14));
        return boton;
    }

    private JButton crearBotonDetalle(String texto) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(180, 40));
        boton.setBackground(new Color(26, 72, 105));
        boton.setForeground(TEXT_MAIN);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return boton;
    }

    public String getDemandaText() {
        return tfDemanda.getText().trim();
    }

    public String getCostoPedidoText() {
        return tfCostoPedido.getText().trim();
    }

    public String getCostoMantenimientoText() {
        return tfCostoMantenimiento.getText().trim();
    }

    public String getDiasText() {
        return tfDias.getText().trim();
    }

    public void setResultadoQ(String valor) {
        lblResultadoQ.setText(valor);
    }

    public void setResultadoN(String valor) {
        lblResultadoN.setText(valor);
    }

    public void setResultadoT(String valor) {
        lblResultadoT.setText(valor);
    }

    public void setResultadoCT(String valor) {
        lblResultadoCT.setText(valor);
    }

    public void setDetallesHabilitados(boolean habilitados) {
        btnDetalleQ.setEnabled(habilitados);
        btnDetalleN.setEnabled(habilitados);
        btnDetalleT.setEnabled(habilitados);
        btnDetalleCT.setEnabled(habilitados);
    }

    public void actualizarEstado(String texto) {
        lblEstado.setText("  ●  " + texto);
    }

    public void limpiarResultados() {
        lblResultadoQ.setText("--");
        lblResultadoN.setText("--");
        lblResultadoT.setText("--");
        lblResultadoCT.setText("--");
        tfDemanda.setText("");
        tfCostoPedido.setText("");
        tfCostoMantenimiento.setText("");
        tfDias.setText("");
        setDetallesHabilitados(false);
        actualizarEstado("Listo para calcular");
        tfDemanda.requestFocus();
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setPreferredSize(new Dimension(100, 32));
        barra.setBackground(STATUS_BG);
        barra.setBorder(new EmptyBorder(0, 22, 0, 22));

        lblEstado = new JLabel("  ●  Listo para calcular");
        lblEstado.setForeground(new Color(86, 207, 133));
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        barra.add(lblEstado, BorderLayout.WEST);
        return barra;
    }

    public void addCalcularListener(ActionListener listener) {
        btnCalcular.addActionListener(listener);
    }

    public void addLimpiarListener(ActionListener listener) {
        btnLimpiar.addActionListener(listener);
    }

    public void addDetalleQListener(ActionListener listener) {
        btnDetalleQ.addActionListener(listener);
    }

    public void addDetalleNListener(ActionListener listener) {
        btnDetalleN.addActionListener(listener);
    }

    public void addDetalleTListener(ActionListener listener) {
        btnDetalleT.addActionListener(listener);
    }

    public void addDetalleCTListener(ActionListener listener) {
        btnDetalleCT.addActionListener(listener);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error de entrada",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Informacion",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static class GradientPanel extends JPanel {
        private final Color inicio;
        private final Color fin;

        GradientPanel(Color inicio, Color fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, inicio, 0, getHeight(), fin));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class MetricCard extends JPanel {
        private final Color accent;
        private final Color surface;

        MetricCard(Color accent, Color surface) {
            this.accent = accent;
            this.surface = surface;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(surface);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(accent);
            g2.fillRect(0, 0, getWidth(), 3);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class IconBadge extends JLabel {
        private final Color accent;
        private final Color fill;

        IconBadge(String text, Color accent) {
            super(text, JLabel.CENTER);
            this.accent = accent;
            this.fill = new Color(
                    Math.max(8, accent.getRed() / 8),
                    Math.max(8, accent.getGreen() / 8),
                    Math.max(8, accent.getBlue() / 8)
            );
            setForeground(accent);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 45));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
