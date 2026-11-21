package estructuras;

import modelo.Venta;

/*
 * ============================================================================
 * CLASE: ListaSimpleVentas
 * ============================================================================
 * (a) Que hace: Almacena el registro historico e inmutable de todas las ventas
 *     realizadas en el puesto. Cada venta nueva se agrega al inicio para ver
 *     primero lo mas reciente, y recorre la lista para sumar lo vendido y la
 *     ganancia real acumulada del dia.
 * (b) Tema de Estructuras de Datos: Unidad 3 - Lista enlazada simple (tema 3.3).
 * (c) Complejidad Big-O principal:
 *     - registrarVenta: O(1) (insercion en cabeza).
 *     - calcularTotalVendido / calcularGananciaTotal: O(n).
 * ============================================================================
 */
public class ListaSimpleVentas {

    // === 1. ATRIBUTOS ===
    private Nodo<Venta> cabeza;
    private int tamanio;

    // === 2. CONSTRUCTOR ===
    // No recibe parametros; crea el historial de ventas vacio.
    public ListaSimpleVentas() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Registrar venta nueva al inicio en O(1) ---
    // Recibe una Venta inmutable; la inserta en la cabeza de la lista en un solo paso.
    public void registrarVenta(Venta venta) {
        // Paso 1: Crear el nodo nuevo con el snapshot de la venta
        Nodo<Venta> nuevo = new Nodo<>(venta);

        // Paso 2: Enlazar el nodo nuevo antes de la cabeza actual
        nuevo.sig = cabeza;
        cabeza = nuevo;
        tamanio = tamanio + 1;
    }

    // --- 2. Sumar el dinero total vendido ---
    // No recibe parametros; recorre la lista y devuelve la suma total en pesos.
    public long calcularTotalVendido() {
        long suma = 0L;
        Nodo<Venta> actual = cabeza;
        while (actual != null) {
            suma = suma + actual.dato.getTotalVenta();
            actual = actual.sig;
        }
        return suma;
    }

    // --- 3. Sumar la ganancia neta real acumulada ---
    // No recibe parametros; recorre la lista y suma la ganancia historica de cada venta.
    public long calcularGananciaTotal() {
        long ganancia = 0L;
        Nodo<Venta> actual = cabeza;
        while (actual != null) {
            ganancia = ganancia + actual.dato.getGananciaReal();
            actual = actual.sig;
        }
        return ganancia;
    }

    // --- 4. Convertir el historial a arreglo propio ---
    // No recibe parametros; copia todas las ventas a un arreglo Venta[] desde la mas reciente.
    public Venta[] aArreglo() {
        Venta[] resultado = new Venta[tamanio];
        Nodo<Venta> actual = cabeza;
        int indice = 0;
        while (actual != null && indice < tamanio) {
            resultado[indice] = actual.dato;
            actual = actual.sig;
            indice = indice + 1;
        }
        return resultado;
    }

    // --- 5. Verificar si esta vacia ---
    // No recibe parametros; devuelve true si no existen ventas registradas.
    public boolean estaVacia() {
        return cabeza == null;
    }

    // === 4. GETTERS Y SETTERS ===

    public Nodo<Venta> getCabeza() {
        return cabeza;
    }

    public int getTamanio() {
        return tamanio;
    }
}
