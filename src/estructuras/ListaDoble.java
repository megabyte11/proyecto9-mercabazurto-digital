package estructuras;

/*
 * ============================================================================
 * CLASE: ListaDoble<T>
 * ============================================================================
 * (a) Que hace: Almacena los movimientos del dia en una cadena de nodos con
 *     doble flecha (anterior y siguiente). Permite revisar la bitacora tanto
 *     del primer movimiento al ultimo (adelante) como del mas reciente al mas
 *     antiguo (atras), ademas de insertar y eliminar actualizando ant y sig.
 * (b) Tema de Estructuras de Datos: Unidad 3 - Lista doblemente enlazada
 *     (tema 3.3, componente opcional de bonificacion +0,3).
 * (c) Complejidad Big-O principal:
 *     - insertarAlInicio / insertarAlFinal: O(1).
 *     - eliminarPrimero / eliminarUltimo: O(1).
 *     - recorrerAdelante / recorrerAtras / eliminarDato: O(n).
 * ============================================================================
 */
public class ListaDoble<T> {

    // === 1. ATRIBUTOS ===
    private NodoDoble<T> primero;
    private NodoDoble<T> ultimo;
    private int tamanio;

    // === 2. CONSTRUCTOR ===
    // No recibe parametros; crea una lista doblemente enlazada vacia.
    public ListaDoble() {
        this.primero = null;
        this.ultimo = null;
        this.tamanio = 0;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Insertar al final en O(1) ---
    // Recibe un dato nuevo; lo agrega al final enlazando correctamente ant y sig.
    public void insertarAlFinal(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);

        if (primero == null) {
            // Si la lista esta vacia, primero y ultimo apuntan al mismo nodo
            primero = nuevo;
            ultimo = nuevo;
        } else {
            // El nuevo nodo mira hacia atras al antiguo ultimo,
            // y el antiguo ultimo mira hacia adelante al nuevo nodo
            nuevo.ant = ultimo;
            ultimo.sig = nuevo;
            ultimo = nuevo;
        }

        tamanio = tamanio + 1;
    }

    // --- 2. Insertar al inicio en O(1) ---
    // Recibe un dato nuevo; lo coloca al principio actualizando ant y sig.
    public void insertarAlInicio(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);

        if (primero == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            nuevo.sig = primero;
            primero.ant = nuevo;
            primero = nuevo;
        }

        tamanio = tamanio + 1;
    }

    // --- 3. Eliminar el primer nodo en O(1) ---
    // No recibe parametros; retira el primer elemento y devuelve su dato (o null si esta vacia).
    public T eliminarPrimero() {
        if (primero == null) {
            return null;
        }

        T datoEliminado = primero.dato;
        primero = primero.sig;

        if (primero == null) {
            // Si quedo vacia, tambien limpiamos ultimo
            ultimo = null;
        } else {
            // El nuevo primero no tiene nodo anterior
            primero.ant = null;
        }

        tamanio = tamanio - 1;
        return datoEliminado;
    }

    // --- 4. Eliminar el ultimo nodo en O(1) ---
    // No recibe parametros; retira el ultimo elemento y devuelve su dato (o null si esta vacia).
    public T eliminarUltimo() {
        if (ultimo == null) {
            return null;
        }

        T datoEliminado = ultimo.dato;
        ultimo = ultimo.ant;

        if (ultimo == null) {
            primero = null;
        } else {
            // El nuevo ultimo no tiene nodo siguiente
            ultimo.sig = null;
        }

        tamanio = tamanio - 1;
        return datoEliminado;
    }

    // --- 5. Eliminar un dato especifico actualizando ant y sig ---
    // Recibe el dato buscado; si lo encuentra lo desconecta por ambos lados y devuelve true.
    public boolean eliminarDato(T dato) {
        if (primero == null || dato == null) {
            return false;
        }

        NodoDoble<T> actual = primero;
        while (actual != null) {
            if (actual.dato.equals(dato)) {
                // Paso 1: Si es el primero, reutilizamos eliminarPrimero
                if (actual == primero) {
                    eliminarPrimero();
                    return true;
                }
                // Paso 2: Si es el ultimo, reutilizamos eliminarUltimo
                if (actual == ultimo) {
                    eliminarUltimo();
                    return true;
                }
                // Paso 3: Si esta en el medio, puenteamos el anterior con el siguiente
                actual.ant.sig = actual.sig;
                actual.sig.ant = actual.ant;
                actual.ant = null;
                actual.sig = null;
                tamanio = tamanio - 1;
                return true;
            }
            actual = actual.sig;
        }

        return false;
    }

    // --- 6. Recorrer hacia adelante (primero -> ultimo) ---
    // Recibe un arreglo destino; lo llena siguiendo los punteros 'sig' desde el inicio.
    public int recorrerAdelante(T[] destino) {
        int contador = 0;
        NodoDoble<T> actual = primero;
        while (actual != null && contador < destino.length) {
            destino[contador] = actual.dato;
            actual = actual.sig;
            contador = contador + 1;
        }
        return contador;
    }

    // --- 7. Recorrer hacia atras (ultimo -> primero) ---
    // Recibe un arreglo destino; lo llena siguiendo los punteros 'ant' desde el final.
    public int recorrerAtras(T[] destino) {
        int contador = 0;
        NodoDoble<T> actual = ultimo;
        while (actual != null && contador < destino.length) {
            destino[contador] = actual.dato;
            actual = actual.ant;
            contador = contador + 1;
        }
        return contador;
    }

    // --- 8. Verificar si esta vacia ---
    // No recibe parametros; devuelve true si no hay nodos en la lista doble.
    public boolean estaVacia() {
        return primero == null;
    }

    // === 4. GETTERS Y SETTERS ===

    public NodoDoble<T> getPrimero() {
        return primero;
    }

    public NodoDoble<T> getUltimo() {
        return ultimo;
    }

    public int getTamanio() {
        return tamanio;
    }
}
