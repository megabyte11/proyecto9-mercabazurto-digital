package algoritmos;

/*
 * ============================================================================
 * CLASE: ConjuntosArreglos
 * ============================================================================
 * (a) Que hace: Calcula operaciones de conjuntos sobre arreglos propios de
 *     clientes (sin usar HashSet ni ArrayList):
 *     - Interseccion (S1 n S2): clientes frecuentes que compraron en ambas semanas.
 *     - Union (S1 u S2): total de clientes unicos atendidos en la quincena.
 * (b) Tema de Estructuras de Datos: Unidad 1 - Operaciones con arreglos:
 *     union e interseccion de conjuntos (tema 1.3).
 * (c) Complejidad Big-O principal: O(|S1| * |S2|) al verificar pertenencia
 *     manualmente sobre arreglos propios.
 * ============================================================================
 */
public class ConjuntosArreglos {

    // === 1. ATRIBUTOS ===
    // Clase de operaciones puras sobre arreglos.

    // === 2. CONSTRUCTOR ===
    public ConjuntosArreglos() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Verificar si un elemento ya existe en un arreglo ---
    // Recibe un arreglo, la cantidad de posiciones ocupadas y el texto a buscar;
    // devuelve true si el valor ya esta guardado (ignorando mayusculas/minusculas).
    public static boolean contiene(String[] arreglo, int cantidad, String valor) {
        if (arreglo == null || valor == null) {
            return false;
        }
        for (int i = 0; i < cantidad; i++) {
            if (arreglo[i] != null && arreglo[i].equalsIgnoreCase(valor)) {
                return true;
            }
        }
        return false;
    }

    // --- 2. Interseccion de dos arreglos (S1 n S2 = Clientes frecuentes) ---
    // Recibe los arreglos de Semana 1 y Semana 2 con sus tamaños;
    // devuelve un arreglo exacto con los clientes que aparecen en ambas semanas sin repetir.
    public static String[] interseccion(String[] semana1, int tam1, String[] semana2, int tam2) {
        // Paso 1: Crear un arreglo temporal del tamaño maximo posible
        int maxTam = tam1 < tam2 ? tam1 : tam2;
        if (maxTam < 0) {
            maxTam = 0;
        }
        String[] temporal = new String[maxTam];
        int encontrados = 0;

        // Paso 2: Recorrer cada cliente de la Semana 1
        for (int i = 0; i < tam1; i++) {
            String cliente = semana1[i];
            if (cliente == null) {
                continue;
            }
            // Paso 3: Si tambien compro en Semana 2 y aun no fue agregado, incluirlo
            if (contiene(semana2, tam2, cliente) && !contiene(temporal, encontrados, cliente)) {
                temporal[encontrados] = cliente;
                encontrados = encontrados + 1;
            }
        }

        // Paso 4: Devolver un arreglo recortado al tamaño exacto de clientes encontrados
        return recortarArreglo(temporal, encontrados);
    }

    // --- 3. Union de dos arreglos sin duplicados (S1 u S2 = Clientes unicos) ---
    // Recibe los arreglos de Semana 1 y Semana 2 con sus tamaños;
    // devuelve un arreglo exacto con todos los clientes distintos atendidos entre las dos semanas.
    public static String[] union(String[] semana1, int tam1, String[] semana2, int tam2) {
        int maxTam = tam1 + tam2;
        if (maxTam < 0) {
            maxTam = 0;
        }
        String[] temporal = new String[maxTam];
        int totalUnicos = 0;

        // Paso 1: Copiar los clientes de la Semana 1 evitando duplicados internos
        for (int i = 0; i < tam1; i++) {
            String cliente = semana1[i];
            if (cliente != null && !contiene(temporal, totalUnicos, cliente)) {
                temporal[totalUnicos] = cliente;
                totalUnicos = totalUnicos + 1;
            }
        }

        // Paso 2: Agregar de la Semana 2 solo los clientes que aun no esten en el resultado
        for (int j = 0; j < tam2; j++) {
            String cliente = semana2[j];
            if (cliente != null && !contiene(temporal, totalUnicos, cliente)) {
                temporal[totalUnicos] = cliente;
                totalUnicos = totalUnicos + 1;
            }
        }

        // Paso 3: Recortar al tamaño real encontrado
        return recortarArreglo(temporal, totalUnicos);
    }

    // --- 4. Recortar un arreglo temporal al tamaño exacto ---
    // Recibe un arreglo con posiciones vacias al final y la cantidad real; devuelve una copia exacta.
    private static String[] recortarArreglo(String[] origen, int cantidadReal) {
        String[] exacto = new String[cantidadReal];
        for (int i = 0; i < cantidadReal; i++) {
            exacto[i] = origen[i];
        }
        return exacto;
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica.
}
