package algoritmos;

import modelo.Producto;

/*
 * ============================================================================
 * CLASE: Ordenamientos
 * ============================================================================
 * (a) Que hace: Ordena arreglos de productos de mayor a menor ganancia por
 *     kilo (precioVenta - precioCompra) usando los cuatro algoritmos exigidos
 *     en el curso: Burbuja estandar, Burbuja mejorada (con bandera de parada),
 *     Quick Sort y Merge Sort. Siempre trabaja sobre una copia propia para no
 *     desordenar el catalogo principal que se mantiene ordenado por codigo.
 * (b) Tema de Estructuras de Datos: Unidad 2 - Metodos de ordenamiento basicos
 *     (Burbuja y Burbuja mejorada, tema 2.2) y avanzados Divide y Venceras
 *     (Quick Sort y Merge Sort, tema 2.3), sin usar Arrays.sort ni Collections.sort.
 * (c) Complejidad Big-O principal:
 *     - burbuja: O(n^2) en todos los casos.
 *     - burbujaMejorada: O(n) en mejor caso (ya ordenado) y O(n^2) en peor caso.
 *     - quickSort: O(n log2 n) en caso promedio.
 *     - mergeSort: O(n log2 n) garantizado en todos los casos.
 * ============================================================================
 */
public class Ordenamientos {

    // Clase auxiliar para devolver el arreglo ordenado y sus estadisticas reales
    public static class ResultadoOrdenamiento {
        public final String algoritmo;
        public final Producto[] productosOrdenados;
        public final long comparaciones;
        public final long intercambios;
        public final long tiempoNanos;

        public ResultadoOrdenamiento(String algoritmo, Producto[] productosOrdenados,
                                     long comparaciones, long intercambios, long tiempoNanos) {
            this.algoritmo = algoritmo;
            this.productosOrdenados = productosOrdenados;
            this.comparaciones = comparaciones;
            this.intercambios = intercambios;
            this.tiempoNanos = tiempoNanos;
        }
    }

    // Contador interno reutilizable para Quick Sort y Merge Sort recursivos
    private static class Contadores {
        long comparaciones = 0L;
        long intercambios = 0L;
    }

    // === 1. ATRIBUTOS ===
    // No requiere estado permanente porque cada llamada recibe su propio arreglo.

    // === 2. CONSTRUCTOR ===
    public Ordenamientos() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Copia manual de arreglo propio ---
    // Recibe el arreglo original y la cantidad valida; devuelve una copia independiente.
    public static Producto[] copiarArreglo(Producto[] origen, int total) {
        Producto[] copia = new Producto[total];
        for (int i = 0; i < total; i++) {
            copia[i] = origen[i];
        }
        return copia;
    }

    // --- 2. Ordenamiento Burbuja estandar (descendente por ganancia) ---
    // Recibe arreglo y cantidad; compara pares vecinos en todas las pasadas sin parar antes.
    public static ResultadoOrdenamiento burbuja(Producto[] origen, int total) {
        Producto[] arreglo = copiarArreglo(origen, total);
        long comparaciones = 0L;
        long intercambios = 0L;
        long inicio = System.nanoTime();

        // Paso 1: Ejecutar n - 1 pasadas completas sobre el arreglo
        for (int i = 0; i < total - 1; i++) {
            // Paso 2: En cada pasada empujar la menor ganancia hacia el final
            for (int j = 0; j < total - 1 - i; j++) {
                comparaciones = comparaciones + 1;
                if (arreglo[j].calcularGananciaPorKg() < arreglo[j + 1].calcularGananciaPorKg()) {
                    Producto temporal = arreglo[j];
                    arreglo[j] = arreglo[j + 1];
                    arreglo[j + 1] = temporal;
                    intercambios = intercambios + 1;
                }
            }
        }

        long fin = System.nanoTime();
        return new ResultadoOrdenamiento("Burbuja estandar", arreglo,
                comparaciones, intercambios, fin - inicio);
    }

    // --- 3. Ordenamiento Burbuja mejorada (con bandera huboCambio) ---
    // Recibe arreglo y cantidad; si en una pasada no ocurre ningun intercambio, se detiene en O(n).
    public static ResultadoOrdenamiento burbujaMejorada(Producto[] origen, int total) {
        Producto[] arreglo = copiarArreglo(origen, total);
        long comparaciones = 0L;
        long intercambios = 0L;
        long inicio = System.nanoTime();

        for (int i = 0; i < total - 1; i++) {
            // Bandera que indica si hubo al menos un intercambio en esta pasada
            boolean huboCambio = false;

            for (int j = 0; j < total - 1 - i; j++) {
                comparaciones = comparaciones + 1;
                if (arreglo[j].calcularGananciaPorKg() < arreglo[j + 1].calcularGananciaPorKg()) {
                    Producto temporal = arreglo[j];
                    arreglo[j] = arreglo[j + 1];
                    arreglo[j + 1] = temporal;
                    intercambios = intercambios + 1;
                    huboCambio = true;
                }
            }

            // Si no hubo intercambios, el arreglo ya quedo ordenado y terminamos de inmediato
            if (!huboCambio) {
                break;
            }
        }

        long fin = System.nanoTime();
        return new ResultadoOrdenamiento("Burbuja mejorada", arreglo,
                comparaciones, intercambios, fin - inicio);
    }

    // --- 4. Ordenamiento Quick Sort (Divide y Venceras) ---
    // Recibe arreglo y cantidad; elige pivote central, particiona mayores a la izquierda y ordena recursivamente.
    public static ResultadoOrdenamiento quickSort(Producto[] origen, int total) {
        Producto[] arreglo = copiarArreglo(origen, total);
        Contadores cont = new Contadores();
        long inicio = System.nanoTime();

        if (total > 1) {
            quickSortRecursivo(arreglo, 0, total - 1, cont);
        }

        long fin = System.nanoTime();
        return new ResultadoOrdenamiento("Quick Sort", arreglo,
                cont.comparaciones, cont.intercambios, fin - inicio);
    }

    // Metodo recursivo de Quick Sort sobre el subarreglo [izq .. der] en orden descendente.
    private static void quickSortRecursivo(Producto[] arreglo, int izq, int der, Contadores cont) {
        int i = izq;
        int j = der;
        // Paso 1: Elegir el elemento central como pivote para evitar peor caso en datos ordenados
        long gananciaPivote = arreglo[izq + (der - izq) / 2].calcularGananciaPorKg();

        // Paso 2: Mover mayores al pivote hacia la izquierda y menores hacia la derecha
        while (i <= j) {
            while (arreglo[i].calcularGananciaPorKg() > gananciaPivote) {
                cont.comparaciones = cont.comparaciones + 1;
                i = i + 1;
            }
            cont.comparaciones = cont.comparaciones + 1;

            while (arreglo[j].calcularGananciaPorKg() < gananciaPivote) {
                cont.comparaciones = cont.comparaciones + 1;
                j = j - 1;
            }
            cont.comparaciones = cont.comparaciones + 1;

            if (i <= j) {
                if (i != j) {
                    Producto temporal = arreglo[i];
                    arreglo[i] = arreglo[j];
                    arreglo[j] = temporal;
                    cont.intercambios = cont.intercambios + 1;
                }
                i = i + 1;
                j = j - 1;
            }
        }

        // Paso 3: Ordenar recursivamente cada mitad
        if (izq < j) {
            quickSortRecursivo(arreglo, izq, j, cont);
        }
        if (i < der) {
            quickSortRecursivo(arreglo, i, der, cont);
        }
    }

    // --- 5. Ordenamiento Merge Sort (Divide y Venceras estable) ---
    // Recibe arreglo y cantidad; divide por la mitad hasta tamaño 1 y luego mezcla en orden descendente.
    public static ResultadoOrdenamiento mergeSort(Producto[] origen, int total) {
        Producto[] arreglo = copiarArreglo(origen, total);
        Producto[] auxiliar = new Producto[total];
        Contadores cont = new Contadores();
        long inicio = System.nanoTime();

        if (total > 1) {
            mergeSortRecursivo(arreglo, auxiliar, 0, total - 1, cont);
        }

        long fin = System.nanoTime();
        return new ResultadoOrdenamiento("Merge Sort", arreglo,
                cont.comparaciones, cont.intercambios, fin - inicio);
    }

    // Divide recursivamente el subarreglo [inicio .. fin] y luego fusiona ambas mitades ordenadas.
    private static void mergeSortRecursivo(Producto[] arreglo, Producto[] auxiliar,
                                           int inicio, int fin, Contadores cont) {
        // Caso base: un subarreglo de 1 elemento ya esta ordenado
        if (inicio >= fin) {
            return;
        }

        int mitad = inicio + (fin - inicio) / 2;

        // Paso 1: Ordenar la mitad izquierda y la mitad derecha
        mergeSortRecursivo(arreglo, auxiliar, inicio, mitad, cont);
        mergeSortRecursivo(arreglo, auxiliar, mitad + 1, fin, cont);

        // Paso 2: Fusionar las dos mitades ya ordenadas
        mezclar(arreglo, auxiliar, inicio, mitad, fin, cont);
    }

    // Mezcla dos mitades contiguas [inicio..mitad] y [mitad+1..fin] en orden descendente de ganancia.
    private static void mezclar(Producto[] arreglo, Producto[] auxiliar,
                                int inicio, int mitad, int fin, Contadores cont) {
        int i = inicio;
        int j = mitad + 1;
        int k = inicio;

        // Comparar elementos de ambas mitades y copiar primero el de mayor ganancia
        while (i <= mitad && j <= fin) {
            cont.comparaciones = cont.comparaciones + 1;
            if (arreglo[i].calcularGananciaPorKg() >= arreglo[j].calcularGananciaPorKg()) {
                auxiliar[k] = arreglo[i];
                i = i + 1;
            } else {
                auxiliar[k] = arreglo[j];
                j = j + 1;
            }
            cont.intercambios = cont.intercambios + 1;
            k = k + 1;
        }

        // Copiar los elementos sobrantes de la mitad izquierda (si quedaron)
        while (i <= mitad) {
            auxiliar[k] = arreglo[i];
            i = i + 1;
            k = k + 1;
        }

        // Copiar los elementos sobrantes de la mitad derecha (si quedaron)
        while (j <= fin) {
            auxiliar[k] = arreglo[j];
            j = j + 1;
            k = k + 1;
        }

        // Devolver el resultado mezclado al arreglo principal
        for (int p = inicio; p <= fin; p++) {
            arreglo[p] = auxiliar[p];
        }
    }

    // --- 6. Verificador auxiliar de orden descendente ---
    // Recibe un arreglo de productos y su tamaño; devuelve true si esta ordenado de mayor a menor ganancia.
    public static boolean estaOrdenadoDescendente(Producto[] arreglo, int total) {
        for (int i = 0; i < total - 1; i++) {
            if (arreglo[i].calcularGananciaPorKg() < arreglo[i + 1].calcularGananciaPorKg()) {
                return false;
            }
        }
        return true;
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica: todos los resultados se devuelven en ResultadoOrdenamiento.
}
