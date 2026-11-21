package modelo;

import estructuras.ListaSimpleLotes;

/*
 * ============================================================================
 * CLASE: Producto
 * ============================================================================
 * (a) Que hace: Modela cada producto perecedero del catalogo del puesto
 *     (ejemplo: Mango Tommy, Pargo Rojo, Tomate Chonto), sus precios vigentes
 *     y su propia ListaSimpleLotes ordenada por fecha de vencimiento (FEFO).
 * (b) Tema de Estructuras de Datos: Unidad 1 - Tipo de Dato Abstracto (TDA,
 *     tema 1.6) integrado con Unidad 3 - Lista enlazada simple de lotes (3.3).
 * (c) Complejidad Big-O principal:
 *     - Consulta de precios y ganancia: O(1).
 *     - Valor recursivo del inventario del producto: O(m), donde m es su numero de lotes.
 * ============================================================================
 */
public class Producto {

    // === 1. ATRIBUTOS ===
    private final String codigo;
    private String nombre;
    private String categoria; // "Fruta", "Verdura" o "Pescado"
    private long precioCompraKg;
    private long precioVentaKg;
    private final ListaSimpleLotes lotes;

    // === 2. CONSTRUCTOR ===
    // Recibe los datos basicos del producto e inicializa vacia su lista FEFO de lotes.
    public Producto(String codigo, String nombre, String categoria,
                    long precioCompraKg, long precioVentaKg) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioCompraKg = precioCompraKg;
        this.precioVentaKg = precioVentaKg;
        this.lotes = new ListaSimpleLotes();
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Calculo de ganancia unitaria por kilo ---
    // No recibe parametros; devuelve la diferencia entre precio de venta y precio de compra.
    public long calcularGananciaPorKg() {
        return precioVentaKg - precioCompraKg;
    }

    // --- 2. Calculo de porcentaje de margen sobre el precio de venta ---
    // No recibe parametros; devuelve el porcentaje de ganancia (ej. 31.9%).
    public double calcularPorcentajeMargen() {
        if (precioVentaKg <= 0) {
            return 0.0;
        }
        return ((double) calcularGananciaPorKg() * 100.0) / (double) precioVentaKg;
    }

    // --- 3. Actualizacion controlada del producto (respeta historial) ---
    // Recibe nuevos nombre, categoria y precios vigentes.
    // REGLA DE INMUTABILIDAD HISTORICA: solo cambia los precios futuros de este producto;
    // NO toca ningun Lote ya ingresado en 'this.lotes' ni ninguna Venta ya registrada.
    public void actualizarDatos(String nuevoNombre, String nuevaCategoria,
                                long nuevoPrecioCompraKg, long nuevoPrecioVentaKg) {
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            this.nombre = nuevoNombre.trim();
        }
        if (nuevaCategoria != null && !nuevaCategoria.trim().isEmpty()) {
            this.categoria = nuevaCategoria.trim();
        }
        if (nuevoPrecioCompraKg > 0) {
            this.precioCompraKg = nuevoPrecioCompraKg;
        }
        if (nuevoPrecioVentaKg > 0) {
            this.precioVentaKg = nuevoPrecioVentaKg;
        }
    }

    // --- 4. Valor recursivo del inventario de este producto ---
    // No recibe parametros; llama a la suma recursiva sobre los nodos de su lista de lotes.
    public long calcularValorInventarioRecursivo() {
        return lotes.calcularValorRecursivo();
    }

    // --- 5. Kilos disponibles actualmente ---
    // No recibe parametros; suma los kilos de todos los lotes activos del producto.
    public int calcularKgDisponibles() {
        return lotes.calcularKgTotales();
    }

    // --- 6. Proxima fecha de vencimiento (cabeza FEFO) ---
    // No recibe parametros; devuelve la fecha del lote que vence primero o "-" si no hay stock.
    public String obtenerProximoVencimiento() {
        Lote primero = lotes.obtenerPrimero();
        if (primero == null) {
            return "-";
        }
        return primero.getFechaVencimiento();
    }

    // --- 7. Representacion en texto ---
    // No recibe parametros; devuelve un resumen legible para la consola.
    @Override
    public String toString() {
        return codigo + " | " + nombre + " (" + categoria + ") | Compra: $"
                + precioCompraKg + " | Venta: $" + precioVentaKg
                + " | Ganancia: $" + calcularGananciaPorKg() + "/kg";
    }

    // === 4. GETTERS Y SETTERS ===

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public long getPrecioCompraKg() {
        return precioCompraKg;
    }

    public void setPrecioCompraKg(long precioCompraKg) {
        this.precioCompraKg = precioCompraKg;
    }

    public long getPrecioVentaKg() {
        return precioVentaKg;
    }

    public void setPrecioVentaKg(long precioVentaKg) {
        this.precioVentaKg = precioVentaKg;
    }

    public ListaSimpleLotes getLotes() {
        return lotes;
    }
}
