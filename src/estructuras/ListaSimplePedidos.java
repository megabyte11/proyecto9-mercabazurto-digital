package estructuras;

import modelo.Pedido;

/*
 * ============================================================================
 * CLASE: ListaSimplePedidos
 * ============================================================================
 * (a) Que hace: Organiza los pedidos recibidos por telefono o WhatsApp en
 *     estricto orden de llegada (el primero que pide es el primero que se
 *     despacha). Mantiene dos punteros propios: 'primero' (salida) y 'ultimo'
 *     (entrada), logrando registrar y despachar en un solo paso.
 * (b) Tema de Estructuras de Datos: Unidad 3 - Lista enlazada simple con
 *     referencias primero y ultimo (tema 3.3), sin usar java.util.Queue.
 * (c) Complejidad Big-O principal:
 *     - registrarPedido: O(1) gracias al puntero 'ultimo'.
 *     - despacharSiguiente: O(1) gracias al puntero 'primero'.
 *     - cancelarPorCodigo: O(n) en el peor caso (debe buscar el codigo).
 * ============================================================================
 */
public class ListaSimplePedidos {

    // === 1. ATRIBUTOS ===
    private Nodo<Pedido> primero;
    private Nodo<Pedido> ultimo;
    private int tamanio;

    // === 2. CONSTRUCTOR ===
    // No recibe parametros; inicializa la lista de pedidos vacia.
    public ListaSimplePedidos() {
        this.primero = null;
        this.ultimo = null;
        this.tamanio = 0;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Registrar pedido al final en O(1) ---
    // Recibe un Pedido nuevo; lo enlaza al final de la lista actualizando 'ultimo'.
    public void registrarPedido(Pedido pedido) {
        // Paso 1: Crear el nodo nuevo para el pedido
        Nodo<Pedido> nuevo = new Nodo<>(pedido);

        // Paso 2: Si la lista esta vacia, tanto 'primero' como 'ultimo' apuntan al nuevo nodo
        if (primero == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            // Paso 3: Si ya hay pedidos, conectamos el ultimo actual con el nuevo
            // y movemos la referencia 'ultimo' al nodo recien ingresado
            ultimo.sig = nuevo;
            ultimo = nuevo;
        }

        tamanio = tamanio + 1;
    }

    // --- 2. Despachar el pedido mas antiguo (inicio) en O(1) ---
    // No recibe parametros; extrae y devuelve el primer pedido en la fila (o null si esta vacia).
    public Pedido despacharSiguiente() {
        // Paso 1: Si no hay pedidos pendientes, devolver null
        if (primero == null) {
            return null;
        }

        // Paso 2: Guardar el dato del primer nodo para devolverlo
        Pedido pedidoAtendido = primero.dato;

        // Paso 3: Avanzar el puntero 'primero' al siguiente nodo
        primero = primero.sig;

        // Paso 4: Si la lista quedo vacia, actualizar tambien 'ultimo' a null
        if (primero == null) {
            ultimo = null;
        }

        tamanio = tamanio - 1;
        return pedidoAtendido;
    }

    // --- 3. Cancelar un pedido buscandolo por codigo ---
    // Recibe el codigo del pedido a cancelar; lo quita de la lista y lo devuelve (o null si no existe).
    public Pedido cancelarPorCodigo(String codigoPedido) {
        if (primero == null || codigoPedido == null) {
            return null;
        }

        // Caso A: El pedido a cancelar es el primero de la lista
        if (primero.dato.getCodigoPedido().equalsIgnoreCase(codigoPedido)) {
            Pedido cancelado = despacharSiguiente();
            cancelado.marcarCancelado();
            return cancelado;
        }

        // Caso B: Buscar el nodo anterior al pedido que queremos retirar
        Nodo<Pedido> anterior = primero;
        while (anterior.sig != null
                && !anterior.sig.dato.getCodigoPedido().equalsIgnoreCase(codigoPedido)) {
            anterior = anterior.sig;
        }

        // Si llegamos al final sin encontrarlo, devolvemos null
        if (anterior.sig == null) {
            return null;
        }

        // Paso final: Desconectar el nodo encontrado y ajustar 'ultimo' si era el ultimo
        Nodo<Pedido> nodoAEliminar = anterior.sig;
        Pedido pedidoCancelado = nodoAEliminar.dato;
        anterior.sig = nodoAEliminar.sig;

        if (nodoAEliminar == ultimo) {
            ultimo = anterior;
        }

        tamanio = tamanio - 1;
        pedidoCancelado.marcarCancelado();
        return pedidoCancelado;
    }

    // --- 4. Consultas auxiliares ---
    // No recibe parametros; muestra cual es el proximo pedido por salir sin retirarlo.
    public Pedido verPrimero() {
        if (primero == null) {
            return null;
        }
        return primero.dato;
    }

    // No recibe parametros; devuelve true si no hay pedidos pendientes.
    public boolean estaVacia() {
        return primero == null;
    }

    // Recibe un limite maximo; devuelve un arreglo propio con los primeros pedidos pendientes.
    public Pedido[] aArreglo(int limite) {
        int cantidad = tamanio;
        if (limite > 0 && limite < cantidad) {
            cantidad = limite;
        }
        Pedido[] resultado = new Pedido[cantidad];
        Nodo<Pedido> actual = primero;
        int indice = 0;
        while (actual != null && indice < cantidad) {
            resultado[indice] = actual.dato;
            actual = actual.sig;
            indice = indice + 1;
        }
        return resultado;
    }

    // No recibe parametros; devuelve todos los pedidos pendientes en un arreglo propio.
    public Pedido[] aArreglo() {
        return aArreglo(tamanio);
    }

    // === 4. GETTERS Y SETTERS ===

    public Nodo<Pedido> getPrimero() {
        return primero;
    }

    public Nodo<Pedido> getUltimo() {
        return ultimo;
    }

    public int getTamanio() {
        return tamanio;
    }
}
