package algoritmos;

import modelo.Producto;

/*
 * ============================================================================
 * CLASE: Busquedas
 * ============================================================================
 * (a) Que hace: Implementa los tres algoritmos de busqueda por codigo sobre
 *     el arreglo de productos del catalogo: busqueda secuencial, busqueda
 *     secuencial ordenada (con parada temprana) y busqueda binaria (dividiendo
 *     el arreglo a la mitad en cada comparacion). Tambien registra la cantidad
 *     real de comparaciones y la traza paso a paso para fines educativos.
 * (b) Tema de Estructuras de Datos: Unidad 2 - Busqueda secuencial, secuencial
 *     ordenada y busqueda binaria sobre arreglos (tema 2.4).
 * (c) Complejidad Big-O principal:
 *     - busquedaSecuencial: O(n) (hasta 200 comparaciones para n = 200).
 *     - busquedaSecuencialOrdenada: O(n) peor caso, parada temprana si el codigo
 *       actual ya supera al buscado.
 *     - busquedaBinaria: O(log2 n) (maximo 8 comparaciones para n = 200).
 * ============================================================================
 */
public class Busquedas {

    // Clase auxiliar para devolver el resultado junto con comparaciones, tiempo y traza
    public static class ResultadoBusqueda {
        public final String algoritmo;
        public final int indice;
        public final Producto producto;
        public final int comparaciones;
        public final long tiempoNanos;
        public final String[] trazaPasos;
        public final int totalPasos;

        public ResultadoBusqueda(String algoritmo, int indice, Producto producto,
                                 int comparaciones, long tiempoNanos,
                                 String[] trazaPasos, int totalPasos) {
            this.algoritmo = algoritmo;
            this.indice = indice;
            this.producto = producto;
            this.comparaciones = comparaciones;
            this.tiempoNanos = tiempoNanos;
            this.trazaPasos = trazaPasos;
            this.totalPasos = totalPasos;
        }
    }

    // === 1. ATRIBUTOS ===
    private static final int MAX_PASOS_TRAZA = 32;

    // === 2. CONSTRUCTOR ===
    // Clase de utilidad con metodos estaticos; no requiere instanciar atributos.
    public Busquedas() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Busqueda secuencial clasica O(n) ---
    // Recibe el arreglo de productos, la cantidad valida y el codigo buscado;
    // revisa uno por uno desde la posicion 0 hasta el final y devuelve el resultado con comparaciones.
    public static ResultadoBusqueda busquedaSecuencial(Producto[] productos, int total, String codigoBuscado) {
        long inicio = System.nanoTime();
        int comparaciones = 0;
        int indiceEncontrado = -1;
        Producto encontrado = null;
        String[] traza = new String[MAX_PASOS_TRAZA];
        int totalPasos = 0;

        if (productos != null && codigoBuscado != null) {
            String objetivo = codigoBuscado.trim();
            // Paso 1: Recorrer indice por indice desde 0 hasta total - 1
            for (int i = 0; i < total; i++) {
                comparaciones = comparaciones + 1;
                if (productos[i].getCodigo().equalsIgnoreCase(objetivo)) {
                    indiceEncontrado = i;
                    encontrado = productos[i];
                    break;
                }
            }
        }

        long fin = System.nanoTime();
        traza[0] = "Recorrido lineal de indice 0 hasta " + (indiceEncontrado >= 0 ? indiceEncontrado : (total - 1))
                + " (" + comparaciones + " comparaciones).";
        totalPasos = 1;

        return new ResultadoBusqueda("Busqueda Secuencial", indiceEncontrado, encontrado,
                comparaciones, fin - inicio, traza, totalPasos);
    }

    // --- 2. Busqueda secuencial ordenada O(n) con parada temprana ---
    // Recibe el arreglo ordenado ascendentemente por codigo, su tamaño y el codigo buscado;
    // se detiene apenas encuentra un codigo mayor al objetivo, ahorrando pasos si no existe.
    public static ResultadoBusqueda busquedaSecuencialOrdenada(Producto[] productos, int total, String codigoBuscado) {
        long inicio = System.nanoTime();
        int comparaciones = 0;
        int indiceEncontrado = -1;
        Producto encontrado = null;
        String[] traza = new String[MAX_PASOS_TRAZA];
        int totalPasos = 0;
        boolean paradaTemprana = false;

        if (productos != null && codigoBuscado != null) {
            String objetivo = codigoBuscado.trim();
            for (int i = 0; i < total; i++) {
                comparaciones = comparaciones + 1;
                int cmp = productos[i].getCodigo().compareToIgnoreCase(objetivo);

                if (cmp == 0) {
                    indiceEncontrado = i;
                    encontrado = productos[i];
                    break;
                }

                // Como el arreglo esta ordenado, si ya pasamos el codigo buscado no hace falta seguir
                if (cmp > 0) {
                    paradaTemprana = true;
                    break;
                }
            }
        }

        long fin = System.nanoTime();
        if (paradaTemprana) {
            traza[0] = "Parada temprana en comparacion " + comparaciones
                    + ": se hallo un codigo mayor y se confirmo que no existe.";
        } else {
            traza[0] = "Recorrido ordenado finalizado en " + comparaciones + " comparaciones.";
        }
        totalPasos = 1;

        return new ResultadoBusqueda("Busqueda Secuencial Ordenada", indiceEncontrado, encontrado,
                comparaciones, fin - inicio, traza, totalPasos);
    }

    // --- 3. Busqueda binaria O(log2 n) ---
    // Recibe el arreglo ordenado por codigo, la cantidad de elementos y el codigo buscado;
    // parte el rango a la mitad en cada paso hasta hallar el producto o agotar el intervalo.
    public static ResultadoBusqueda busquedaBinaria(Producto[] productos, int total, String codigoBuscado) {
        long inicio = System.nanoTime();
        int comparaciones = 0;
        int indiceEncontrado = -1;
        Producto encontrado = null;
        String[] traza = new String[MAX_PASOS_TRAZA];
        int totalPasos = 0;

        if (productos != null && codigoBuscado != null && total > 0) {
            String objetivo = codigoBuscado.trim();
            int izquierda = 0;
            int derecha = total - 1;

            // Paso 1: Mientras el extremo izquierdo no supere al derecho
            while (izquierda <= derecha) {
                // Paso 2: Calcular la posicion central evitando desbordamiento
                int medio = izquierda + (derecha - izquierda) / 2;
                comparaciones = comparaciones + 1;

                String codigoMedio = productos[medio].getCodigo();
                int cmp = codigoMedio.compareToIgnoreCase(objetivo);

                // Guardar la explicacion de este paso en la traza
                if (cmp == 0) {
                    if (totalPasos < MAX_PASOS_TRAZA) {
                        traza[totalPasos] = "Comp. " + comparaciones + ": izq=" + izquierda
                                + ", der=" + derecha + " -> medio=" + medio
                                + " (" + codigoMedio + " == " + objetivo + " -> Encontrado)";
                        totalPasos = totalPasos + 1;
                    }
                    indiceEncontrado = medio;
                    encontrado = productos[medio];
                    break;
                } else if (cmp < 0) {
                    // Paso 3A: El codigo del medio es menor; descartamos toda la mitad izquierda
                    if (totalPasos < MAX_PASOS_TRAZA) {
                        traza[totalPasos] = "Comp. " + comparaciones + ": izq=" + izquierda
                                + ", der=" + derecha + " -> medio=" + medio
                                + " (" + codigoMedio + " < " + objetivo + ", busca en mitad derecha)";
                        totalPasos = totalPasos + 1;
                    }
                    izquierda = medio + 1;
                } else {
                    // Paso 3B: El codigo del medio es mayor; descartamos toda la mitad derecha
                    if (totalPasos < MAX_PASOS_TRAZA) {
                        traza[totalPasos] = "Comp. " + comparaciones + ": izq=" + izquierda
                                + ", der=" + derecha + " -> medio=" + medio
                                + " (" + codigoMedio + " > " + objetivo + ", busca en mitad izquierda)";
                        totalPasos = totalPasos + 1;
                    }
                    derecha = medio - 1;
                }
            }
        }

        long fin = System.nanoTime();
        return new ResultadoBusqueda("Busqueda Binaria", indiceEncontrado, encontrado,
                comparaciones, fin - inicio, traza, totalPasos);
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica: los resultados se devuelven en la estructura ResultadoBusqueda.
}
