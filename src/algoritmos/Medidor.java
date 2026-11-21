package algoritmos;

import java.util.Random;
import estructuras.ListaCircular;
import estructuras.ListaSimpleLotes;
import estructuras.ListaSimplePedidos;
import modelo.Lote;
import modelo.Mensajero;
import modelo.Pedido;
import modelo.Producto;

/*
 * ============================================================================
 * CLASE: Medidor
 * ============================================================================
 * (a) Que hace: Mide con reloj real de alta precision (System.nanoTime()) el
 *     tiempo de ejecucion de todas las estructuras y algoritmos del sistema
 *     para los tres tamaños oficiales exigidos en la guia: n = 200, n = 2.000
 *     y n = 5.000, contrastando la teoria Big-O con resultados experimentales.
 * (b) Tema de Estructuras de Datos: Unidad 1 - Complejidad algoritmica Big-O
 *     (tema 1.4), Unidad 2 (busquedas y ordenamientos) y Unidad 3 (listas).
 * (c) Complejidad Big-O principal: Ejecuta pruebas de O(1), O(log2 n), O(n),
 *     O(n log2 n) y O(n^2) para validar empiricamente su curva de crecimiento.
 * ============================================================================
 */
public class Medidor {

    // Clase auxiliar para representar una fila de la tabla comparativa Big-O
    public static class FilaMedicion {
        public final String operacion;
        public final String bigO;
        public final String formula;
        public final long ops200;
        public final long ops2000;
        public final long ops5000;
        public final double ms200;
        public final double ms2000;
        public final double ms5000;

        public FilaMedicion(String operacion, String bigO, String formula,
                            long ops200, long ops2000, long ops5000,
                            double ms200, double ms2000, double ms5000) {
            this.operacion = operacion;
            this.bigO = bigO;
            this.formula = formula;
            this.ops200 = ops200;
            this.ops2000 = ops2000;
            this.ops5000 = ops5000;
            this.ms200 = ms200;
            this.ms2000 = ms2000;
            this.ms5000 = ms5000;
        }
    }

    // === 1. ATRIBUTOS ===
    public static final int[] TAMANIOS = {200, 2000, 5000};

    // === 2. CONSTRUCTOR ===
    public Medidor() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Ejecutar bateria completa de mediciones Big-O ---
    // No recibe parametros; mide las 9 operaciones clave en n = 200, 2.000 y 5.000 y devuelve la tabla.
    public static FilaMedicion[] ejecutarMedicionCompleta() {
        // Paso 1: Calentamiento completo de la JVM (JIT) para que n = 200 no cargue
        // el tiempo de compilacion inicial y todas las mediciones sean comparables
        Producto[] calCod = crearProductosOrdenadosPorCodigo(200);
        Producto[] calDes = crearProductosDesordenados(200);
        ListaSimpleLotes calLotes = crearListaLotesDeTamanio(200);
        for (int w = 0; w < 400; w++) {
            Busquedas.busquedaBinaria(calCod, 200, "P00200");
            Busquedas.busquedaSecuencial(calCod, 200, "P00200");
            calLotes.calcularValorRecursivo();
        }
        for (int w = 0; w < 30; w++) {
            Ordenamientos.mergeSort(calDes, 200);
            Ordenamientos.quickSort(calDes, 200);
            Ordenamientos.burbujaMejorada(calDes, 200);
            Ordenamientos.burbuja(calDes, 200);
        }
        medirOperacionO1(200);

        double[] o1 = new double[3];
        double[] binaria = new double[3];
        double[] secuencial = new double[3];
        double[] recursivo = new double[3];
        double[] insercionLote = new double[3];
        double[] burbujaMejorCaso = new double[3];
        double[] mergeSortT = new double[3];
        double[] quickSortT = new double[3];
        double[] burbujaPeorCaso = new double[3];

        // Paso 2: Medir para cada uno de los tres tamaños oficiales (200, 2.000 y 5.000)
        for (int t = 0; t < TAMANIOS.length; t++) {
            int n = TAMANIOS[t];

            Producto[] ordenadosPorCodigo = crearProductosOrdenadosPorCodigo(n);
            Producto[] desordenadosGanancia = crearProductosDesordenados(n);
            Producto[] ordenadosDescGanancia = Ordenamientos.mergeSort(desordenadosGanancia, n).productosOrdenados;
            Producto[] inversoGanancia = invertirArreglo(ordenadosDescGanancia, n);
            String ultimoCodigo = String.format("P%05d", n);

            // 1) Operaciones O(1): registrar pedido + despachar + turno Round Robin
            o1[t] = medirOperacionO1(n);

            // 2) Busqueda binaria (peor caso: ultimo codigo) - promedio de 200 repeticiones
            long sumaBin = 0L;
            int repsBusq = 200;
            for (int r = 0; r < repsBusq; r++) {
                sumaBin = sumaBin + Busquedas.busquedaBinaria(ordenadosPorCodigo, n, ultimoCodigo).tiempoNanos;
            }
            binaria[t] = (sumaBin / (double) repsBusq) / 1_000_000.0;

            // 3) Busqueda secuencial (peor caso: ultimo codigo) - promedio de 50 repeticiones
            long sumaSec = 0L;
            int repsSec = 50;
            for (int r = 0; r < repsSec; r++) {
                sumaSec = sumaSec + Busquedas.busquedaSecuencial(ordenadosPorCodigo, n, ultimoCodigo).tiempoNanos;
            }
            secuencial[t] = (sumaSec / (double) repsSec) / 1_000_000.0;

            // 4) Valor recursivo sobre lista de n lotes
            ListaSimpleLotes listaLotes = crearListaLotesDeTamanio(n);
            long sumaRec = 0L;
            int repsRec = 20;
            for (int r = 0; r < repsRec; r++) {
                long ini = System.nanoTime();
                listaLotes.calcularValorRecursivo();
                long fin = System.nanoTime();
                sumaRec = sumaRec + (fin - ini);
            }
            recursivo[t] = (sumaRec / (double) repsRec) / 1_000_000.0;

            // 5) Insercion ordenada de 1 lote al final de una lista de tamaño n (peor caso)
            long sumaIns = 0L;
            int repsIns = 20;
            for (int r = 0; r < repsIns; r++) {
                Lote loteTardio = new Lote("L-Z", "P001", "2099-12-31", 10, 3200L);
                long ini = System.nanoTime();
                listaLotes.insertarOrdenado(loteTardio);
                long fin = System.nanoTime();
                sumaIns = sumaIns + (fin - ini);
            }
            insercionLote[t] = (sumaIns / (double) repsIns) / 1_000_000.0;

            // 6) Burbuja mejorada en mejor caso (arreglo ya ordenado descendentemente)
            long sumaBMej = 0L;
            int repsOrden = (n == 200) ? 15 : 5;
            for (int r = 0; r < repsOrden; r++) {
                sumaBMej = sumaBMej + Ordenamientos.burbujaMejorada(ordenadosDescGanancia, n).tiempoNanos;
            }
            burbujaMejorCaso[t] = (sumaBMej / (double) repsOrden) / 1_000_000.0;

            // 7) Merge Sort (caso promedio)
            long sumaMerge = 0L;
            for (int r = 0; r < repsOrden; r++) {
                sumaMerge = sumaMerge + Ordenamientos.mergeSort(desordenadosGanancia, n).tiempoNanos;
            }
            mergeSortT[t] = (sumaMerge / (double) repsOrden) / 1_000_000.0;

            // 8) Quick Sort (caso promedio)
            long sumaQuick = 0L;
            for (int r = 0; r < repsOrden; r++) {
                sumaQuick = sumaQuick + Ordenamientos.quickSort(desordenadosGanancia, n).tiempoNanos;
            }
            quickSortT[t] = (sumaQuick / (double) repsOrden) / 1_000_000.0;

            // 9) Burbuja estandar (peor caso: orden inverso)
            int repsCuad = (n <= 2000) ? 3 : 1;
            long sumaBurb = 0L;
            for (int r = 0; r < repsCuad; r++) {
                sumaBurb = sumaBurb + Ordenamientos.burbuja(inversoGanancia, n).tiempoNanos;
            }
            burbujaPeorCaso[t] = (sumaBurb / (double) repsCuad) / 1_000_000.0;
        }

        // Paso 3: Construir la tabla oficial de 9 filas
        FilaMedicion[] tabla = new FilaMedicion[9];
        tabla[0] = new FilaMedicion("Despachar lote / Pedido / Turno Round Robin", "O(1)", "1 cambio de puntero",
                1, 1, 1, o1[0], o1[1], o1[2]);
        tabla[1] = new FilaMedicion("Busqueda binaria por codigo (peor caso)", "O(log2 n)", "floor(log2(n)) + 1",
                8, 11, 13, binaria[0], binaria[1], binaria[2]);
        tabla[2] = new FilaMedicion("Busqueda secuencial por codigo (peor caso)", "O(n)", "n comparaciones",
                200, 2000, 5000, secuencial[0], secuencial[1], secuencial[2]);
        tabla[3] = new FilaMedicion("Valor recursivo del inventario", "O(n)", "n llamadas recursivas",
                200, 2000, 5000, recursivo[0], recursivo[1], recursivo[2]);
        tabla[4] = new FilaMedicion("Insercion ordenada de un lote (peor caso)", "O(n)", "n comparaciones de fecha",
                200, 2000, 5000, insercionLote[0], insercionLote[1], insercionLote[2]);
        tabla[5] = new FilaMedicion("Burbuja mejorada (mejor caso: ya ordenado)", "O(n)", "n - 1 comparaciones",
                199, 1999, 4999, burbujaMejorCaso[0], burbujaMejorCaso[1], burbujaMejorCaso[2]);
        tabla[6] = new FilaMedicion("Merge Sort (caso promedio)", "O(n log2 n)", "n * log2(n)",
                1529, 21932, 61439, mergeSortT[0], mergeSortT[1], mergeSortT[2]);
        tabla[7] = new FilaMedicion("Quick Sort (caso promedio)", "O(n log2 n)", "n * log2(n)",
                1529, 21932, 61439, quickSortT[0], quickSortT[1], quickSortT[2]);
        tabla[8] = new FilaMedicion("Burbuja estandar (peor caso)", "O(n^2)", "n * (n - 1) / 2",
                19900, 1999000, 12497500, burbujaPeorCaso[0], burbujaPeorCaso[1], burbujaPeorCaso[2]);

        return tabla;
    }

    // --- 2. Metodos auxiliares de generacion de datos para medicion ---
    // Mide el tiempo promedio en milisegundos de una operacion O(1) con n elementos ya cargados.
    private static double medirOperacionO1(int n) {
        ListaSimplePedidos pedidos = new ListaSimplePedidos();
        for (int i = 0; i < n; i++) {
            pedidos.registrarPedido(new Pedido("PED-" + i, "Cliente", "P001", 10, 1, "2026-11-20 05:00"));
        }
        ListaCircular<Mensajero> mensajeros = new ListaCircular<>();
        mensajeros.insertar(new Mensajero("M1", "Carlos", "300"));
        mensajeros.insertar(new Mensajero("M2", "Dairo", "301"));
        mensajeros.insertar(new Mensajero("M3", "Edgar", "302"));

        int repeticiones = 1000;
        long ini = System.nanoTime();
        for (int r = 0; r < repeticiones; r++) {
            pedidos.registrarPedido(new Pedido("PED-N", "Restaurante", "P001", 5, 1, "2026-11-20 06:00"));
            pedidos.despacharSiguiente();
            mensajeros.siguienteTurno();
        }
        long fin = System.nanoTime();
        // Dividimos por (repeticiones * 3 operaciones O(1)) para obtener el costo unitario en ms
        return ((fin - ini) / (double) (repeticiones * 3)) / 1_000_000.0;
    }

    // Crea un arreglo de n productos ordenados ascendentemente por codigo.
    private static Producto[] crearProductosOrdenadosPorCodigo(int n) {
        Producto[] arr = new Producto[n];
        for (int i = 0; i < n; i++) {
            String codigo = String.format("P%05d", i + 1);
            arr[i] = new Producto(codigo, "Producto " + (i + 1), "Fruta", 3000L, 4500L);
        }
        return arr;
    }

    // Crea un arreglo de n productos con ganancias aleatorias reproducibles.
    private static Producto[] crearProductosDesordenados(int n) {
        Random rnd = new Random(42L + n);
        Producto[] arr = new Producto[n];
        for (int i = 0; i < n; i++) {
            long compra = 2000L + rnd.nextInt(15000);
            long ganancia = 500L + rnd.nextInt(8000);
            arr[i] = new Producto(String.format("P%05d", i + 1), "Prod " + i, "Fruta",
                    compra, compra + ganancia);
        }
        return arr;
    }

    // Invierte un arreglo de productos para construir el peor caso de Burbuja.
    private static Producto[] invertirArreglo(Producto[] origen, int n) {
        Producto[] inv = new Producto[n];
        for (int i = 0; i < n; i++) {
            inv[i] = origen[n - 1 - i];
        }
        return inv;
    }

    // Construye una lista de n lotes ordenados para pruebas de recursividad e insercion.
    private static ListaSimpleLotes crearListaLotesDeTamanio(int n) {
        ListaSimpleLotes lista = new ListaSimpleLotes();
        // Insertamos fechas estrictamente decrecientes para que cada insercion entre en cabeza O(1)
        // y al medir insercionOrdenado en peor caso ("2099-12-31") recorra exactamente los n nodos.
        java.time.LocalDate fechaBase = java.time.LocalDate.of(2026, 1, 1);
        for (int i = n; i >= 1; i--) {
            String fecha = fechaBase.plusDays(i).toString();
            lista.insertarOrdenado(new Lote("L-" + i, "P001", fecha, 10, 3200L));
        }
        return lista;
    }

    // --- 3. Imprimir tabla en consola y punto de entrada directo ---
    // Recibe la tabla medida y la imprime formateada con tiempos reales en milisegundos.
    public static void imprimirTabla(FilaMedicion[] filas) {
        System.out.println("================================================================================================================");
        System.out.println("  TABLA DE COMPLEJIDAD BIG-O Y TIEMPOS REALES MEDIDOS (System.nanoTime()) - MERCABAZURTO DIGITAL");
        System.out.println("================================================================================================================");
        System.out.printf("%-44s | %-11s | %10s | %10s | %10s%n",
                "Operacion / Algoritmo", "Big-O", "n = 200", "n = 2.000", "n = 5.000");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < filas.length; i++) {
            FilaMedicion f = filas[i];
            System.out.printf("%-44s | %-11s | %7.4f ms | %7.4f ms | %7.4f ms%n",
                    f.operacion, f.bigO, f.ms200, f.ms2000, f.ms5000);
        }
        System.out.println("================================================================================================================");
    }

    // Permite ejecutar el medidor directamente desde terminal: java -cp bin algoritmos.Medidor
    public static void main(String[] args) {
        FilaMedicion[] filas = ejecutarMedicionCompleta();
        imprimirTabla(filas);
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica.
}
