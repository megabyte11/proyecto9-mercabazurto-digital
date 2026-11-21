package estructuras;

import modelo.Lote;

/*
 * ============================================================================
 * CLASE: ListaSimpleLotes
 * ============================================================================
 * (a) Que hace: Mantiene todos los lotes de un producto perecedero ordenados
 *     de menor a mayor fecha de vencimiento (regla FEFO: primero vence,
 *     primero sale). Permite ingresar lotes en su posicion exacta, vender
 *     descontando siempre desde el primero, retirar vencidos y calcular el
 *     valor total del inventario usando recursividad.
 * (b) Tema de Estructuras de Datos: Unidad 3 - Lista enlazada simple ordenada
 *     (tema 3.3) y Unidad 1 - Recursividad sobre nodos (tema 1.5).
 * (c) Complejidad Big-O principal:
 *     - insertarOrdenado: O(n) en el peor caso (recorre hasta encontrar fecha).
 *     - venderDesdePrimero: O(1) por cada lote que se agota en la cabeza.
 *     - retirarVencidos: O(1) por cada lote vencido retirado en la cabeza.
 *     - calcularValorRecursivo: O(n) porque visita cada nodo exactamente una vez.
 * ============================================================================
 */
public class ListaSimpleLotes {

    // Clase auxiliar sencilla para devolver kilos despachados, costo real y detalle
    public static class ResultadoVentaLotes {
        public final int kgDespachados;
        public final long costoCompraReal;
        public final String detalleLotes;

        public ResultadoVentaLotes(int kgDespachados, long costoCompraReal, String detalleLotes) {
            this.kgDespachados = kgDespachados;
            this.costoCompraReal = costoCompraReal;
            this.detalleLotes = detalleLotes;
        }
    }

    // === 1. ATRIBUTOS ===
    private Nodo<Lote> cabeza;
    private int tamanio;

    // === 2. CONSTRUCTOR ===
    // No recibe parametros; crea una lista vacia sin nodos.
    public ListaSimpleLotes() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Insercion ordenada por fecha de vencimiento (FEFO) ---
    // Recibe un Lote nuevo; lo ubica en la posicion que mantiene las fechas de menor a mayor.
    public void insertarOrdenado(Lote nuevoLote) {
        // Paso 1: Envolver el lote dentro de un nodo nuevo
        Nodo<Lote> nuevo = new Nodo<>(nuevoLote);

        // Paso 2: Si la lista esta vacia o el nuevo lote vence antes (o igual) que la cabeza,
        // el nuevo nodo pasa a ser la nueva cabeza en O(1)
        if (cabeza == null
                || nuevoLote.getFechaVencimiento().compareTo(cabeza.dato.getFechaVencimiento()) <= 0) {
            nuevo.sig = cabeza;
            cabeza = nuevo;
            tamanio = tamanio + 1;
            return;
        }

        // Paso 3: Avanzar mientras el siguiente nodo tenga una fecha menor que la del nuevo lote
        Nodo<Lote> actual = cabeza;
        while (actual.sig != null
                && actual.sig.dato.getFechaVencimiento().compareTo(nuevoLote.getFechaVencimiento()) < 0) {
            actual = actual.sig;
        }

        // Paso 4: Conectar el nuevo nodo entre 'actual' y 'actual.sig'
        nuevo.sig = actual.sig;
        actual.sig = nuevo;
        tamanio = tamanio + 1;
    }

    // --- 2. Venta simple desde el primer lote (FEFO) ---
    // Recibe los kilos solicitados; descuenta siempre desde la cabeza y devuelve los kilos despachados.
    public int venderDesdePrimero(int kgSolicitados) {
        ResultadoVentaLotes resultado = venderDesdePrimeroConDetalle(kgSolicitados);
        return resultado.kgDespachados;
    }

    // --- 3. Venta FEFO con detalle de lotes y costo historico real ---
    // Recibe los kilos solicitados; descuenta desde la cabeza sumando el costo real de cada lote usado.
    public ResultadoVentaLotes venderDesdePrimeroConDetalle(int kgSolicitados) {
        if (kgSolicitados <= 0 || cabeza == null) {
            return new ResultadoVentaLotes(0, 0L, "");
        }

        int kgRestantes = kgSolicitados;
        long costoCompraReal = 0L;
        StringBuilder detalle = new StringBuilder();

        // Paso 1: Mientras haya lotes en la cabeza y falten kilos por despachar
        while (cabeza != null && kgRestantes > 0) {
            Lote loteCabeza = cabeza.dato;
            int kgDisponiblesEnLote = loteCabeza.getCantidadKg();

            if (detalle.length() > 0) {
                detalle.append(", ");
            }

            // Paso 2: Si el lote de la cabeza tiene igual o menos kilos que los que faltan,
            // se consume completo y la cabeza avanza al siguiente nodo en O(1)
            if (kgDisponiblesEnLote <= kgRestantes) {
                kgRestantes = kgRestantes - kgDisponiblesEnLote;
                costoCompraReal = costoCompraReal + ((long) kgDisponiblesEnLote * loteCabeza.getPrecioCompraKg());
                detalle.append(loteCabeza.getCodigoLote()).append(" (").append(kgDisponiblesEnLote).append(" kg)");
                cabeza = cabeza.sig;
                tamanio = tamanio - 1;
            } else {
                // Paso 3: Si el lote tiene mas kilos de los pedidos, solo restamos la cantidad necesaria
                loteCabeza.setCantidadKg(kgDisponiblesEnLote - kgRestantes);
                costoCompraReal = costoCompraReal + ((long) kgRestantes * loteCabeza.getPrecioCompraKg());
                detalle.append(loteCabeza.getCodigoLote()).append(" (").append(kgRestantes).append(" kg)");
                kgRestantes = 0;
            }
        }

        int kgDespachados = kgSolicitados - kgRestantes;
        return new ResultadoVentaLotes(kgDespachados, costoCompraReal, detalle.toString());
    }

    // --- 4. Retiro de lotes vencidos desde la cabeza ---
    // Recibe la fecha actual en formato ISO "AAAA-MM-DD"; elimina los lotes vencidos y devuelve los kilos retirados.
    public int retirarVencidos(String fechaHoy) {
        int kgRetirados = 0;

        // Como la lista esta ordenada por vencimiento, todos los lotes vencidos
        // estan agrupados al inicio; basta desconectar la cabeza en O(1) cada vez.
        while (cabeza != null && cabeza.dato.estaVencido(fechaHoy)) {
            kgRetirados = kgRetirados + cabeza.dato.getCantidadKg();
            cabeza = cabeza.sig;
            tamanio = tamanio - 1;
        }

        return kgRetirados;
    }

    // --- 5. Calculo recursivo del valor del inventario ---
    // No recibe parametros; inicia la suma recursiva desde la cabeza de la lista.
    public long calcularValorRecursivo() {
        return calcularValorRecursivo(cabeza);
    }

    // Recibe el nodo actual; suma el valor de este lote mas el valor recursivo del resto de la lista.
    public long calcularValorRecursivo(Nodo<Lote> actual) {
        // Caso base: si llegamos al final de la cadena (null), el valor es 0
        if (actual == null) {
            return 0L;
        }

        // Caso recursivo: multiplicamos kilos por el costo historico de este lote
        // y le sumamos el resultado de llamar al metodo con el nodo siguiente
        long subtotalNodo = actual.dato.calcularValorLote();
        return subtotalNodo + calcularValorRecursivo(actual.sig);
    }

    // --- 6. Consultas de apoyo sobre los lotes ---
    // No recibe parametros; recorre la lista y suma todos los kilos disponibles.
    public int calcularKgTotales() {
        int totalKg = 0;
        Nodo<Lote> actual = cabeza;
        while (actual != null) {
            totalKg = totalKg + actual.dato.getCantidadKg();
            actual = actual.sig;
        }
        return totalKg;
    }

    // No recibe parametros; devuelve el lote que vence primero sin quitarlo (o null si esta vacia).
    public Lote obtenerPrimero() {
        if (cabeza == null) {
            return null;
        }
        return cabeza.dato;
    }

    // No recibe parametros; devuelve true si no existen lotes en la lista.
    public boolean estaVacia() {
        return cabeza == null;
    }

    // No recibe parametros; copia los lotes en un arreglo propio para mostrarlos sin usar colecciones.
    public Lote[] aArreglo() {
        Lote[] resultado = new Lote[tamanio];
        Nodo<Lote> actual = cabeza;
        int indice = 0;
        while (actual != null && indice < tamanio) {
            resultado[indice] = actual.dato;
            actual = actual.sig;
            indice = indice + 1;
        }
        return resultado;
    }

    // === 4. GETTERS Y SETTERS ===

    public Nodo<Lote> getCabeza() {
        return cabeza;
    }

    public int getTamanio() {
        return tamanio;
    }
}
