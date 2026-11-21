package app;

import algoritmos.Busquedas;
import algoritmos.Medidor;
import algoritmos.Ordenamientos;
import algoritmos.SimuladorSemana;
import java.awt.Desktop;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.util.Scanner;
import modelo.Mensajero;
import modelo.Movimiento;
import modelo.Pedido;
import modelo.Producto;
import modelo.Puesto;
import modelo.Venta;
import pruebas.PruebasSistema;

/*
 * ============================================================================
 * CLASE: Main
 * ============================================================================
 * (a) Que hace: Punto de entrada principal de MercaBazurto Digital. Carga los
 *     3 archivos CSV oficiales (200 productos, 2.000 lotes y 5.000 pedidos),
 *     arranca el servidor local en http://localhost:8080, abre automaticamente
 *     la interfaz visual en el navegador y deja disponible un menu interactivo
 *     de consola por si el docente solicita pruebas directas en terminal.
 * (b) Tema de Estructuras de Datos: Integracion general del Proyecto 9
 *     (Unidades 1, 2 y 3 en Java 17 puro sin dependencias externas).
 * (c) Complejidad Big-O principal: Carga inicial O(P + L + D); menu y
 *     servidor delegan directamente en las estructuras propias de Puesto.
 * ============================================================================
 */
public class Main {

    // === 1. ATRIBUTOS ===
    private static final int PUERTO_DEFECTO = 8080;
    private static final String CARPETA_DATOS = "datos";
    private static final String CARPETA_WEB = "web";
    private static final String FECHA_OPERACION = "2026-11-20";

    // === 2. CONSTRUCTOR ===
    public Main() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Punto de arranque principal ---
    // Recibe argumentos opcionales (--solo-servidor o --consola); inicia todo el sistema.
    public static void main(String[] args) {
        boolean soloServidor = false;
        boolean soloConsola = false;
        int puerto = PUERTO_DEFECTO;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--solo-servidor")) {
                soloServidor = true;
            } else if (args[i].equalsIgnoreCase("--consola")) {
                soloConsola = true;
            } else if (args[i].startsWith("--puerto=")) {
                puerto = Integer.parseInt(args[i].substring("--puerto=".length()));
            }
        }

        Puesto puesto = new Puesto("Puesto de Doña Rosa - Mercado de Bazurto", FECHA_OPERACION);
        String rutaDatos = resolverCarpeta(CARPETA_DATOS);
        String rutaWeb = resolverCarpeta(CARPETA_WEB);

        try {
            // Paso 1: Cargar los 3 archivos CSV oficiales (generandolos si no existen)
            puesto.cargarDesdeArchivosCSV(rutaDatos);
            puesto.cargarVentasMadrugadorasDemo();

            System.out.println("=================================================================");
            System.out.println("  MERCABAZURTO DIGITAL - Java 17 (Estructuras de Datos Propias)");
            System.out.println("=================================================================");
            System.out.println("  Catalogo cargado : " + puesto.getTotalProductos() + " productos");
            System.out.println("  Lotes FEFO       : " + puesto.getLotesCargadosCSV() + " lotes cargados");
            System.out.println("  Pedidos en fila  : " + puesto.getPedidosPendientes().getTamanio() + " pedidos");
            System.out.println("  Valor inventario : $" + puesto.calcularValorTotalInventarioRecursivo() + " COP (recursivo)");

            ServidorApp servidor = null;

            // Paso 2: Levantar servidor HTTP local salvo que se haya pedido solo consola
            if (!soloConsola) {
                servidor = new ServidorApp(puesto, rutaWeb, rutaDatos, puerto);
                servidor.iniciar();
                int puertoReal = servidor.getPuerto();
                String urlDirecta = "http://127.0.0.1:" + puertoReal;
                System.out.println("  Interfaz local   : " + urlDirecta + "  (o http://localhost:" + puertoReal + ")");
                String ipRed = obtenerIpLocal();
                if (ipRed != null) {
                    System.out.println("  Red local (Wi-Fi): http://" + ipRed + ":" + puertoReal);
                }
                System.out.println("=================================================================");

                if (!soloServidor) {
                    intentarAbrirNavegador(urlDirecta);
                }
            }

            // Paso 3: Si es modo --solo-servidor, esperar indefinidamente hasta Ctrl+C
            if (soloServidor) {
                System.out.println("Servidor activo en modo continuo. Presiona Ctrl+C para detener.");
                Thread.currentThread().join();
                return;
            }

            // Paso 4: Mantener tambien el menu interactivo de consola disponible
            boolean usuarioEligioSalir = ejecutarMenuConsola(puesto);

            // Si la consola no tenia entrada interactiva (EOF), mantener el servidor vivo
            // para que la interfaz web siga funcionando hasta pulsar Stop en IntelliJ.
            if (!usuarioEligioSalir && servidor != null) {
                System.out.println("Servidor web activo en http://127.0.0.1:" + servidor.getPuerto()
                        + " (detenlo con el boton Stop de IntelliJ o Ctrl+C).");
                Thread.currentThread().join();
                return;
            }

            if (servidor != null) {
                servidor.detener();
            }
            System.out.println("MercaBazurto Digital cerrado correctamente.");

        } catch (Exception e) {
            System.err.println("Error al iniciar MercaBazurto Digital: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- 2. Utilidades de arranque seguro en cualquier equipo ---
    // Busca la carpeta (datos/ o web/) en la raiz actual o en una carpeta superior.
    private static String resolverCarpeta(String nombre) {
        File directa = new File(nombre);
        if (directa.exists() && directa.isDirectory()) {
            return directa.getPath();
        }
        File padre = new File("..", nombre);
        if (padre.exists() && padre.isDirectory()) {
            return padre.getPath();
        }
        return nombre;
    }

    // Obtiene la IP de red local del equipo (para abrirlo incluso desde otro dispositivo en la misma red).
    private static String obtenerIpLocal() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            if (ip != null && !ip.equals("127.0.0.1")) {
                return ip;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // Recibe la URL local; intenta abrirla sin fallar si el entorno no tiene escritorio grafico.
    private static void intentarAbrirNavegador(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            System.out.println("Abre manualmente en tu navegador: " + url);
        }
    }

    // --- 3. Menu interactivo de consola (respaldo completo para sustentacion en terminal) ---
    // Recibe la instancia del Puesto; devuelve true solo si el usuario eligio salir (opcion 0).
    private static boolean ejecutarMenuConsola(Puesto puesto) {
        Scanner sc = new Scanner(System.in);
        boolean activo = true;
        boolean salioManual = false;

        while (activo) {
            System.out.println();
            System.out.println("--- MENU DE CONSOLA (la interfaz web sigue activa en el navegador) ---");
            System.out.println(" 1. Ver resumen del puesto (valor recursivo y alertas FEFO)");
            System.out.println(" 2. Registrar una venta (descuento FEFO automatico)");
            System.out.println(" 3. Ver lotes de un producto e ingresar lote / retirar vencidos");
            System.out.println(" 4. Pedidos pendientes y asignar turno Round Robin");
            System.out.println(" 5. Comparar Busqueda Secuencial vs. Binaria por codigo");
            System.out.println(" 6. Ranking de productos por ganancia (4 algoritmos)");
            System.out.println(" 7. Clientes frecuentes (Interseccion y Union de arreglos)");
            System.out.println(" 8. Simulacion semana de Doña Rosa (Cuaderno vs. FEFO)");
            System.out.println(" 9. Ejecutar Medidor de complejidad Big-O (n = 200, 2000, 5000)");
            System.out.println("10. Ver movimientos del dia en ListaDoble (adelante / atras)");
            System.out.println("11. Ejecutar las 16 pruebas automatizadas");
            System.out.println(" 0. Salir y cerrar servidor");
            System.out.print("Elige una opcion: ");

            if (!sc.hasNextLine()) {
                return false;
            }
            String opcion = sc.nextLine().trim();

            switch (opcion) {
                case "1":
                    System.out.println("Vendido hoy      : $" + puesto.getHistorialVentas().calcularTotalVendido() + " COP");
                    System.out.println("Ganancia neta    : $" + puesto.getHistorialVentas().calcularGananciaTotal() + " COP");
                    System.out.println("Valor inventario : $" + puesto.calcularValorTotalInventarioRecursivo() + " COP");
                    System.out.println("Lotes vencidos   : " + puesto.contarLotesPorEstado("VENCIDO"));
                    System.out.println("Proximos a vencer: " + puesto.contarLotesPorEstado("PROXIMO"));
                    System.out.println("Pedidos en fila  : " + puesto.getPedidosPendientes().getTamanio());
                    break;
                case "2":
                    try {
                        System.out.print("Codigo del producto (ej. P001): ");
                        String cod = sc.nextLine().trim().toUpperCase();
                        System.out.print("Kilos a vender: ");
                        int kg = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Cliente (opcional): ");
                        String cli = sc.nextLine().trim();
                        Venta v = puesto.registrarVenta(cod, kg, cli);
                        System.out.println("Venta registrada -> " + v);
                        System.out.println("Lotes descontados FEFO: " + v.getDetalleLotesUsados());
                    } catch (Exception e) {
                        System.out.println("No se pudo registrar la venta: " + e.getMessage());
                    }
                    break;
                case "3":
                    System.out.print("Codigo de producto (ej. P001) o escribe RETIRAR para sacar vencidos: ");
                    String entrada = sc.nextLine().trim().toUpperCase();
                    if (entrada.equals("RETIRAR")) {
                        int ret = puesto.retirarTodosLosVencidos(puesto.getFechaOperacion());
                        System.out.println("Kilos vencidos retirados en cabeza O(1): " + ret + " kg");
                    } else {
                        Puesto.LoteDetallado[] lotes = puesto.obtenerLotesOrdenadosFEFO(entrada, "TODOS", 15);
                        System.out.println("Lotes FEFO de " + entrada + " (" + lotes.length + "):");
                        for (int i = 0; i < lotes.length; i++) {
                            System.out.println("  [" + lotes[i].estado + "] " + lotes[i].lote);
                        }
                    }
                    break;
                case "4":
                    Pedido desp = puesto.despacharSiguientePedido(true);
                    if (desp != null) {
                        System.out.println("Despachado en O(1): " + desp + " | Mensajero: " + desp.getMensajeroAsignado());
                    } else {
                        System.out.println("No hay pedidos pendientes.");
                    }
                    Mensajero[] mens = puesto.obtenerMensajerosArreglo();
                    for (int i = 0; i < mens.length; i++) {
                        System.out.println("  " + mens[i]);
                    }
                    break;
                case "5":
                    System.out.print("Codigo a buscar (ej. P200): ");
                    String codBusq = sc.nextLine().trim().toUpperCase();
                    Busquedas.ResultadoBusqueda sec = puesto.buscarProductoSecuencial(codBusq);
                    Busquedas.ResultadoBusqueda bin = puesto.buscarProductoBinaria(codBusq);
                    System.out.println("Secuencial -> " + sec.comparaciones + " comparaciones");
                    System.out.println("Binaria    -> " + bin.comparaciones + " comparaciones");
                    for (int i = 0; i < bin.totalPasos; i++) {
                        System.out.println("  " + bin.trazaPasos[i]);
                    }
                    break;
                case "6":
                    System.out.print("Algoritmo (burbuja / burbuja_mejorada / quicksort / mergesort): ");
                    String alg = sc.nextLine().trim();
                    Ordenamientos.ResultadoOrdenamiento resOrd = puesto.obtenerRankingPorGanancia(alg);
                    System.out.println("Algoritmo: " + resOrd.algoritmo + " | Comp: " + resOrd.comparaciones
                            + " | Intercambios: " + resOrd.intercambios);
                    for (int i = 0; i < 10 && i < resOrd.productosOrdenados.length; i++) {
                        Producto p = resOrd.productosOrdenados[i];
                        System.out.println("  " + (i + 1) + ". " + p);
                    }
                    break;
                case "7":
                    String[] frec = puesto.obtenerClientesFrecuentes();
                    String[] unic = puesto.obtenerClientesUnicos();
                    System.out.println("Clientes frecuentes (S1 n S2): " + frec.length);
                    for (int i = 0; i < frec.length; i++) {
                        System.out.println("  - " + frec[i]);
                    }
                    System.out.println("Clientes unicos (S1 u S2): " + unic.length);
                    break;
                case "8":
                    SimuladorSemana.ResultadoSimulacion sim = SimuladorSemana.simularCasoDonaRosa();
                    System.out.println("Cuaderno -> Perdidos: " + sim.kgPerdidosCuaderno + " kg | Neto: $" + sim.resultadoNetoCuaderno);
                    System.out.println("FEFO     -> Perdidos: " + sim.kgPerdidosFefo + " kg | Neto: $" + sim.resultadoNetoFefo);
                    System.out.println("Ahorro   -> " + sim.kgSalvados + " kg y $" + sim.pesosSalvados + " COP salvados en 1 semana");
                    break;
                case "9":
                    Medidor.imprimirTabla(Medidor.ejecutarMedicionCompleta());
                    break;
                case "10":
                    System.out.print("Sentido (adelante / atras): ");
                    String dir = sc.nextLine().trim();
                    Movimiento[] movs = puesto.obtenerMovimientos(dir);
                    for (int i = 0; i < movs.length; i++) {
                        System.out.println("  " + movs[i]);
                    }
                    break;
                case "11":
                    PruebasSistema.main(new String[0]);
                    break;
                case "0":
                    activo = false;
                    salioManual = true;
                    break;
                default:
                    System.out.println("Opcion no reconocida.");
            }
        }
        sc.close();
        return salioManual;
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica.
}
