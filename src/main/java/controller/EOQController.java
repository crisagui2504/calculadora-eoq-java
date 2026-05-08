package controller;

import model.EOQModel;
import view.EOQResultWindow;
import view.EOQSensitivityWindow;
import view.EOQView;

import javax.swing.JFileChooser;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EOQController {

    private final EOQView vista;
    private final EOQModel modelo;
    private final LinkedList<String> historial = new LinkedList<>();

    private double ultimoQ;
    private double ultimoN;
    private double ultimoT;
    private double ultimoCT;
    private double ultimoCostoPedidos;
    private double ultimoCostoMantenimiento;
    private boolean hayResultados;

    public EOQController(EOQView vista, EOQModel modelo) {
        this.vista = vista;
        this.modelo = modelo;

        vista.addCalcularListener(e -> calcular());
        vista.addLimpiarListener(e -> limpiar());
        vista.addExportarPDFListener(e -> exportarPDF());
        vista.addExportarListener(e -> exportarCSV());
        vista.addSensibilidadListener(e -> abrirSensibilidad());
        vista.addDetalleQListener(e -> abrirDetalleQ());
        vista.addDetalleNListener(e -> abrirDetalleN());
        vista.addDetalleTListener(e -> abrirDetalleT());
        vista.addDetalleCTListener(e -> abrirDetalleCT());
    }

    private void calcular() {
        try {
            String txtD = vista.getDemandaText();
            String txtCp = vista.getCostoPedidoText();
            String txtCm = vista.getCostoMantenimientoText();
            String txtDias = vista.getDiasText();

            if (txtD.isEmpty() || txtCp.isEmpty() || txtCm.isEmpty() || txtDias.isEmpty()) {
                vista.mostrarError("Por favor, completa todos los campos.");
                return;
            }

            double d = Double.parseDouble(txtD);
            double cp = Double.parseDouble(txtCp);
            double cm = Double.parseDouble(txtCm);
            double dias = Double.parseDouble(txtDias);

            if (d <= 0 || cp <= 0 || cm <= 0 || dias <= 0) {
                vista.mostrarError("Todos los valores deben ser mayores que cero.");
                return;
            }

            modelo.setDemanda(d);
            modelo.setCostoPedido(cp);
            modelo.setCostoMantenimiento(cm);
            modelo.setDiasLaborables(dias);

            ultimoQ = modelo.calcularQ();
            ultimoN = modelo.calcularNumeroPedidos();
            ultimoT = modelo.calcularTiempoEntrePedidos();
            ultimoCT = modelo.calcularCostoTotal();
            ultimoCostoPedidos = modelo.calcularCostoPedidos();
            ultimoCostoMantenimiento = modelo.calcularCostoMantenimiento();

            vista.setResultadoQ(ultimoQ);
            vista.setResultadoN(ultimoN);
            vista.setResultadoT(ultimoT);
            vista.setResultadoCT(ultimoCT);
            vista.setDetallesHabilitados(true);
            vista.actualizarEstado("Calculo completado - D=" + formato(d)
                    + " - Cp=$" + formato(cp)
                    + " - Cm=$" + formato(cm));
            agregarHistorial();
            hayResultados = true;

        } catch (NumberFormatException ex) {
            vista.mostrarError("Entrada invalida. Ingresa solo numeros y usa punto para decimales.");
        } catch (IllegalStateException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    private void limpiar() {
        hayResultados = false;
        vista.limpiarResultados();
    }

    private void exportarCSV() {
        if (!validarResultados()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("resultados_eoq.csv"));
        if (chooser.showSaveDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
            pw.println("Indicador,Valor,Unidad");
            pw.printf("Lote economico Q*,%s,unidades%n", formato(ultimoQ));
            pw.printf("Pedidos al anio,%s,pedidos%n", formato(ultimoN));
            pw.printf("Tiempo entre pedidos,%s,dias%n", formato(ultimoT));
            pw.printf("Costo total anual,%s,MXN%n", formato(ultimoCT));
            pw.println();
            pw.println("Parametro,Valor");
            pw.printf("Demanda anual,%s%n", formato(modelo.getDemanda()));
            pw.printf("Costo por pedido,%s%n", formato(modelo.getCostoPedido()));
            pw.printf("Costo mantenimiento,%s%n", formato(modelo.getCostoMantenimiento()));
            pw.printf("Dias laborables,%s%n", formato(modelo.getDiasLaborables()));
            vista.mostrarInfo("CSV exportado correctamente.");
        } catch (IOException ex) {
            vista.mostrarError("No se pudo exportar el archivo: " + ex.getMessage());
        }
    }

    private void exportarPDF() {
        if (!validarResultados()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("resultados_eoq.pdf"));
        if (chooser.showSaveDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        List<String> lineas = List.of(
                "Resultados EOQ",
                "Lote economico Q*: " + formato(ultimoQ) + " unidades",
                "Pedidos al anio: " + formato(ultimoN),
                "Tiempo entre pedidos: " + formato(ultimoT) + " dias",
                "Costo total anual: $" + formato(ultimoCT),
                "",
                "Parametros",
                "Demanda anual: " + formato(modelo.getDemanda()),
                "Costo por pedido: $" + formato(modelo.getCostoPedido()),
                "Costo mantenimiento: $" + formato(modelo.getCostoMantenimiento()),
                "Dias laborables: " + formato(modelo.getDiasLaborables())
        );

        try {
            escribirPDFSimple(chooser.getSelectedFile(), lineas);
            vista.mostrarInfo("PDF exportado correctamente.");
        } catch (IOException ex) {
            vista.mostrarError("No se pudo exportar el PDF: " + ex.getMessage());
        }
    }

    private void abrirSensibilidad() {
        if (!validarResultados()) {
            return;
        }
        new EOQSensitivityWindow(modelo.calcularSensibilidadQ(0.5, 1.5, 9)).setVisible(true);
    }

    private void abrirDetalleQ() {
        if (!validarResultados()) {
            return;
        }

        EOQResultWindow ventana = new EOQResultWindow(
                "Lote economico Q*",
                "Cantidad recomendada para cada orden de compra",
                formato(ultimoQ),
                "unidades",
                "Q* = sqrt((2 x D x Cp) / Cm)",
                "La empresa deberia pedir este volumen en cada reposicion para equilibrar el costo de ordenar y el costo de mantener inventario.",
                "Demanda anual: " + formato(modelo.getDemanda()) + " unidades",
                "Costo por pedido: $" + formato(modelo.getCostoPedido()),
                "Costo mantenimiento: $" + formato(modelo.getCostoMantenimiento()) + " por unidad/anio",
                new Color(18, 94, 145),
                EOQResultWindow.ChartType.Q_OPTIMO,
                ultimoQ,
                modelo.getDemanda(),
                modelo.getCostoMantenimiento()
        );
        ventana.setVisible(true);
    }

    private void abrirDetalleN() {
        if (!validarResultados()) {
            return;
        }

        double menorFrecuencia = Math.max(1.0, ultimoN * 0.65);
        double mayorFrecuencia = ultimoN * 1.35;

        EOQResultWindow ventana = new EOQResultWindow(
                "Numero de pedidos al anio",
                "Frecuencia anual estimada de reposicion",
                formato(ultimoN),
                "pedidos",
                "N = D / Q*",
                "Este resultado indica cuantas ordenes se harian durante el anio si cada pedido usa el lote economico recomendado.",
                "Demanda anual: " + formato(modelo.getDemanda()) + " unidades",
                "Lote por pedido: " + formato(ultimoQ) + " unidades",
                "Equivale a una politica de compra periodica y estable.",
                new Color(30, 118, 96),
                EOQResultWindow.ChartType.PEDIDOS,
                ultimoN,
                menorFrecuencia,
                mayorFrecuencia
        );
        ventana.setVisible(true);
    }

    private void abrirDetalleT() {
        if (!validarResultados()) {
            return;
        }

        EOQResultWindow ventana = new EOQResultWindow(
                "Tiempo entre pedidos",
                "Intervalo sugerido entre una orden y la siguiente",
                formato(ultimoT),
                "dias",
                "T = dias laborables / N",
                "Este ritmo ayuda a programar compras y revisiones de inventario sin esperar a que el stock llegue a niveles criticos.",
                "Dias laborables: " + formato(modelo.getDiasLaborables()),
                "Pedidos al anio: " + formato(ultimoN),
                "Intervalo operativo: cada " + formato(ultimoT) + " dias.",
                new Color(146, 94, 25),
                EOQResultWindow.ChartType.TIEMPO,
                ultimoT,
                modelo.getDiasLaborables(),
                ultimoN
        );
        ventana.setVisible(true);
    }

    private void abrirDetalleCT() {
        if (!validarResultados()) {
            return;
        }

        EOQResultWindow ventana = new EOQResultWindow(
                "Costo total anual",
                "Costo relevante anual asociado al EOQ",
                "$" + formato(ultimoCT),
                "pesos",
                "CT = Cp x (D / Q*) + Cm x (Q* / 2)",
                "El costo total combina el gasto anual de emitir pedidos y el costo anual de mantener inventario promedio.",
                "Costo anual de pedidos: $" + formato(ultimoCostoPedidos),
                "Costo anual de mantenimiento: $" + formato(ultimoCostoMantenimiento),
                "Costo minimo estimado: $" + formato(ultimoCT),
                new Color(122, 65, 144),
                EOQResultWindow.ChartType.COSTO,
                ultimoCT,
                ultimoCostoPedidos,
                ultimoCostoMantenimiento
        );
        ventana.setVisible(true);
    }

    private boolean validarResultados() {
        if (!hayResultados) {
            vista.mostrarInfo("Primero calcula los resultados EOQ.");
            return false;
        }
        return true;
    }

    private void agregarHistorial() {
        historial.addFirst("Q*=" + formato(ultimoQ)
                + " | N=" + formato(ultimoN)
                + " | T=" + formato(ultimoT)
                + " | CT=$" + formato(ultimoCT));
        if (historial.size() > 5) {
            historial.removeLast();
        }
        vista.actualizarHistorial(List.copyOf(historial));
    }

    private String formato(double numero) {
        if (numero >= 1000) {
            return String.format("%,.2f", numero);
        }
        return String.format("%.2f", numero);
    }

    private void escribirPDFSimple(java.io.File archivo, List<String> lineas) throws IOException {
        StringBuilder contenido = new StringBuilder();
        contenido.append("BT\n/F1 18 Tf\n72 760 Td\n");
        for (int i = 0; i < lineas.size(); i++) {
            if (i == 1) {
                contenido.append("/F1 12 Tf\n");
            }
            contenido.append("(").append(escaparPDF(lineas.get(i))).append(") Tj\n0 -24 Td\n");
        }
        contenido.append("ET\n");

        List<String> objetos = new ArrayList<>();
        objetos.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objetos.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        objetos.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");
        objetos.add("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        objetos.add("5 0 obj\n<< /Length " + contenido.length() + " >>\nstream\n" + contenido + "endstream\nendobj\n");

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (String objeto : objetos) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length);
            pdf.append(objeto);
        }
        int xref = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
        pdf.append("xref\n0 ").append(objetos.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objetos.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xref).append("\n%%EOF");

        try (FileOutputStream out = new FileOutputStream(archivo)) {
            out.write(pdf.toString().getBytes(StandardCharsets.ISO_8859_1));
        }
    }

    private String escaparPDF(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
