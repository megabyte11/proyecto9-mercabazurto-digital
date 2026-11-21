package estructuras;

/*
 * ============================================================================
 * CLASE: NodoDoble<T>
 * ============================================================================
 * (a) Que hace: Representa un eslabon de una lista doblemente enlazada.
 *     Guarda el dato y dos referencias: una hacia el nodo anterior (ant) y
 *     otra hacia el nodo siguiente (sig), permitiendo caminar hacia adelante
 *     y hacia atras.
 * (b) Tema de Estructuras de Datos: Unidad 3 - Referencias dinamicas (3.2) y
 *     lista doblemente enlazada (3.3 - componente de bonificacion).
 * (c) Complejidad Big-O principal: O(1) para crear el nodo y actualizar sus
 *     punteros ant y sig.
 * ============================================================================
 */
public class NodoDoble<T> {

    // === 1. ATRIBUTOS ===
    public T dato;
    public NodoDoble<T> ant;
    public NodoDoble<T> sig;

    // === 2. CONSTRUCTOR ===
    // Recibe el dato a almacenar e inicializa las referencias ant y sig en null.
    public NodoDoble(T dato) {
        this.dato = dato;
        this.ant = null;
        this.sig = null;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Representacion en texto ---
    // No recibe parametros; devuelve el texto del dato almacenado.
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

    public NodoDoble<T> getAnt() {
        return ant;
    }

    public void setAnt(NodoDoble<T> ant) {
        this.ant = ant;
    }

    public NodoDoble<T> getSig() {
        return sig;
    }

    public void setSig(NodoDoble<T> sig) {
        this.sig = sig;
    }
}
