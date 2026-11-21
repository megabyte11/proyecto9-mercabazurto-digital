package modelo;

import java.time.LocalDate;

/*
 * ============================================================================
 * CLASE: Lote
 * ============================================================================
 * (a) Que hace: Representa una entrada fisica (canasta o bulto) de un producto
 *     perecedero en el puesto del mercado, con su fecha de vencimiento y el
 *     precio de compra historico que tenia el producto al ingresar este lote.
 * (b) Tema de Estructuras de Datos: Unidad 1 - Abstraccion y Tipo de Dato
 *     Abstracto (TDA, tema 1.6).
 * (c) Complejidad Big-O principal: O(1) en todas sus operaciones de consulta
 *     y actualizacion de kilos.
 * ============================================================================
 */
public class Lote {

    // === 1. ATRIBUTOS ===
    private final String codigoLote;
    private final String codigoProducto;
    private final String fechaVencimiento; // Formato ISO "AAAA-MM-DD" para comparar facilmente
    private int cantidadKg;
    // Snapshot historico inmutable: si el precio del producto cambia despues,
    // este lote conserva el costo real al que se compro.
    private final long precioCompraKg;

    // === 2. CONSTRUCTOR ===
    // Recibe los datos del lote al momento de entrar al puesto y fija su costo historico.
    public Lote(String codigoLote, String codigoProducto, String fechaVencimiento,
                int cantidadKg, long precioCompraKg) {
        this.codigoLote = codigoLote;
        this.codigoProducto = codigoProducto;
        this.fechaVencimiento = fechaVencimiento;
        this.cantidadKg = cantidadKg;
        this.precioCompraKg = precioCompraKg;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Calculo de valor del lote ---
    // No recibe parametros; multiplica los kilos actuales por el costo de compra historico.
    public long calcularValorLote() {
        return (long) cantidadKg * precioCompraKg;
    }

    // --- 2. Verificacion de vencimiento ---
    // Recibe la fecha actual en formato ISO "AAAA-MM-DD"; devuelve true si el lote ya vencio.
    public boolean estaVencido(String fechaHoy) {
        // En formato ISO AAAA-MM-DD el orden alfabetico coincide con el orden del calendario
        return fechaVencimiento.compareTo(fechaHoy) < 0;
    }

    // --- 3. Calculo de dias restantes para vencer ---
    // Recibe la fecha actual en formato ISO; devuelve los dias que faltan (negativo si vencio).
    public long calcularDiasParaVencer(String fechaHoy) {
        LocalDate hoy = LocalDate.parse(fechaHoy);
        LocalDate vencimiento = LocalDate.parse(fechaVencimiento);
        return vencimiento.toEpochDay() - hoy.toEpochDay();
    }

    // --- 4. Clasificacion visual del estado del lote ---
    // Recibe la fecha de hoy; devuelve "VENCIDO", "PROXIMO" (0 a 3 dias) o "VIGENTE".
    public String obtenerEstado(String fechaHoy) {
        long dias = calcularDiasParaVencer(fechaHoy);
        if (dias < 0) {
            return "VENCIDO";
        }
        if (dias <= 3) {
            return "PROXIMO";
        }
        return "VIGENTE";
    }

    // --- 5. Copia independiente del lote ---
    // No recibe parametros; devuelve una copia exacta util para simulaciones sin dañar datos reales.
    public Lote clonar() {
        return new Lote(codigoLote, codigoProducto, fechaVencimiento, cantidadKg, precioCompraKg);
    }

    // --- 6. Representacion en texto ---
    // No recibe parametros; devuelve una linea clara para mostrar en consola.
    @Override
    public String toString() {
        return codigoLote + " [" + codigoProducto + " | " + cantidadKg
                + " kg | vence " + fechaVencimiento + " | $" + precioCompraKg + "/kg]";
    }

    // === 4. GETTERS Y SETTERS ===

    public String getCodigoLote() {
        return codigoLote;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getCantidadKg() {
        return cantidadKg;
    }

    public void setCantidadKg(int cantidadKg) {
        this.cantidadKg = cantidadKg;
    }

    public long getPrecioCompraKg() {
        return precioCompraKg;
    }
}
