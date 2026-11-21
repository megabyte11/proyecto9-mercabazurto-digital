package estructuras;

/*
 * ============================================================================
 * CLASE: ListaCircular<T>
 * ============================================================================
 * (a) Que hace: Mantiene elementos enlazados en circulo (el ultimo apunta de
 *     vuelta al primero). Se usa para repartir los domicilios del puesto por
 *     turnos equitativos (Round Robin) entre Carlos, Dairo y Edgar sin peleas.
 * (b) Tema de Estructuras de Datos: Unidad 3 - Lista enlazada circular y
 *     algoritmo de turnos Round Robin (tema 3.3).
 * (c) Complejidad Big-O principal:
 *     - insertar: O(1) usando la referencia al nodo 'ultimo'.
 *     - siguienteTurno: O(1) avanzando un solo puntero circular.
 * ============================================================================
 */
public class ListaCircular<T> {

    // === 1. ATRIBUTOS ===
    private Nodo<T> ultimo; // ultimo.sig siempre apunta al primer elemento del ciclo
    private int tamanio;

    // === 2. CONSTRUCTOR ===
    // No recibe parametros; crea una lista circular vacia.
    public ListaCircular() {
        this.ultimo = null;
        this.tamanio = 0;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Insertar elemento en el circulo en O(1) ---
    // Recibe un dato generico (por ejemplo un Mensajero); lo agrega al ciclo manteniendo el enlace.
    public void insertar(T dato) {
        // Paso 1: Crear el nodo nuevo
        Nodo<T> nuevo = new Nodo<>(dato);

        // Paso 2: Si la lista esta vacia, el nodo se apunta a si mismo formando un ciclo de 1
        if (ultimo == null) {
            nuevo.sig = nuevo;
        } else {
            // Paso 3: El nuevo nodo apunta al primero (ultimo.sig) y el ultimo pasa a apuntar al nuevo
            nuevo.sig = ultimo.sig;
            ultimo.sig = nuevo;
        }

        // Paso 4: El nuevo nodo queda como el nuevo 'ultimo'
        ultimo = nuevo;
        tamanio = tamanio + 1;
    }

    // --- 2. Asignar el siguiente turno (Round Robin) en O(1) ---
    // No recibe parametros; avanza el puntero circular una posicion y devuelve el dato elegido.
    public T siguienteTurno() {
        if (ultimo == null) {
            return null;
        }

        // Paso 1: Avanzamos 'ultimo' al siguiente nodo del anillo (que era el primero en turno)
        ultimo = ultimo.sig;

        // Paso 2: Devolvemos ese elemento; en la proxima llamada le tocara al siguiente en el circulo
        return ultimo.dato;
    }

    // --- 3. Consultar quien sigue en el turno sin rotar ---
    // No recibe parametros; devuelve el proximo elemento que saldra sin alterar los punteros.
    public T verProximoTurno() {
        if (ultimo == null) {
            return null;
        }
        return ultimo.sig.dato;
    }

    // --- 4. Eliminar un elemento del circulo ---
    // Recibe el dato a retirar; actualiza los enlaces circulares y devuelve true si lo encontro.
    public boolean eliminar(T dato) {
        if (ultimo == null || dato == null) {
            return false;
        }

        Nodo<T> anterior = ultimo;
        Nodo<T> actual = ultimo.sig;

        for (int i = 0; i < tamanio; i++) {
            if (actual.dato.equals(dato)) {
                // Caso unico elemento: la lista queda vacia
                if (tamanio == 1) {
                    ultimo = null;
                } else {
                    anterior.sig = actual.sig;
                    if (actual == ultimo) {
                        ultimo = anterior;
                    }
                }
                tamanio = tamanio - 1;
                return true;
            }
            anterior = actual;
            actual = actual.sig;
        }

        return false;
    }

    // --- 5. Copiar los elementos en un arreglo propio ---
    // Recibe un arreglo destino del tamaño de la lista; lo llena en orden comenzando por el proximo turno.
    public void llenarArreglo(T[] destino) {
        if (ultimo == null || destino == null) {
            return;
        }
        Nodo<T> actual = ultimo.sig;
        int limite = tamanio;
        if (destino.length < limite) {
            limite = destino.length;
        }
        for (int i = 0; i < limite; i++) {
            destino[i] = actual.dato;
            actual = actual.sig;
        }
    }

    // --- 6. Verificar si esta vacia ---
    // No recibe parametros; devuelve true si no hay elementos en el circulo.
    public boolean estaVacia() {
        return ultimo == null;
    }

    // === 4. GETTERS Y SETTERS ===

    public Nodo<T> getUltimo() {
        return ultimo;
    }

    public int getTamanio() {
        return tamanio;
    }
}
