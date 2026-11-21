package app;

import algoritmos.Busquedas;
import algoritmos.CargadorCSV;
import algoritmos.ConjuntosArreglos;
import algoritmos.Medidor;
import algoritmos.Ordenamientos;
import algoritmos.SimuladorSemana;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;
import modelo.Lote;
import modelo.Mensajero;
import modelo.Movimiento;
import modelo.Pedido;
import modelo.Producto;
import modelo.Puesto;
import modelo.Venta;
import pruebas.PruebasSistema;

/*
 * ============================================================================
 * CLASE: ServidorApp
 * ============================================================================
 * (a) Que hace: Levanta un servidor web local ligero usando el servidor HTTP
 *     nativo que ya trae incluido Java 17 (com.sun.net.httpserver.HttpServer),
 *     sin librerias ni frameworks externos. Sirve la interfaz visual limpia en
 *     web/ y conecta cada boton de la pantalla con las estructuras de datos
 *     propias de la clase Puesto.
 * (b) Tema de Estructuras de Datos: Capa de presentacion y API local conectada
 *     directamente al nucleo de Unidades 1, 2 y 3 (sin colecciones Java).
 * (c) Complejidad Big-O principal: Despacho de rutas O(1); delega cada accion
 *     a las operaciones propias de Puesto, Listas, Busquedas y Ordenamientos.
 * ============================================================================
 */
public class ServidorApp {

    // === 1. ATRIBUTOS ===
    private final Puesto puesto;
    private final String carpetaWeb;
    private final String carpetaDatos;
    private int puerto;
    private HttpServer servidor;

    // === 2. CONSTRUCTOR ===
    // Recibe la instancia del Puesto, las rutas de web y datos, y el puerto local (ej. 8080).
    public ServidorApp(Puesto puesto, String carpetaWeb, String carpetaDatos, int puerto) {
        this.puesto = puesto;
        this.carpetaWeb = carpetaWeb;
        this.carpetaDatos = carpetaDatos;
        this.puerto = puerto;
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Iniciar y detener servidor HTTP local ---
    // No recibe parametros; escucha en todas las IPs del equipo (localhost, 127.0.0.1 y red local).
    // Si el puerto inicial (8080) esta ocupado, busca automaticamente el siguiente puerto libre.
    public void iniciar() throws IOException {
        IOException ultimoError = null;
        int puertoIntentado = puerto;

        // Probar hasta 20 puertos consecutivos (8080..8099) para evitar fallos en exposicion
        for (int intento = 0; intento < 20; intento++) {
            try {
                servidor = HttpServer.create(new InetSocketAddress(puertoIntentado), 0);
                this.puerto = puertoIntentado;
                ultimoError = null;
                break;
            } catch (IOException e) {
                ultimoError = e;
                puertoIntentado = puertoIntentado + 1;
            }
        }

        if (servidor == null) {
            throw ultimoError != null ? ultimoError : new IOException("No se encontro un puerto libre.");
        }

        // Ruta principal para archivos estaticos (index.html, styles.css, app.js)
        servidor.createContext("/", new ManejadorEstaticos());

        // Rutas de la API del comerciante
        servidor.createContext("/api/resumen", new ManejadorApi());
        servidor.createContext("/api/productos", new ManejadorApi());
        servidor.createContext("/api/ventas", new ManejadorApi());
        servidor.createContext("/api/lotes", new ManejadorApi());
        servidor.createContext("/api/pedidos", new ManejadorApi());
        servidor.createContext("/api/mensajeros", new ManejadorApi());
        servidor.createContext("/api/margen", new ManejadorApi());
        servidor.createContext("/api/clientes", new ManejadorApi());

        // Rutas de la pestaña Auditoria Tecnica / Exposicion
        servidor.createContext("/api/auditoria", new ManejadorApi());

        // Pool de hilos ligero para atender multiples peticiones fetch() en paralelo sin bloqueos
        servidor.setExecutor(Executors.newFixedThreadPool(8));
        servidor.start();
    }

    // No recibe parametros; detiene ordenadamente el servidor local.
    public void detener() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    // --- 2. Manejador de archivos estaticos de la carpeta web/ ---
    private class ManejadorEstaticos implements HttpHandler {
        @Override
        public void handle(HttpExchange intercambio) throws IOException {
            // Vaciar siempre el cuerpo de entrada para mantener limpias las conexiones keep-alive
            leerCuerpo(intercambio);
            String ruta = intercambio.getRequestURI().getPath();
            if (ruta == null || ruta.equals("/") || ruta.isEmpty()) {
                ruta = "/index.html";
            }

            // Evitar cualquier intento de salir de la carpeta web/
            if (ruta.contains("..")) {
                enviarTexto(intercambio, 403, "text/plain; charset=UTF-8", "Acceso denegado");
                return;
            }

            File archivo = new File(carpetaWeb, ruta.substring(1));
            if (!archivo.exists() || archivo.isDirectory()) {
                enviarTexto(intercambio, 404, "text/plain; charset=UTF-8", "Archivo no encontrado");
                return;
            }

            String tipoContenido = detectarTipoContenido(archivo.getName());
            byte[] bytes = leerBytesArchivo(archivo);
            intercambio.getResponseHeaders().set("Content-Type", tipoContenido);
            intercambio.getResponseHeaders().set("Cache-Control", "no-cache");
            intercambio.sendResponseHeaders(200, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        }
    }

    // --- 3. Manejador central de todas las rutas /api/* ---
    private class ManejadorApi implements HttpHandler {
        @Override
        public void handle(HttpExchange intercambio) throws IOException {
            String metodo = intercambio.getRequestMethod();
            String ruta = intercambio.getRequestURI().getPath();
            String query = intercambio.getRequestURI().getRawQuery();
            String cuerpo = leerCuerpo(intercambio);

            intercambio.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            intercambio.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            intercambio.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if (metodo.equalsIgnoreCase("OPTIONS")) {
                intercambio.sendResponseHeaders(204, -1);
                intercambio.close();
                return;
            }

            try {
                String respuestaJson;
                synchronized (puesto) {
                    respuestaJson = procesarRutaApi(metodo, ruta, query, cuerpo);
                }
                enviarTexto(intercambio, 200, "application/json; charset=UTF-8", respuestaJson);
            } catch (IllegalArgumentException e) {
                String err = "{\"ok\":false,\"error\":\"" + escaparJson(e.getMessage()) + "\"}";
                enviarTexto(intercambio, 400, "application/json; charset=UTF-8", err);
            } catch (Throwable e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error interno en el servidor.";
                String err = "{\"ok\":false,\"error\":\"" + escaparJson(msg) + "\"}";
                enviarTexto(intercambio, 500, "application/json; charset=UTF-8", err);
            }
        }
    }

    // --- 4. Enrutador simple de endpoints JSON ---
    // Recibe metodo HTTP, ruta, parametros de consulta y cuerpo JSON; devuelve la respuesta JSON.
    private String procesarRutaApi(String metodo, String ruta, String query, String cuerpo) throws IOException {
        if (ruta.equals("/api/resumen") && metodo.equals("GET")) {
            return construirJsonResumen();
        }
        if (ruta.equals("/api/productos") && metodo.equals("GET")) {
            String filtroTexto = obtenerParametroQuery(query, "q");
            String filtroCat = obtenerParametroQuery(query, "cat");
            return construirJsonProductos(filtroTexto, filtroCat);
        }
        if (ruta.equals("/api/productos") && metodo.equals("POST")) {
            String codigo = extraerCampoJson(cuerpo, "codigo");
            String nombre = extraerCampoJson(cuerpo, "nombre");
            String categoria = extraerCampoJson(cuerpo, "categoria");
            long compra = parseLongSeguro(extraerCampoJson(cuerpo, "precioCompraKg"), 0L);
            long venta = parseLongSeguro(extraerCampoJson(cuerpo, "precioVentaKg"), 0L);
            Producto creado = puesto.agregarProducto(codigo, nombre, categoria, compra, venta);
            return "{\"ok\":true,\"mensaje\":\"Producto agregado al catalogo ordenado.\",\"producto\":"
                    + productoAJson(creado) + "}";
        }
        if (ruta.equals("/api/productos") && metodo.equals("PUT")) {
            String codigo = extraerCampoJson(cuerpo, "codigo");
            String nombre = extraerCampoJson(cuerpo, "nombre");
            String categoria = extraerCampoJson(cuerpo, "categoria");
            long compra = parseLongSeguro(extraerCampoJson(cuerpo, "precioCompraKg"), 0L);
            long venta = parseLongSeguro(extraerCampoJson(cuerpo, "precioVentaKg"), 0L);
            Producto act = puesto.actualizarProducto(codigo, nombre, categoria, compra, venta);
            return "{\"ok\":true,\"mensaje\":\"Producto actualizado. Las ventas y lotes previos mantienen sus precios historicos.\",\"producto\":"
                    + productoAJson(act) + "}";
        }
        if (ruta.equals("/api/ventas") && metodo.equals("GET")) {
            String q = obtenerParametroQuery(query, "q");
            return construirJsonVentas(q);
        }
        if (ruta.equals("/api/ventas") && metodo.equals("POST")) {
            String codigoProd = extraerCampoJson(cuerpo, "codigoProducto");
            int kg = (int) parseLongSeguro(extraerCampoJson(cuerpo, "cantidadKg"), 0L);
            String cliente = extraerCampoJson(cuerpo, "cliente");
            Venta nueva = puesto.registrarVenta(codigoProd, kg, cliente);
            return "{\"ok\":true,\"mensaje\":\"Venta registrada descontando primero lo que vence antes.\",\"venta\":"
                    + ventaAJson(nueva) + "}";
        }
        if (ruta.equals("/api/lotes") && metodo.equals("GET")) {
            String codProd = obtenerParametroQuery(query, "codigoProducto");
            String estado = obtenerParametroQuery(query, "estado");
            int limite = (int) parseLongSeguro(obtenerParametroQuery(query, "limite"), 80L);
            return construirJsonLotes(codProd, estado, limite);
        }
        if (ruta.equals("/api/lotes") && metodo.equals("POST")) {
            String codProd = extraerCampoJson(cuerpo, "codigoProducto");
            String codLote = extraerCampoJson(cuerpo, "codigoLote");
            String fechaVenc = extraerCampoJson(cuerpo, "fechaVencimiento");
            int kg = (int) parseLongSeguro(extraerCampoJson(cuerpo, "cantidadKg"), 0L);
            Lote nuevo = puesto.ingresarLote(codProd, codLote, fechaVenc, kg);
            return "{\"ok\":true,\"mensaje\":\"Lote insertado en orden FEFO por fecha de vencimiento.\",\"lote\":"
                    + loteSimpleAJson(nuevo) + "}";
        }
        if (ruta.equals("/api/lotes/retirar-vencidos") && metodo.equals("POST")) {
            int retirados = puesto.retirarTodosLosVencidos(puesto.getFechaOperacion());
            String msg = retirados > 0
                    ? ("Se retiraron " + retirados + " kg de lotes vencidos desde la cabeza de cada lista.")
                    : "No habia lotes vencidos pendientes por retirar.";
            return "{\"ok\":true,\"kgRetirados\":" + retirados + ",\"mensaje\":\"" + escaparJson(msg) + "\"}";
        }
        if (ruta.equals("/api/pedidos") && metodo.equals("GET")) {
            int limite = (int) parseLongSeguro(obtenerParametroQuery(query, "limite"), 40L);
            return construirJsonPedidos(limite);
        }
        if (ruta.equals("/api/pedidos") && metodo.equals("POST")) {
            String cliente = extraerCampoJson(cuerpo, "cliente");
            String codProd = extraerCampoJson(cuerpo, "codigoProducto");
            int kg = (int) parseLongSeguro(extraerCampoJson(cuerpo, "cantidadKg"), 0L);
            int semana = (int) parseLongSeguro(extraerCampoJson(cuerpo, "semana"), 1L);
            Pedido creado = puesto.registrarPedido(cliente, codProd, kg, semana);
            return "{\"ok\":true,\"mensaje\":\"Pedido agregado al final de la fila en O(1).\",\"pedido\":"
                    + pedidoAJson(creado) + "}";
        }
        if (ruta.equals("/api/pedidos/despachar") && metodo.equals("POST")) {
            String flag = extraerCampoJson(cuerpo, "asignarMensajero");
            boolean asignar = flag == null || !flag.equalsIgnoreCase("false");
            Pedido atendido = puesto.despacharSiguientePedido(asignar);
            if (atendido == null) {
                throw new IllegalArgumentException("No hay pedidos pendientes por despachar.");
            }
            return "{\"ok\":true,\"mensaje\":\"Pedido despachado y turno asignado a "
                    + escaparJson(atendido.getMensajeroAsignado()) + ".\",\"pedido\":"
                    + pedidoAJson(atendido) + "}";
        }
        if (ruta.equals("/api/pedidos/cancelar") && metodo.equals("POST")) {
            String codigo = extraerCampoJson(cuerpo, "codigoPedido");
            Pedido canc = puesto.cancelarPedido(codigo);
            if (canc == null) {
                throw new IllegalArgumentException("No se encontro el pedido pendiente " + codigo + ".");
            }
            return "{\"ok\":true,\"mensaje\":\"Pedido " + escaparJson(canc.getCodigoPedido())
                    + " cancelado correctamente.\",\"pedido\":" + pedidoAJson(canc) + "}";
        }
        if (ruta.equals("/api/mensajeros") && metodo.equals("GET")) {
            return construirJsonMensajeros();
        }
        if (ruta.equals("/api/mensajeros/turno") && metodo.equals("POST")) {
            String destino = extraerCampoJson(cuerpo, "destino");
            Mensajero elegido = puesto.asignarSiguienteMensajero(destino);
            if (elegido == null) {
                throw new IllegalArgumentException("No hay mensajeros registrados.");
            }
            return "{\"ok\":true,\"mensaje\":\"Turno asignado a " + escaparJson(elegido.getNombre())
                    + " (viaje #" + elegido.getViajesRealizados() + ").\",\"mensajero\":"
                    + mensajeroAJson(elegido) + "}";
        }
        if (ruta.equals("/api/margen") && metodo.equals("GET")) {
            String algoritmo = obtenerParametroQuery(query, "algoritmo");
            return construirJsonMargen(algoritmo);
        }
        if (ruta.equals("/api/clientes") && metodo.equals("GET")) {
            return construirJsonClientes();
        }
        if (ruta.equals("/api/auditoria/recargar-csv") && metodo.equals("POST")) {
            CargadorCSV.generarArchivosOficiales(carpetaDatos);
            puesto.cargarDesdeArchivosCSV(carpetaDatos);
            puesto.cargarVentasMadrugadorasDemo();
            return "{\"ok\":true,\"mensaje\":\"Se regeneraron y cargaron los 3 CSV oficiales (200 productos, 2.000 lotes, 5.000 pedidos).\"}";
        }
        if (ruta.equals("/api/auditoria/buscar") && metodo.equals("GET")) {
            String codigo = obtenerParametroQuery(query, "codigo");
            if (codigo == null || codigo.trim().isEmpty()) {
                codigo = "P200";
            }
            return construirJsonComparacionBusqueda(codigo);
        }
        if (ruta.equals("/api/auditoria/medidor") && metodo.equals("GET")) {
            return construirJsonMedidor();
        }
        if (ruta.equals("/api/auditoria/simulacion") && metodo.equals("GET")) {
            int demanda = (int) parseLongSeguro(obtenerParametroQuery(query, "demanda"), 90L);
            return construirJsonSimulacion(demanda);
        }
        if (ruta.equals("/api/auditoria/movimientos") && metodo.equals("GET")) {
            String dir = obtenerParametroQuery(query, "direccion");
            return construirJsonMovimientos(dir);
        }
        if (ruta.equals("/api/auditoria/pruebas") && metodo.equals("GET")) {
            return construirJsonPruebas();
        }

        throw new IllegalArgumentException("Ruta de API no reconocida: " + ruta);
    }

    // --- 5. Constructores de respuestas JSON (manuales, sin dependencias externas) ---

    private String construirJsonResumen() {
        long valorInv = puesto.calcularValorTotalInventarioRecursivo();
        long vendidoHoy = puesto.getHistorialVentas().calcularTotalVendido();
        long gananciaHoy = puesto.getHistorialVentas().calcularGananciaTotal();
        int vencidos = puesto.contarLotesPorEstado("VENCIDO");
        int proximos = puesto.contarLotesPorEstado("PROXIMO");
        int vigentes = puesto.contarLotesPorEstado("VIGENTE");
        Mensajero prox = puesto.getMensajeros().verProximoTurno();
        Puesto.LoteDetallado[] alertas = puesto.obtenerLotesOrdenadosFEFO(null, "TODOS", 8);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"ok\":true,");
        sb.append("\"nombrePuesto\":\"").append(escaparJson(puesto.getNombrePuesto())).append("\",");
        sb.append("\"fechaOperacion\":\"").append(escaparJson(puesto.getFechaOperacion())).append("\",");
        sb.append("\"totalVendidoHoy\":").append(vendidoHoy).append(",");
        sb.append("\"gananciaNetaHoy\":").append(gananciaHoy).append(",");
        sb.append("\"cantidadVentasHoy\":").append(puesto.getHistorialVentas().getTamanio()).append(",");
        sb.append("\"valorInventarioRecursivo\":").append(valorInv).append(",");
        sb.append("\"totalProductos\":").append(puesto.getTotalProductos()).append(",");
        sb.append("\"totalLotesActivos\":").append(puesto.contarLotesActivos()).append(",");
        sb.append("\"lotesVencidos\":").append(vencidos).append(",");
        sb.append("\"lotesProximos\":").append(proximos).append(",");
        sb.append("\"lotesVigentes\":").append(vigentes).append(",");
        sb.append("\"pedidosPendientes\":").append(puesto.getPedidosPendientes().getTamanio()).append(",");
        sb.append("\"lotesCargadosCSV\":").append(puesto.getLotesCargadosCSV()).append(",");
        sb.append("\"pedidosCargadosCSV\":").append(puesto.getPedidosCargadosCSV()).append(",");
        sb.append("\"proximoMensajero\":\"").append(prox != null ? escaparJson(prox.getNombre()) : "-").append("\",");
        sb.append("\"alertasFefo\":[");
        for (int i = 0; i < alertas.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(loteDetalladoAJson(alertas[i]));
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonProductos(String filtroTexto, String filtroCat) {
        Producto[] catalogo = puesto.getCatalogo();
        int total = puesto.getTotalProductos();
        String q = filtroTexto == null ? "" : filtroTexto.trim().toLowerCase();
        String cat = filtroCat == null ? "" : filtroCat.trim().toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"totalCatalogo\":").append(total).append(",\"productos\":[");
        boolean primero = true;
        for (int i = 0; i < total; i++) {
            Producto p = catalogo[i];
            if (!cat.isEmpty() && !cat.equals("todas") && !p.getCategoria().toLowerCase().equals(cat)) {
                continue;
            }
            if (!q.isEmpty()
                    && !p.getCodigo().toLowerCase().contains(q)
                    && !p.getNombre().toLowerCase().contains(q)) {
                continue;
            }
            if (!primero) {
                sb.append(",");
            }
            primero = false;
            sb.append(productoAJson(p));
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonVentas(String filtroTexto) {
        Venta[] ventas = puesto.getHistorialVentas().aArreglo();
        String q = filtroTexto == null ? "" : filtroTexto.trim().toLowerCase();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"totalVendido\":").append(puesto.getHistorialVentas().calcularTotalVendido()).append(",");
        sb.append("\"gananciaTotal\":").append(puesto.getHistorialVentas().calcularGananciaTotal()).append(",");
        sb.append("\"cantidadVentas\":").append(ventas.length).append(",");
        sb.append("\"ventas\":[");
        boolean primero = true;
        for (int i = 0; i < ventas.length; i++) {
            Venta v = ventas[i];
            if (!q.isEmpty()
                    && !v.getIdVenta().toLowerCase().contains(q)
                    && !v.getNombreProductoSnapshot().toLowerCase().contains(q)
                    && !v.getCliente().toLowerCase().contains(q)
                    && !v.getCodigoProducto().toLowerCase().contains(q)) {
                continue;
            }
            if (!primero) {
                sb.append(",");
            }
            primero = false;
            sb.append(ventaAJson(v));
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonLotes(String codProd, String estado, int limite) {
        Puesto.LoteDetallado[] lotes = puesto.obtenerLotesOrdenadosFEFO(codProd, estado, limite);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"fechaOperacion\":\"").append(escaparJson(puesto.getFechaOperacion())).append("\",");
        sb.append("\"lotesVencidos\":").append(puesto.contarLotesPorEstado("VENCIDO")).append(",");
        sb.append("\"lotesProximos\":").append(puesto.contarLotesPorEstado("PROXIMO")).append(",");
        sb.append("\"lotesVigentes\":").append(puesto.contarLotesPorEstado("VIGENTE")).append(",");
        sb.append("\"lotes\":[");
        for (int i = 0; i < lotes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(loteDetalladoAJson(lotes[i]));
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonPedidos(int limite) {
        Pedido[] pendientes = puesto.getPedidosPendientes().aArreglo(limite);
        Pedido[] atendidos = puesto.getPedidosAtendidos();
        Mensajero prox = puesto.getMensajeros().verProximoTurno();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"totalPendientes\":").append(puesto.getPedidosPendientes().getTamanio()).append(",");
        sb.append("\"proximoMensajero\":\"").append(prox != null ? escaparJson(prox.getNombre()) : "-").append("\",");
        sb.append("\"pendientes\":[");
        for (int i = 0; i < pendientes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(pedidoAJson(pendientes[i]));
        }
        sb.append("],\"atendidos\":[");
        int maxAt = atendidos.length < 20 ? atendidos.length : 20;
        for (int i = 0; i < maxAt; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(pedidoAJson(atendidos[i]));
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonMensajeros() {
        Mensajero[] lista = puesto.obtenerMensajerosArreglo();
        Mensajero prox = puesto.getMensajeros().verProximoTurno();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"proximoTurno\":\"").append(prox != null ? escaparJson(prox.getNombre()) : "-").append("\",");
        sb.append("\"mensajeros\":[");
        for (int i = 0; i < lista.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(mensajeroAJson(lista[i]));
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonMargen(String algoritmo) {
        Ordenamientos.ResultadoOrdenamiento res = puesto.obtenerRankingPorGanancia(algoritmo);
        double tiempoMs = res.tiempoNanos / 1_000_000.0;

        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"algoritmo\":\"").append(escaparJson(res.algoritmo)).append("\",");
        sb.append("\"comparaciones\":").append(res.comparaciones).append(",");
        sb.append("\"intercambios\":").append(res.intercambios).append(",");
        sb.append("\"tiempoMs\":").append(String.format(Locale.US, "%.4f", tiempoMs)).append(",");
        sb.append("\"productos\":[");
        for (int i = 0; i < res.productosOrdenados.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(productoAJson(res.productosOrdenados[i]));
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonClientes() {
        String[] frecuentesReales = puesto.obtenerClientesFrecuentes();
        String[] unicosReales = puesto.obtenerClientesUnicos();

        // Caso de verificacion exacto del Plan de Negocio (Seccion 15.8)
        String[] s1 = {"C01 (Rest. La Mulata)", "C02 (Tienda El Sol)", "C03 (Hotel Caribe)",
                "C04 (Comedor Bazurto)", "C05 (Tienda San Diego)"};
        String[] s2 = {"C02 (Tienda El Sol)", "C04 (Comedor Bazurto)", "C05 (Tienda San Diego)",
                "C06 (Rest. El Muelle)", "C07 (Fruver Manga)"};
        String[] frecCaso = ConjuntosArreglos.interseccion(s1, s1.length, s2, s2.length);
        String[] uniCaso = ConjuntosArreglos.union(s1, s1.length, s2, s2.length);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"frecuentes\":").append(arregloStringsAJson(frecuentesReales)).append(",");
        sb.append("\"unicos\":").append(arregloStringsAJson(unicosReales)).append(",");
        sb.append("\"casoDonaRosa\":{");
        sb.append("\"s1\":").append(arregloStringsAJson(s1)).append(",");
        sb.append("\"s2\":").append(arregloStringsAJson(s2)).append(",");
        sb.append("\"frecuentes\":").append(arregloStringsAJson(frecCaso)).append(",");
        sb.append("\"unicos\":").append(arregloStringsAJson(uniCaso));
        sb.append("}}");
        return sb.toString();
    }

    private String construirJsonComparacionBusqueda(String codigo) {
        Busquedas.ResultadoBusqueda sec = puesto.buscarProductoSecuencial(codigo);
        Busquedas.ResultadoBusqueda secOrd = puesto.buscarProductoSecuencialOrdenada(codigo);
        Busquedas.ResultadoBusqueda bin = puesto.buscarProductoBinaria(codigo);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"codigoBuscado\":\"").append(escaparJson(codigo)).append("\",");
        sb.append("\"totalCatalogo\":").append(puesto.getTotalProductos()).append(",");
        sb.append("\"secuencial\":").append(resultadoBusquedaAJson(sec)).append(",");
        sb.append("\"secuencialOrdenada\":").append(resultadoBusquedaAJson(secOrd)).append(",");
        sb.append("\"binaria\":").append(resultadoBusquedaAJson(bin));
        sb.append("}");
        return sb.toString();
    }

    private String construirJsonMedidor() {
        Medidor.FilaMedicion[] filas = Medidor.ejecutarMedicionCompleta();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"filas\":[");
        for (int i = 0; i < filas.length; i++) {
            Medidor.FilaMedicion f = filas[i];
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"operacion\":\"").append(escaparJson(f.operacion)).append("\",");
            sb.append("\"bigO\":\"").append(escaparJson(f.bigO)).append("\",");
            sb.append("\"formula\":\"").append(escaparJson(f.formula)).append("\",");
            sb.append("\"ops200\":").append(f.ops200).append(",");
            sb.append("\"ops2000\":").append(f.ops2000).append(",");
            sb.append("\"ops5000\":").append(f.ops5000).append(",");
            sb.append("\"ms200\":").append(String.format(Locale.US, "%.4f", f.ms200)).append(",");
            sb.append("\"ms2000\":").append(String.format(Locale.US, "%.4f", f.ms2000)).append(",");
            sb.append("\"ms5000\":").append(String.format(Locale.US, "%.4f", f.ms5000));
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonSimulacion(int demandaKg) {
        SimuladorSemana.ResultadoSimulacion s = SimuladorSemana.simularConDemanda(demandaKg);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"kgInventarioInicial\":").append(s.kgInventarioInicial).append(",");
        sb.append("\"valorInventarioInicial\":").append(s.valorInventarioInicial).append(",");
        sb.append("\"demandaKg\":").append(s.demandaKg).append(",");
        sb.append("\"costoKg\":").append(s.costoKg).append(",");
        sb.append("\"precioVentaKg\":").append(s.precioVentaKg).append(",");
        sb.append("\"margenKg\":").append(s.margenKg).append(",");
        sb.append("\"kgVendidosCuaderno\":").append(s.kgVendidosCuaderno).append(",");
        sb.append("\"kgPerdidosCuaderno\":").append(s.kgPerdidosCuaderno).append(",");
        sb.append("\"costoBotadoCuaderno\":").append(s.costoBotadoCuaderno).append(",");
        sb.append("\"gananciaBrutaCuaderno\":").append(s.gananciaBrutaCuaderno).append(",");
        sb.append("\"resultadoNetoCuaderno\":").append(s.resultadoNetoCuaderno).append(",");
        sb.append("\"detalleDespachoCuaderno\":\"").append(escaparJson(s.detalleDespachoCuaderno)).append("\",");
        sb.append("\"detalleVencidosCuaderno\":\"").append(escaparJson(s.detalleVencidosCuaderno)).append("\",");
        sb.append("\"kgVendidosFefo\":").append(s.kgVendidosFefo).append(",");
        sb.append("\"kgPerdidosFefo\":").append(s.kgPerdidosFefo).append(",");
        sb.append("\"costoBotadoFefo\":").append(s.costoBotadoFefo).append(",");
        sb.append("\"gananciaBrutaFefo\":").append(s.gananciaBrutaFefo).append(",");
        sb.append("\"resultadoNetoFefo\":").append(s.resultadoNetoFefo).append(",");
        sb.append("\"detalleDespachoFefo\":\"").append(escaparJson(s.detalleDespachoFefo)).append("\",");
        sb.append("\"detalleSobranteFefo\":\"").append(escaparJson(s.detalleSobranteFefo)).append("\",");
        sb.append("\"kgSalvados\":").append(s.kgSalvados).append(",");
        sb.append("\"pesosSalvados\":").append(s.pesosSalvados).append(",");
        sb.append("\"resumenTurnosMensajeros\":\"").append(escaparJson(s.resumenTurnosMensajeros)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String construirJsonMovimientos(String direccion) {
        String dir = (direccion != null && direccion.equalsIgnoreCase("atras")) ? "atras" : "adelante";
        Movimiento[] movs = puesto.obtenerMovimientos(dir);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"direccion\":\"").append(dir).append("\",");
        sb.append("\"total\":").append(movs.length).append(",\"movimientos\":[");
        for (int i = 0; i < movs.length; i++) {
            Movimiento m = movs[i];
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"id\":\"").append(escaparJson(m.getIdMovimiento())).append("\",");
            sb.append("\"fechaHora\":\"").append(escaparJson(m.getFechaHora())).append("\",");
            sb.append("\"tipo\":\"").append(escaparJson(m.getTipo())).append("\",");
            sb.append("\"descripcion\":\"").append(escaparJson(m.getDescripcion())).append("\",");
            sb.append("\"montoImpacto\":").append(m.getMontoImpacto());
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String construirJsonPruebas() {
        PruebasSistema.ResultadoPrueba[] res = PruebasSistema.ejecutarTodas();
        int pasadas = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"pruebas\":[");
        for (int i = 0; i < res.length; i++) {
            if (res[i].paso) {
                pasadas = pasadas + 1;
            }
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"codigo\":\"").append(escaparJson(res[i].codigo)).append("\",");
            sb.append("\"nombre\":\"").append(escaparJson(res[i].nombre)).append("\",");
            sb.append("\"paso\":").append(res[i].paso).append(",");
            sb.append("\"detalle\":\"").append(escaparJson(res[i].detalle)).append("\"");
            sb.append("}");
        }
        sb.append("],\"pasadas\":").append(pasadas).append(",\"total\":").append(res.length).append("}");
        return sb.toString();
    }

    // --- 6. Serializadores JSON de cada TDA ---

    private String productoAJson(Producto p) {
        return "{"
                + "\"codigo\":\"" + escaparJson(p.getCodigo()) + "\","
                + "\"nombre\":\"" + escaparJson(p.getNombre()) + "\","
                + "\"categoria\":\"" + escaparJson(p.getCategoria()) + "\","
                + "\"precioCompraKg\":" + p.getPrecioCompraKg() + ","
                + "\"precioVentaKg\":" + p.getPrecioVentaKg() + ","
                + "\"gananciaKg\":" + p.calcularGananciaPorKg() + ","
                + "\"margenPorcentaje\":" + String.format(Locale.US, "%.1f", p.calcularPorcentajeMargen()) + ","
                + "\"kgDisponibles\":" + p.calcularKgDisponibles() + ","
                + "\"cantidadLotes\":" + p.getLotes().getTamanio() + ","
                + "\"proximoVencimiento\":\"" + escaparJson(p.obtenerProximoVencimiento()) + "\""
                + "}";
    }

    private String ventaAJson(Venta v) {
        return "{"
                + "\"idVenta\":\"" + escaparJson(v.getIdVenta()) + "\","
                + "\"fechaHora\":\"" + escaparJson(v.getFechaHora()) + "\","
                + "\"codigoProducto\":\"" + escaparJson(v.getCodigoProducto()) + "\","
                + "\"nombreProducto\":\"" + escaparJson(v.getNombreProductoSnapshot()) + "\","
                + "\"cliente\":\"" + escaparJson(v.getCliente()) + "\","
                + "\"cantidadKg\":" + v.getCantidadKg() + ","
                + "\"precioVentaAplicado\":" + v.getPrecioVentaAplicado() + ","
                + "\"costoCompraTotal\":" + v.getCostoCompraTotal() + ","
                + "\"totalVenta\":" + v.getTotalVenta() + ","
                + "\"gananciaReal\":" + v.getGananciaReal() + ","
                + "\"detalleLotesUsados\":\"" + escaparJson(v.getDetalleLotesUsados()) + "\""
                + "}";
    }

    private String loteSimpleAJson(Lote l) {
        return "{"
                + "\"codigoLote\":\"" + escaparJson(l.getCodigoLote()) + "\","
                + "\"codigoProducto\":\"" + escaparJson(l.getCodigoProducto()) + "\","
                + "\"fechaVencimiento\":\"" + escaparJson(l.getFechaVencimiento()) + "\","
                + "\"cantidadKg\":" + l.getCantidadKg() + ","
                + "\"precioCompraKg\":" + l.getPrecioCompraKg() + ","
                + "\"valorLote\":" + l.calcularValorLote()
                + "}";
    }

    private String loteDetalladoAJson(Puesto.LoteDetallado ld) {
        Lote l = ld.lote;
        return "{"
                + "\"codigoLote\":\"" + escaparJson(l.getCodigoLote()) + "\","
                + "\"codigoProducto\":\"" + escaparJson(l.getCodigoProducto()) + "\","
                + "\"nombreProducto\":\"" + escaparJson(ld.nombreProducto) + "\","
                + "\"categoria\":\"" + escaparJson(ld.categoria) + "\","
                + "\"fechaVencimiento\":\"" + escaparJson(l.getFechaVencimiento()) + "\","
                + "\"cantidadKg\":" + l.getCantidadKg() + ","
                + "\"precioCompraKg\":" + l.getPrecioCompraKg() + ","
                + "\"valorLote\":" + l.calcularValorLote() + ","
                + "\"estado\":\"" + escaparJson(ld.estado) + "\","
                + "\"diasParaVencer\":" + ld.diasParaVencer
                + "}";
    }

    private String pedidoAJson(Pedido p) {
        Producto prod = puesto.buscarProductoBinaria(p.getCodigoProducto()).producto;
        String nomProd = prod != null ? prod.getNombre() : p.getCodigoProducto();
        return "{"
                + "\"codigoPedido\":\"" + escaparJson(p.getCodigoPedido()) + "\","
                + "\"cliente\":\"" + escaparJson(p.getCliente()) + "\","
                + "\"codigoProducto\":\"" + escaparJson(p.getCodigoProducto()) + "\","
                + "\"nombreProducto\":\"" + escaparJson(nomProd) + "\","
                + "\"cantidadKg\":" + p.getCantidadKg() + ","
                + "\"semana\":" + p.getSemana() + ","
                + "\"fechaHora\":\"" + escaparJson(p.getFechaHora()) + "\","
                + "\"estado\":\"" + escaparJson(p.getEstado()) + "\","
                + "\"mensajeroAsignado\":\"" + escaparJson(p.getMensajeroAsignado()) + "\""
                + "}";
    }

    private String mensajeroAJson(Mensajero m) {
        return "{"
                + "\"codigo\":\"" + escaparJson(m.getCodigo()) + "\","
                + "\"nombre\":\"" + escaparJson(m.getNombre()) + "\","
                + "\"telefono\":\"" + escaparJson(m.getTelefono()) + "\","
                + "\"viajesRealizados\":" + m.getViajesRealizados() + ","
                + "\"ultimoDestino\":\"" + escaparJson(m.getUltimoDestino()) + "\""
                + "}";
    }

    private String resultadoBusquedaAJson(Busquedas.ResultadoBusqueda r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"algoritmo\":\"").append(escaparJson(r.algoritmo)).append("\",");
        sb.append("\"indice\":").append(r.indice).append(",");
        sb.append("\"comparaciones\":").append(r.comparaciones).append(",");
        sb.append("\"tiempoMicros\":").append(String.format(Locale.US, "%.2f", r.tiempoNanos / 1000.0)).append(",");
        sb.append("\"encontrado\":").append(r.producto != null).append(",");
        sb.append("\"producto\":").append(r.producto != null ? productoAJson(r.producto) : "null").append(",");
        sb.append("\"pasos\":[");
        for (int i = 0; i < r.totalPasos; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(escaparJson(r.trazaPasos[i])).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String arregloStringsAJson(String[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(escaparJson(arr[i])).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    // --- 7. Utilidades HTTP, lectura de archivos y parseo JSON sin librerias externas ---

    private void enviarTexto(HttpExchange intercambio, int codigo, String contentType, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", contentType);
        intercambio.getResponseHeaders().set("Cache-Control", "no-store");
        intercambio.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = intercambio.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String leerCuerpo(HttpExchange intercambio) throws IOException {
        try (InputStream is = intercambio.getRequestBody()) {
            byte[] data = is.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    private byte[] leerBytesArchivo(File archivo) throws IOException {
        try (FileInputStream fis = new FileInputStream(archivo)) {
            return fis.readAllBytes();
        }
    }

    private String detectarTipoContenido(String nombreArchivo) {
        String lower = nombreArchivo.toLowerCase();
        if (lower.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (lower.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (lower.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    // Extrae un parametro de la query string (?clave=valor&otra=valor)
    private String obtenerParametroQuery(String query, String claveBuscada) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        String[] pares = query.split("&");
        for (int i = 0; i < pares.length; i++) {
            int posIgual = pares[i].indexOf('=');
            if (posIgual > 0) {
                String clave = pares[i].substring(0, posIgual);
                if (clave.equals(claveBuscada)) {
                    String valor = pares[i].substring(posIgual + 1);
                    return URLDecoder.decode(valor, StandardCharsets.UTF_8);
                }
            }
        }
        return "";
    }

    // Extrae el valor de un campo de un objeto JSON plano sin usar librerias externas
    private String extraerCampoJson(String json, String campo) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        String patron = "\"" + campo + "\"";
        int idx = json.indexOf(patron);
        if (idx < 0) {
            return "";
        }
        int dosPuntos = json.indexOf(':', idx + patron.length());
        if (dosPuntos < 0) {
            return "";
        }
        int pos = dosPuntos + 1;
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos = pos + 1;
        }
        if (pos >= json.length()) {
            return "";
        }

        // Caso valor entre comillas
        if (json.charAt(pos) == '"') {
            pos = pos + 1;
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos);
                if (c == '\\' && pos + 1 < json.length()) {
                    char sig = json.charAt(pos + 1);
                    if (sig == '"' || sig == '\\' || sig == '/') {
                        sb.append(sig);
                    } else if (sig == 'n') {
                        sb.append('\n');
                    } else {
                        sb.append(sig);
                    }
                    pos = pos + 2;
                    continue;
                }
                if (c == '"') {
                    break;
                }
                sb.append(c);
                pos = pos + 1;
            }
            return sb.toString();
        }

        // Caso valor numerico o booleano
        int fin = pos;
        while (fin < json.length() && json.charAt(fin) != ',' && json.charAt(fin) != '}'
                && !Character.isWhitespace(json.charAt(fin))) {
            fin = fin + 1;
        }
        return json.substring(pos, fin).trim();
    }

    private long parseLongSeguro(String texto, long defecto) {
        if (texto == null || texto.trim().isEmpty()) {
            return defecto;
        }
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // === 4. GETTERS Y SETTERS ===

    public int getPuerto() {
        return puerto;
    }

    public Puesto getPuesto() {
        return puesto;
    }
}
