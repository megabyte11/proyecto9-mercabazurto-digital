package estructuras;

/*
 * ============================================================================
 * CLASE: Nodo<T>
 * ============================================================================
 * (a) Que hace: Representa un eslabon basico de una lista enlazada simple o
 *     circular. Guarda un dato cualquiera (Lote, Pedido, Venta o Mensajero)
 *     y una flecha (referencia) hacia el siguiente nodo de la cadena.
 * (b) Tema de Estructuras de Datos: Unidad 3 - Referencias, variables
 *     dinamicas (3.2) y estructuras enlazadas genericas (3.3).
 * (c) Complejidad Big-O principal: O(1) para crear un nodo y enlazar su
 *     referencia al siguiente elemento.
 * ============================================================================
 */
public class Nodo<T> {

    // === 1. ATRIBUTOS ===
    public T dato;
    public Nodo<T> sig;

    // === 2. CONSTRUCTOR ===
    // Recibe el dato que guardara este nodo y deja su puntero siguiente en null.
    public Nodo(T dato) {
        this.dato = dato;
        this.sig = null;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Representacion en texto ---
    // No recibe parametros; devuelve el texto del dato guardado para depuracion.
    @Override
    public String toString() {
        if (dato == null) {
            return "null";
        }
        return dato.toString();
    }

    // === 4. GETTERS Y SETTERS ===

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public Nodo<T> getSig() {
        return sig;
    }

    public void setSig(Nodo<T> sig) {
        this.sig = sig;
    }
}
