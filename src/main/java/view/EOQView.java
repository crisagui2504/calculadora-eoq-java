package view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EOQView extends JFrame {

    private static final Color BG_DARK = AppColors.BG_DARK;
    private static final Color SIDEBAR = AppColors.SIDEBAR;
    private static final Color CARD_BG = AppColors.CARD_BG;
    private static final Color BORDER = AppColors.BORDER;
    private static final Color GOLD = AppColors.GOLD;
    private static final Color TEXT_MAIN = AppColors.TEXT_MAIN;
    private static final Color TEXT_MUTED = AppColors.TEXT_MUTED;
    private static final Color FIELD = AppColors.FIELD;
    private static final Color BLUE = AppColors.BLUE;
    private static final Color GREEN = AppColors.GREEN;
    private static final Color PURPLE = AppColors.PURPLE;
    private static final Color STATUS_BG = AppColors.STATUS_BG;

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
    private JButton btnExportarPDF;
    private JButton btnExportar;
    private JButton btnSensibilidad;
    private JButton btnDetalleQ;
    private JButton btnDetalleN;
    private JButton btnDetalleT;
    private JButton btnDetalleCT;

    private JLabel lblEstado;
    private JTextArea taHistorial;

    public EOQView() {
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("EOQ Manager - Analisis de Inventario");
        setSize(1240, 860);
        setMinimumSize(new Dimension(1120, 760));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing usara su estilo por defecto si falla el Look & Feel.
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
        activarEnterParaCalcular();
    }

    private JPanel crearBarraLateral() {
        JPanel barra = new GradientPanel(SIDEBAR, new Color(13, 29, 46));
        barra.setPreferredSize(new Dimension(400, 760));
        barra.setLayout(new BorderLayout(0, 18));
        barra.setBorder(new EmptyBorder(22, 28, 18, 28));

        JPanel superior = new JPanel();
        superior.setOpaque(false);
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("EOQ Manager");
        titulo.setForeground(TEXT_MAIN);
        titulo.setFont(AppFonts.TITLE);

        JLabel subtitulo = new JLabel("<html>Planeacion de inventario, compras y costos operativos.</html>");
        subtitulo.setForeground(TEXT_MUTED);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setBorder(new EmptyBorder(4, 0, 8, 0));

        JSeparator separador = new JSeparator();
        separador.setForeground(BORDER);
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        superior.add(titulo);
        superior.add(subtitulo);
        superior.add(separador);
        superior.add(Box.createVerticalStrut(12));
        superior.add(crearEtiquetaLateral("Datos de entrada"));
        superior.add(Box.createVerticalStrut(10));

        tfDemanda = crearCampoEntrada("4800", "D: unidades vendidas o consumidas por anio.");
        tfCostoPedido = crearCampoEntrada("30", "Cp: costo de emitir una sola orden de compra.");
        tfCostoMantenimiento = crearCampoEntrada("8", "Cm: costo anual de mantener una unidad en inventario.");
        tfDias = crearCampoEntrada("240", "Dias habiles disponibles para reponer inventario.");

        superior.add(crearCampoConEtiqueta("Demanda anual (D)", "Unidades requeridas por anio", tfDemanda));
        superior.add(crearCampoConEtiqueta("Costo por pedido (Cp)", "Costo por emitir una orden", tfCostoPedido));
        superior.add(crearCampoConEtiqueta("Costo mantenimiento (Cm)", "Costo anual por unidad", tfCostoMantenimiento));
        superior.add(crearCampoConEtiqueta("Dias laborables", "Dias utiles de operacion", tfDias));

        JPanel inferior = new JPanel();
        inferior.setOpaque(false);
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));
        inferior.add(crearFormulaBox());
        inferior.add(Box.createVerticalStrut(10));

        btnCalcular = crearBoton("Calcular EOQ", GOLD, BG_DARK, true);
        btnLimpiar = crearBoton("Limpiar tablero", new Color(26, 54, 78), TEXT_MAIN, false);
        btnExportarPDF = crearBoton("Exportar PDF", new Color(82, 65, 130), TEXT_MAIN, false);
        btnExportar = crearBoton("Exportar CSV", new Color(28, 78, 107), TEXT_MAIN, false);
        btnSensibilidad = crearBoton("Analisis sensibilidad", new Color(32, 91, 77), TEXT_MAIN, false);

        inferior.add(btnCalcular);
        inferior.add(Box.createVerticalStrut(7));
        inferior.add(btnLimpiar);
        inferior.add(Box.createVerticalStrut(7));
        inferior.add(btnExportarPDF);
        inferior.add(Box.createVerticalStrut(7));
        inferior.add(btnExportar);
        inferior.add(Box.createVerticalStrut(7));
        inferior.add(btnSensibilidad);

        JScrollPane scrollDatos = new JScrollPane(superior);
        scrollDatos.setBorder(null);
        scrollDatos.setOpaque(false);
        scrollDatos.getViewport().setOpaque(false);
        scrollDatos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDatos.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDatos.getVerticalScrollBar().setUnitIncrement(14);
        estilizarScrollBar(scrollDatos.getVerticalScrollBar());

        barra.add(scrollDatos, BorderLayout.CENTER);
        barra.add(inferior, BorderLayout.SOUTH);
        return barra;
    }

    private void estilizarScrollBar(JScrollBar scrollBar) {
        scrollBar.setPreferredSize(new Dimension(10, 0));
        scrollBar.setOpaque(false);
        scrollBar.setBackground(SIDEBAR);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(57, 101, 132);
                this.trackColor = SIDEBAR;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonScrollInvisible();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonScrollInvisible();
            }

            @Override
            protected void paintTrack(Graphics g, javax.swing.JComponent c, java.awt.Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(12, 27, 43));
                g2.fillRoundRect(trackBounds.x + 2, trackBounds.y, trackBounds.width - 4, trackBounds.height, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintThumb(Graphics g, javax.swing.JComponent c, java.awt.Rectangle thumbBounds) {
                if (!scrollBar.isEnabled() || thumbBounds.height <= 0) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(76, 135, 176));
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
                g2.setColor(new Color(106, 168, 210));
                g2.drawRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 5, thumbBounds.height - 5, 8, 8);
                g2.dispose();
            }
        });
    }

    private JButton crearBotonScrollInvisible() {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(0, 0));
        boton.setMinimumSize(new Dimension(0, 0));
        boton.setMaximumSize(new Dimension(0, 0));
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        return boton;
    }

    private JPanel crearAreaResultados() {
        JPanel area = new JPanel(new BorderLayout(0, 18));
        area.setBackground(BG_DARK);
        area.setBorder(new EmptyBorder(28, 28, 20, 28));

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

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(tarjetas, BorderLayout.CENTER);
        centro.add(crearPanelHistorial(), BorderLayout.SOUTH);

        area.add(header, BorderLayout.NORTH);
        area.add(centro, BorderLayout.CENTER);
        return area;
    }

    private JPanel crearTarjetaResultado(String titulo,
                                         String subtitulo,
                                         JLabel valor,
                                         String unidad,
                                         JButton boton,
                                         Color acento,
                                         String icono) {
        MetricCard tarjeta = new MetricCard(acento, CARD_BG);
        tarjeta.setLayout(new BorderLayout(0, 8));
        tarjeta.setBorder(new EmptyBorder(22, 26, 18, 26));

        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.add(crearIconoTarjeta(icono, acento));
        cabecera.add(Box.createVerticalStrut(10));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXT_MUTED);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        cabecera.add(lblTitulo);

        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setForeground(TEXT_MUTED);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cabecera.add(Box.createVerticalStrut(7));
        cabecera.add(lblSubtitulo);

        JPanel valorPanel = new JPanel();
        valorPanel.setOpaque(false);
        valorPanel.setLayout(new BoxLayout(valorPanel, BoxLayout.Y_AXIS));
        valorPanel.setPreferredSize(new Dimension(260, 96));
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

        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setHovered(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setHovered(false);
            }
        });
        return tarjeta;
    }

    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));
        panel.setPreferredSize(new Dimension(100, 112));

        JLabel titulo = new JLabel("Historial de calculos recientes");
        titulo.setForeground(TEXT_MAIN);
        titulo.setFont(AppFonts.LABEL);

        taHistorial = new JTextArea("Sin calculos recientes.");
        taHistorial.setEditable(false);
        taHistorial.setFocusable(false);
        taHistorial.setOpaque(false);
        taHistorial.setLineWrap(true);
        taHistorial.setWrapStyleWord(true);
        taHistorial.setForeground(TEXT_MUTED);
        taHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(taHistorial, BorderLayout.CENTER);
        return panel;
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
        label.setFont(AppFonts.SECTION);
        return label;
    }

    private JPanel crearCampoConEtiqueta(String etiqueta, String ayuda, JTextField campo) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel label = new JLabel(etiqueta);
        label.setForeground(TEXT_MAIN);
        label.setFont(AppFonts.LABEL);

        JLabel help = new JLabel(ayuda);
        help.setForeground(TEXT_MUTED);
        help.setFont(AppFonts.HELP);

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(campo);
        panel.add(Box.createVerticalStrut(3));
        panel.add(help);
        return panel;
    }

    private JTextField crearCampoEntrada(String valor, String tooltip) {
        JTextField campo = new JTextField(valor);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setPreferredSize(new Dimension(260, 34));
        campo.setFont(AppFonts.FIELD);
        campo.setForeground(TEXT_MAIN);
        campo.setBackground(FIELD);
        campo.setCaretColor(GOLD);
        campo.setToolTipText(tooltip);
        aplicarBordeCampo(campo, BORDER);
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validarCampo(campo);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validarCampo(campo);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validarCampo(campo);
            }
        });
        return campo;
    }

    private JPanel crearFormulaBox() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(18, 42, 64));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 111, 143)),
                new EmptyBorder(14, 14, 14, 14)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

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
        label.setFont(new Font("Segoe UI", Font.BOLD, 32));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        return label;
    }

    private JButton crearBoton(String texto, Color fondo, Color textoColor, boolean primario) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        boton.setPreferredSize(new Dimension(260, 36));
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
        boton.setFont(AppFonts.BUTTON);
        return boton;
    }

    private void validarCampo(JTextField campo) {
        String texto = campo.getText().trim();
        boolean ok = true;
        if (!texto.isEmpty()) {
            try {
                ok = Double.parseDouble(texto) > 0;
            } catch (NumberFormatException ex) {
                ok = false;
            }
        }
        aplicarBordeCampo(campo, ok ? BORDER : AppColors.ERROR);
    }

    private void aplicarBordeCampo(JTextField campo, Color color) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color),
                new EmptyBorder(8, 11, 8, 11)
        ));
    }

    private void animarValor(JLabel label, double objetivo, String prefijo, String sufijo) {
        final int pasos = 30;
        final int[] paso = {0};
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            paso[0]++;
            double avance = Math.min(1.0, paso[0] / (double) pasos);
            double valor = objetivo * avance;
            label.setText(prefijo + formato(valor) + sufijo);
            if (paso[0] >= pasos) {
                label.setText(prefijo + formato(objetivo) + sufijo);
                timer.stop();
            }
        });
        timer.start();
    }

    private String formato(double numero) {
        if (numero >= 1000) {
            return String.format("%,.2f", numero);
        }
        return String.format("%.2f", numero);
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

    public void setResultadoQ(double valor) {
        animarValor(lblResultadoQ, valor, "", "");
    }

    public void setResultadoN(String valor) {
        lblResultadoN.setText(valor);
    }

    public void setResultadoN(double valor) {
        animarValor(lblResultadoN, valor, "", "");
    }

    public void setResultadoT(String valor) {
        lblResultadoT.setText(valor);
    }

    public void setResultadoT(double valor) {
        animarValor(lblResultadoT, valor, "", "");
    }

    public void setResultadoCT(String valor) {
        lblResultadoCT.setText(valor);
    }

    public void setResultadoCT(double valor) {
        animarValor(lblResultadoCT, valor, "$", "");
    }

    public void setDetallesHabilitados(boolean habilitados) {
        btnDetalleQ.setEnabled(habilitados);
        btnDetalleN.setEnabled(habilitados);
        btnDetalleT.setEnabled(habilitados);
        btnDetalleCT.setEnabled(habilitados);
        btnExportarPDF.setEnabled(habilitados);
        btnExportar.setEnabled(habilitados);
        btnSensibilidad.setEnabled(habilitados);
    }

    public void actualizarEstado(String texto) {
        lblEstado.setText("  *  " + texto);
    }

    public void actualizarHistorial(List<String> entradas) {
        if (entradas.isEmpty()) {
            taHistorial.setText("Sin calculos recientes.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String entrada : entradas) {
            sb.append(entrada).append(System.lineSeparator());
        }
        taHistorial.setText(sb.toString());
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

        lblEstado = new JLabel("  *  Listo para calcular");
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

    public void addExportarListener(ActionListener listener) {
        btnExportar.addActionListener(listener);
    }

    public void addExportarPDFListener(ActionListener listener) {
        btnExportarPDF.addActionListener(listener);
    }

    public void addSensibilidadListener(ActionListener listener) {
        btnSensibilidad.addActionListener(listener);
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

    public void activarEnterParaCalcular() {
        getRootPane().setDefaultButton(btnCalcular);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de entrada", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Informacion", JOptionPane.INFORMATION_MESSAGE);
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
        private boolean hovered;

        MetricCard(Color accent, Color surface) {
            this.accent = accent;
            this.surface = surface;
            setOpaque(false);
        }

        void setHovered(boolean hovered) {
            this.hovered = hovered;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? aclarar(surface, 14) : surface);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(hovered ? accent : BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(accent);
            g2.fillRect(0, 0, getWidth(), 3);
            g2.dispose();
            super.paintComponent(g);
        }

        private Color aclarar(Color color, int cantidad) {
            return new Color(
                    Math.min(255, color.getRed() + cantidad),
                    Math.min(255, color.getGreen() + cantidad),
                    Math.min(255, color.getBlue() + cantidad)
            );
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
