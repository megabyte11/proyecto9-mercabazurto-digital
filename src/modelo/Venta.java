package modelo;

/*
 * ============================================================================
 * CLASE: Venta
 * ============================================================================
 * (a) Que hace: Guarda el comprobante historico e inmutable de cada venta
 *     realizada en el puesto. Conserva una "foto" (snapshot) del nombre del
 *     producto, precio de venta aplicado, costo real de los lotes descontados
 *     y ganancia obtenida en ese instante. Si el producto cambia de precio
 *     despues, esta venta nunca se altera.
 * (b) Tema de Estructuras de Datos: Unidad 1 - Tipo de Dato Abstracto (TDA,
 *     tema 1.6) con proteccion de inmutabilidad historica.
 * (c) Complejidad Big-O principal: O(1) en creacion y consulta de atributos.
 * ============================================================================
 */
public class Venta {

    // === 1. ATRIBUTOS ===
    private final String idVenta;
    private final String fechaHora;
    private final String codigoProducto;
    private final String nombreProductoSnapshot;
    private final String cliente;
    private final int cantidadKg;
    private final long precioVentaAplicado;
    private final long costoCompraTotal;
    private final long totalVenta;
    private final long gananciaReal;
    private final String detalleLotesUsados;

    // === 2. CONSTRUCTOR ===
    // Recibe los datos finales de la venta y congela el snapshot de precios y ganancia.
    public Venta(String idVenta, String fechaHora, String codigoProducto,
                 String nombreProductoSnapshot, String cliente, int cantidadKg,
                 long precioVentaAplicado, long costoCompraTotal, String detalleLotesUsados) {
        this.idVenta = idVenta;
        this.fechaHora = fechaHora;
        this.codigoProducto = codigoProducto;
        this.nombreProductoSnapshot = nombreProductoSnapshot;
        if (cliente == null || cliente.trim().isEmpty()) {
            this.cliente = "Venta de mostrador";
        } else {
            this.cliente = cliente.trim();
        }
        this.cantidadKg = cantidadKg;
        this.precioVentaAplicado = precioVentaAplicado;
        this.costoCompraTotal = costoCompraTotal;
        this.totalVenta = (long) cantidadKg * precioVentaAplicado;
        this.gananciaReal = this.totalVenta - this.costoCompraTotal;
        this.detalleLotesUsados = detalleLotesUsados;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Costo promedio por kilo de los lotes vendidos ---
    // No recibe parametros; devuelve el costo unitario promedio real de esta venta.
    public long getCostoCompraUnitarioPromedio() {
        if (cantidadKg <= 0) {
            return 0L;
        }
        return costoCompraTotal / cantidadKg;
    }

    // --- 2. Representacion en texto ---
    // No recibe parametros; devuelve un resumen legible de la venta.
    @Override
    public String toString() {
        return idVenta + " | " + fechaHora + " | " + nombreProductoSnapshot
                + " (" + cantidadKg + " kg) | Total: $" + totalVenta
                + " | Ganancia: $" + gananciaReal;
    }

    // === 4. GETTERS Y SETTERS ===
    // Nota: Esta clase NO tiene setters porque una venta registrada es historica e inmutable.

    public String getIdVenta() {
        return idVenta;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreProductoSnapshot() {
        return nombreProductoSnapshot;
    }

    public String getCliente() {
        return cliente;
    }

    public int getCantidadKg() {
        return cantidadKg;
    }

    public long getPrecioVentaAplicado() {
        return precioVentaAplicado;
    }

    public long getCostoCompraTotal() {
        return costoCompraTotal;
    }

    public long getTotalVenta() {
        return totalVenta;
    }

    public long getGananciaReal() {
        return gananciaReal;
    }

    public String getDetalleLotesUsados() {
        return detalleLotesUsados;
    }
}
