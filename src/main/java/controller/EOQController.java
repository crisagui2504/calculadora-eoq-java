package controller;

import model.EOQModel;
import view.EOQResultWindow;
import view.EOQView;

import java.awt.Color;

/**
 * CONTROLADOR - Conecta la vista principal, el modelo matematico y las
 * ventanas visuales de detalle.
 */
public class EOQController {

    private final EOQView vista;
    private final EOQModel modelo;

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

            if (txtD.isEmpty() || txtCp.isEmpty() ||
                    txtCm.isEmpty() || txtDias.isEmpty()) {
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

            vista.setResultadoQ(formato(ultimoQ));
            vista.setResultadoN(formato(ultimoN));
            vista.setResultadoT(formato(ultimoT));
            vista.setResultadoCT("$" + formato(ultimoCT));
            vista.setDetallesHabilitados(true);
            vista.actualizarEstado("Cálculo completado · D=" + formato(d)
                    + " · Cp=$" + formato(cp)
                    + " · Cm=$" + formato(cm));
            hayResultados = true;

        } catch (NumberFormatException ex) {
            vista.mostrarError(
                    "Entrada invalida.\n" +
                            "Asegurate de ingresar solo numeros (usa punto para decimales)."
            );
        } catch (IllegalStateException ex) {
            vista.mostrarError(ex.getMessage());
        }
    }

    private void limpiar() {
        hayResultados = false;
        vista.limpiarResultados();
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
            vista.mostrarInfo("Primero calcula los resultados EOQ para abrir el detalle.");
            return false;
        }
        return true;
    }

    private String formato(double numero) {
        if (numero >= 1000) {
            return String.format("%,.2f", numero);
        }
        return String.format("%.2f", numero);
    }
}
