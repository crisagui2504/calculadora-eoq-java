package model;

/**
 * MODELO - Contiene toda la logica matematica del EOQ.
 * No conoce nada sobre la interfaz grafica.
 *
 * Formulas implementadas:
 * Q* = sqrt(2 * D * Cp / Cm)
 * N  = D / Q*
 * T  = diasLaborables / N
 * CT = Cp * (D / Q*) + Cm * (Q* / 2)
 */
public class EOQModel {

    // Atributos privados
    private double demanda;            // D  - unidades por anio
    private double costoPedido;        // Cp - costo por orden
    private double costoMantenimiento; // Cm - costo de almacen por unidad/anio
    private double diasLaborables;     // dias habiles en el anio
    private double qCache = -1.0;      // evita recalcular Q* si los datos no cambian

    // Constructor
    public EOQModel(double demanda,
                    double costoPedido,
                    double costoMantenimiento,
                    double diasLaborables) {
        this.demanda = demanda;
        this.costoPedido = costoPedido;
        this.costoMantenimiento = costoMantenimiento;
        this.diasLaborables = diasLaborables;
    }

    // Setters: el Controlador actualiza el modelo con nuevos datos.
    public void setDemanda(double demanda) {
        this.demanda = demanda;
        invalidarCache();
    }

    public void setCostoPedido(double costoPedido) {
        this.costoPedido = costoPedido;
        invalidarCache();
    }

    public void setCostoMantenimiento(double costoMantenimiento) {
        this.costoMantenimiento = costoMantenimiento;
        invalidarCache();
    }

    public void setDiasLaborables(double diasLaborables) {
        this.diasLaborables = diasLaborables;
    }

    // Getters de entrada.
    public double getDemanda() {
        return demanda;
    }

    public double getCostoPedido() {
        return costoPedido;
    }

    public double getCostoMantenimiento() {
        return costoMantenimiento;
    }

    public double getDiasLaborables() {
        return diasLaborables;
    }

    /**
     * a) Lote economico Q* = sqrt(2 * D * Cp / Cm)
     * Con D=4800, Cp=30, Cm=8: Q* ~= 189.74 unidades.
     */
    public double calcularQ() {
        validarDatosCalculo();
        if (qCache < 0) {
            qCache = Math.sqrt((2.0 * demanda * costoPedido) / costoMantenimiento);
        }
        return qCache;
    }

    /**
     * b) Numero de pedidos al anio: N = D / Q*
     * Con los datos del ejemplo: N ~= 25.30 pedidos.
     */
    public double calcularNumeroPedidos() {
        return demanda / calcularQ();
    }

    /**
     * c) Tiempo entre pedidos: T = diasLaborables / N
     * Con 240 dias: T ~= 9.49 dias.
     */
    public double calcularTiempoEntrePedidos() {
        return diasLaborables / calcularNumeroPedidos();
    }

    /**
     * d) Costo total anual: CT = Cp * (D / Q*) + Cm * (Q* / 2)
     * Con los datos del ejemplo: CT ~= $1,517.89.
     */
    public double calcularCostoTotal() {
        return calcularCostoPedidos() + calcularCostoMantenimiento();
    }

    /**
     * Costo anual por emitir pedidos: Cp * (D / Q*).
     */
    public double calcularCostoPedidos() {
        return costoPedido * (demanda / calcularQ());
    }

    /**
     * Costo anual por mantener inventario: Cm * (Q* / 2).
     */
    public double calcularCostoMantenimiento() {
        return costoMantenimiento * (calcularQ() / 2.0);
    }

    /**
     * Calcula Q* variando el costo de pedido desde factorMin hasta factorMax.
     */
    public double[] calcularSensibilidadQ(double factorMin, double factorMax, int pasos) {
        if (pasos < 2) {
            throw new IllegalArgumentException("La sensibilidad requiere al menos 2 pasos.");
        }
        validarDatosCalculo();

        double[] valores = new double[pasos];
        double paso = (factorMax - factorMin) / (pasos - 1);

        for (int i = 0; i < pasos; i++) {
            double factor = factorMin + paso * i;
            valores[i] = Math.sqrt((2.0 * demanda * costoPedido * factor) / costoMantenimiento);
        }
        return valores;
    }

    private void invalidarCache() {
        qCache = -1.0;
    }

    private void validarDatosCalculo() {
        if (demanda <= 0) {
            throw new IllegalStateException("La demanda debe ser mayor que cero.");
        }
        if (costoPedido <= 0) {
            throw new IllegalStateException("El costo por pedido debe ser mayor que cero.");
        }
        if (costoMantenimiento <= 0) {
            throw new IllegalStateException("El costo de mantenimiento debe ser mayor que cero.");
        }
    }
}
