package modelo;

/*
 * ============================================================================
 * CLASE: Movimiento
 * ============================================================================
 * (a) Que hace: Registra cada accion importante del dia en el puesto (ventas,
 *     entradas de lotes, retiro de vencidos, pedidos, domicilios o cambios de
 *     precio) para poder revisarla cronologicamente hacia adelante o hacia atras.
 * (b) Tema de Estructuras de Datos: Unidad 1 (TDA) y Unidad 3 - Dato almacenado
 *     en ListaDoble<Movimiento> (bonificacion de lista doblemente enlazada).
 * (c) Complejidad Big-O principal: O(1) en creacion y lectura de campos.
 * ============================================================================
 */
public class Movimiento {

    // === 1. ATRIBUTOS ===
    private final String idMovimiento;
    private final String fechaHora;
    private final String tipo; // VENTA, LOTE_INGRESO, LOTE_VENCIDO, PEDIDO, DOMICILIO, PRODUCTO
    private final String descripcion;
    private final long montoImpacto;

    // === 2. CONSTRUCTOR ===
    // Recibe los datos del evento ocurrido y los almacena de forma inmutable.
    public Movimiento(String idMovimiento, String fechaHora, String tipo,
                      String descripcion, long montoImpacto) {
        this.idMovimiento = idMovimiento;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.montoImpacto = montoImpacto;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Representacion en texto ---
    // No recibe parametros; devuelve la linea de bitacora para mostrar en pantalla.
    @Override
    public String toString() {
        return idMovimiento + " | " + fechaHora + " | [" + tipo + "] " + descripcion;
    }

    // === 4. GETTERS Y SETTERS ===

    public String getIdMovimiento() {
        return idMovimiento;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public long getMontoImpacto() {
        return montoImpacto;
    }
}
