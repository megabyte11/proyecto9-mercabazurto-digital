package modelo;

import algoritmos.Busquedas;
import algoritmos.CargadorCSV;
import algoritmos.ConjuntosArreglos;
import algoritmos.Ordenamientos;
import estructuras.ListaCircular;
import estructuras.ListaDoble;
import estructuras.ListaSimpleLotes;
import estructuras.ListaSimplePedidos;
import estructuras.ListaSimpleVentas;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/*
 * ============================================================================
 * CLASE: Puesto
 * ============================================================================
 * (a) Que hace: Es el corazon operativo de MercaBazurto Digital. Integra en un
 *     solo lugar el catalogo de productos en arreglo ordenado por codigo, los
 *     lotes FEFO de cada producto, el historial inmutable de ventas, la lista
 *     de pedidos pendientes, el anillo circular Round Robin de mensajeros, la
 *     bitacora doblemente enlazada de movimientos y los conjuntos de clientes.
 * (b) Tema de Estructuras de Datos: Integracion completa de Unidades 1, 2 y 3
 *     (arreglos propios, TDA, recursividad, conjuntos, busquedas, ordenamientos,
 *     listas simples, lista circular y lista doblemente enlazada).
 * (c) Complejidad Big-O principal:
 *     - Buscar producto por codigo (binaria): O(log2 n).
 *     - Despachar pedido / asignar mensajero / registrar venta en lista: O(1).
 *     - Valor total del inventario (recursivo): O(totalLotes).
 * ============================================================================
 */
public class Puesto {

    // Clase auxiliar para mostrar lotes junto con el nombre de su producto en la vista FEFO
    public static class LoteDetallado {
        public final Lote lote;
        public final String nombreProducto;
        public final String categoria;
        public final String estado;
        public final long diasParaVencer;

        public LoteDetallado(Lote lote, String nombreProducto, String categoria,
                             String estado, long diasParaVencer) {
            this.lote = lote;
            this.nombreProducto = nombreProducto;
            this.categoria = categoria;
            this.estado = estado;
            this.diasParaVencer = diasParaVencer;
        }
    }

    // === 1. ATRIBUTOS ===
    private String nombrePuesto;
    private String fechaOperacion;

    // Arreglo propio ordenado ascendentemente por codigo (sin ArrayList)
    private Producto[] catalogo;
    private int totalProductos;

    // Listas enlazadas propias
    private ListaSimplePedidos pedidosPendientes;
    private ListaSimpleVentas historialVentas;
    private ListaCircular<Mensajero> mensajeros;
    private ListaDoble<Movimiento> movimientosDelDia;

    // Arreglos propios para historial reciente de pedidos atendidos y conjuntos de clientes
    private Pedido[] pedidosAtendidos;
    private int totalPedidosAtendidos;
    private String[] clientesSemana1;
    private int totalClientesS1;
    private String[] clientesSemana2;
    private int totalClientesS2;

    // Contadores consecutivos y metricas de carga CSV
    private int consecutivoVenta;
    private int consecutivoLote;
    private int consecutivoPedido;
    private int consecutivoMovimiento;
    private int lotesCargadosCSV;
    private int pedidosCargadosCSV;

    // === 2. CONSTRUCTOR ===
    // Recibe el nombre del puesto y la fecha de operacion; prepara todas las estructuras vacias.
    public Puesto(String nombrePuesto, String fechaOperacion) {
        this.nombrePuesto = nombrePuesto;
        this.fechaOperacion = fechaOperacion;
        reiniciarEstructuras();
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Reinicializar estructuras internas ---
    // No recibe parametros; limpia el catalogo, listas y contadores y carga los 3 mensajeros oficiales.
    public void reiniciarEstructuras() {
        this.catalogo = new Producto[256];
        this.totalProductos = 0;

        this.pedidosPendientes = new ListaSimplePedidos();
        this.historialVentas = new ListaSimpleVentas();
        this.mensajeros = new ListaCircular<>();
        this.movimientosDelDia = new ListaDoble<>();

        this.pedidosAtendidos = new Pedido[64];
        this.totalPedidosAtendidos = 0;

        this.clientesSemana1 = new String[64];
        this.totalClientesS1 = 0;
        this.clientesSemana2 = new String[64];
        this.totalClientesS2 = 0;

        this.consecutivoVenta = 1;
        this.consecutivoLote = 2500;
        this.consecutivoPedido = 5001;
        this.consecutivoMovimiento = 1;
        this.lotesCargadosCSV = 0;
        this.pedidosCargadosCSV = 0;

        // Registrar los tres mensajeros oficiales del puesto (Carlos, Dairo y Édgar)
        this.mensajeros.insertar(new Mensajero("M1", "Carlos", "300-411-2201"));
        this.mensajeros.insertar(new Mensajero("M2", "Dairo", "301-522-3302"));
        this.mensajeros.insertar(new Mensajero("M3", "Édgar", "302-633-4403"));
    }

    // --- 2. Carga completa desde los 3 archivos CSV oficiales ---
    // Recibe la carpeta de datos ("datos"); genera los CSV si no existen y carga productos, lotes y pedidos.
    public void cargarDesdeArchivosCSV(String carpetaDatos) throws IOException {
        File archivoProd = new File(carpetaDatos, "productos.csv");
        File archivoLotes = new File(carpetaDatos, "lotes.csv");
        File archivoPedidos = new File(carpetaDatos, "pedidos.csv");

        if (!archivoProd.exists() || !archivoLotes.exists() || !archivoPedidos.exists()) {
            CargadorCSV.generarArchivosOficiales(carpetaDatos);
        }

        reiniciarEstructuras();

        // Paso 1: Cargar los 200 productos ordenados por codigo
        Producto[] prods = CargadorCSV.cargarProductos(archivoProd.getPath());
        for (int i = 0; i < prods.length; i++) {
            agregarProductoInterno(prods[i], false);
        }

        // Paso 2: Cargar los 2.000 lotes en la ListaSimpleLotes de cada producto (orden FEFO)
        Lote[] lotes = CargadorCSV.cargarLotes(archivoLotes.getPath());
        for (int i = 0; i < lotes.length; i++) {
            Producto prod = buscarProductoBinaria(lotes[i].getCodigoProducto()).producto;
            if (prod != null) {
                prod.getLotes().insertarOrdenado(lotes[i]);
                lotesCargadosCSV = lotesCargadosCSV + 1;
            }
        }

        // Paso 3: Cargar los 5.000 pedidos en ListaSimplePedidos en orden de llegada O(1)
        Pedido[] pedidos = CargadorCSV.cargarPedidos(archivoPedidos.getPath());
        for (int i = 0; i < pedidos.length; i++) {
            pedidosPendientes.registrarPedido(pedidos[i]);
            registrarClienteEnSemana(pedidos[i].getCliente(), pedidos[i].getSemana());
            pedidosCargadosCSV = pedidosCargadosCSV + 1;
        }

        consecutivoPedido = pedidosCargadosCSV + 1;
        registrarMovimiento("SISTEMA", "Carga inicial CSV: " + totalProductos + " productos, "
                + lotesCargadosCSV + " lotes y " + pedidosCargadosCSV + " pedidos.", 0L);
    }

    // --- 3. Precarga de ventas madrugadoras para la vista inicial ---
    // No recibe parametros; registra 3 ventas iniciales (sin tocar P001 Mango Tommy) para dar vida al panel.
    public void cargarVentasMadrugadorasDemo() {
        registrarVenta("P002", 6, "C03 (Hotel Caribe)");
        registrarVenta("P003", 12, "C02 (Tienda El Sol)");
        registrarVenta("P004", 10, "C01 (Rest. La Mulata)");
    }

    // --- 4. Gestion del catalogo de productos (arreglo ordenado por codigo) ---
    // Recibe los datos del nuevo producto; lo inserta ordenado por codigo y devuelve el Producto creado.
    public Producto agregarProducto(String codigo, String nombre, String categoria,
                                    long precioCompraKg, long precioVentaKg) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo del producto es obligatorio.");
        }
        String codigoLimpio = codigo.trim().toUpperCase();
        if (buscarProductoBinaria(codigoLimpio).producto != null) {
            throw new IllegalArgumentException("Ya existe un producto con el codigo " + codigoLimpio + ".");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (precioCompraKg <= 0 || precioVentaKg <= 0) {
            throw new IllegalArgumentException("Los precios deben ser mayores que cero.");
        }

        Producto nuevo = new Producto(codigoLimpio, nombre.trim(), categoria.trim(),
                precioCompraKg, precioVentaKg);
        agregarProductoInterno(nuevo, true);
        return nuevo;
    }

    // Inserta un producto manteniendo el arreglo ordenado ascendentemente por codigo.
    private void agregarProductoInterno(Producto nuevo, boolean registrarBitacora) {
        // Paso 1: Si el arreglo esta lleno, ampliar su capacidad al doble manualmente
        if (totalProductos == catalogo.length) {
            Producto[] ampliado = new Producto[catalogo.length * 2];
            for (int i = 0; i < totalProductos; i++) {
                ampliado[i] = catalogo[i];
            }
            catalogo = ampliado;
        }

        // Paso 2: Encontrar la posicion exacta donde va el codigo para mantener el orden
        int pos = totalProductos;
        while (pos > 0 && catalogo[pos - 1].getCodigo().compareToIgnoreCase(nuevo.getCodigo()) > 0) {
            catalogo[pos] = catalogo[pos - 1];
            pos = pos - 1;
        }
        catalogo[pos] = nuevo;
        totalProductos = totalProductos + 1;

        if (registrarBitacora) {
            registrarMovimiento("PRODUCTO", "Producto agregado: " + nuevo.getCodigo()
                    + " - " + nuevo.getNombre() + " (compra $" + nuevo.getPrecioCompraKg()
                    + ", venta $" + nuevo.getPrecioVentaKg() + ").", 0L);
        }
    }

    // --- 5. Actualizar producto protegiendo historial ---
    // Recibe codigo y nuevos datos; actualiza SOLO los precios vigentes futuros del producto.
    // Ningun lote ya ingresado ni ninguna venta previa cambia su precio historico.
    public Producto actualizarProducto(String codigo, String nuevoNombre, String nuevaCategoria,
                                       long nuevoPrecioCompraKg, long nuevoPrecioVentaKg) {
        Producto existente = buscarProductoBinaria(codigo).producto;
        if (existente == null) {
            throw new IllegalArgumentException("No se encontro el producto con codigo " + codigo + ".");
        }
        if (nuevoPrecioCompraKg <= 0 || nuevoPrecioVentaKg <= 0) {
            throw new IllegalArgumentException("Los precios de compra y venta deben ser mayores a cero.");
        }

        long precioAnterior = existente.getPrecioVentaKg();
        // Solo actualizamos el objeto Producto (lotes y ventas guardan su propio snapshot)
        existente.actualizarDatos(nuevoNombre, nuevaCategoria, nuevoPrecioCompraKg, nuevoPrecioVentaKg);

        registrarMovimiento("PRODUCTO", "Producto actualizado " + existente.getCodigo()
                + " (" + existente.getNombre() + "): venta $" + precioAnterior
                + " -> $" + existente.getPrecioVentaKg() + " (historial intacto).", 0L);
        return existente;
    }

    // --- 6. Busquedas sobre el catalogo ---
    // Recibe un codigo; ejecuta busqueda binaria O(log2 n) sobre el arreglo ordenado.
    public Busquedas.ResultadoBusqueda buscarProductoBinaria(String codigo) {
        return Busquedas.busquedaBinaria(catalogo, totalProductos, codigo);
    }

    // Recibe un codigo; ejecuta busqueda secuencial O(n) para comparacion en auditoria.
    public Busquedas.ResultadoBusqueda buscarProductoSecuencial(String codigo) {
        return Busquedas.busquedaSecuencial(catalogo, totalProductos, codigo);
    }

    // Recibe un codigo; ejecuta busqueda secuencial ordenada con parada temprana.
    public Busquedas.ResultadoBusqueda buscarProductoSecuencialOrdenada(String codigo) {
        return Busquedas.busquedaSecuencialOrdenada(catalogo, totalProductos, codigo);
    }

    // --- 7. Ingresar nuevo lote en orden FEFO ---
    // Recibe producto, codigo opcional de lote, fecha ISO y kilos; usa el costo vigente como snapshot.
    public Lote ingresarLote(String codigoProducto, String codigoLoteOpcional,
                             String fechaVencimiento, int cantidadKg) {
        Producto producto = buscarProductoBinaria(codigoProducto).producto;
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);
        }
        if (cantidadKg <= 0) {
            throw new IllegalArgumentException("La cantidad de kilos debe ser mayor que cero.");
        }
        if (fechaVencimiento == null || !fechaVencimiento.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("La fecha debe tener formato AAAA-MM-DD.");
        }

        String codigoLote = codigoLoteOpcional;
        if (codigoLote == null || codigoLote.trim().isEmpty()) {
            codigoLote = "L-" + consecutivoLote;
            consecutivoLote = consecutivoLote + 1;
        } else {
            codigoLote = codigoLote.trim().toUpperCase();
        }

        // Snapshot del precio de compra actual del producto: queda congelado dentro del lote
        long costoHistorico = producto.getPrecioCompraKg();
        Lote nuevoLote = new Lote(codigoLote, producto.getCodigo(), fechaVencimiento,
                cantidadKg, costoHistorico);

        producto.getLotes().insertarOrdenado(nuevoLote);

        registrarMovimiento("LOTE_INGRESO", "Ingreso lote " + codigoLote + " de "
                + producto.getNombre() + " (" + cantidadKg + " kg, vence " + fechaVencimiento + ").",
                nuevoLote.calcularValorLote());
        return nuevoLote;
    }

    // --- 8. Registrar una venta descontando FEFO ---
    // Recibe producto, kilos y cliente opcional; descuenta desde el lote que vence primero y guarda snapshot.
    public Venta registrarVenta(String codigoProducto, int cantidadKg, String cliente) {
        Producto producto = buscarProductoBinaria(codigoProducto).producto;
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);
        }
        if (cantidadKg <= 0) {
            throw new IllegalArgumentException("Ingresa una cantidad de kilos mayor a cero.");
        }

        int disponibles = producto.calcularKgDisponibles();
        if (disponibles < cantidadKg) {
            throw new IllegalArgumentException("Solo hay " + disponibles + " kg disponibles de "
                    + producto.getNombre() + ".");
        }

        // Paso 1: Descontar desde la cabeza de la ListaSimpleLotes (FEFO)
        ListaSimpleLotes.ResultadoVentaLotes resLotes =
                producto.getLotes().venderDesdePrimeroConDetalle(cantidadKg);

        // Paso 2: Crear la venta inmutable congelando precio actual y costo real de los lotes usados
        String idVenta = String.format("V-%04d", consecutivoVenta);
        consecutivoVenta = consecutivoVenta + 1;

        Venta nuevaVenta = new Venta(
                idVenta,
                obtenerFechaHoraActual(),
                producto.getCodigo(),
                producto.getNombre(),
                cliente,
                resLotes.kgDespachados,
                producto.getPrecioVentaKg(),
                resLotes.costoCompraReal,
                resLotes.detalleLotes
        );

        // Paso 3: Guardar la venta en ListaSimpleVentas y registrar el movimiento en ListaDoble
        historialVentas.registrarVenta(nuevaVenta);
        registrarMovimiento("VENTA", "Venta " + idVenta + ": " + resLotes.kgDespachados + " kg de "
                + producto.getNombre() + " a " + nuevaVenta.getCliente()
                + " (lotes: " + resLotes.detalleLotes + ").", nuevaVenta.getTotalVenta());

        return nuevaVenta;
    }

    // --- 9. Retirar todos los lotes vencidos del puesto ---
    // Recibe la fecha actual; recorre el catalogo y retira los lotes vencidos en cabeza O(1).
    public int retirarTodosLosVencidos(String fechaHoy) {
        int totalKgRetirados = 0;
        for (int i = 0; i < totalProductos; i++) {
            int retirados = catalogo[i].getLotes().retirarVencidos(fechaHoy);
            totalKgRetirados = totalKgRetirados + retirados;
        }
        if (totalKgRetirados > 0) {
            registrarMovimiento("LOTE_VENCIDO", "Retiro de lotes vencidos antes de "
                    + fechaHoy + ": " + totalKgRetirados + " kg retirados de inventario.", 0L);
        }
        return totalKgRetirados;
    }

    // --- 10. Pedidos pendientes (ListaSimplePedidos O(1)) y domicilios Round Robin ---
    // Recibe cliente, codigo de producto, kilos y semana; registra el pedido al final en O(1).
    public Pedido registrarPedido(String cliente, String codigoProducto, int cantidadKg, int semana) {
        if (cliente == null || cliente.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        Producto prod = buscarProductoBinaria(codigoProducto).producto;
        if (prod == null) {
            throw new IllegalArgumentException("No existe el producto " + codigoProducto + ".");
        }
        if (cantidadKg <= 0) {
            throw new IllegalArgumentException("La cantidad de kilos debe ser mayor que cero.");
        }

        String codigoPedido = String.format("PED-%04d", consecutivoPedido);
        consecutivoPedido = consecutivoPedido + 1;
        int semanaValida = (semana == 2) ? 2 : 1;

        Pedido pedido = new Pedido(codigoPedido, cliente.trim(), prod.getCodigo(),
                cantidadKg, semanaValida, obtenerFechaHoraActual());
        pedidosPendientes.registrarPedido(pedido);
        registrarClienteEnSemana(pedido.getCliente(), semanaValida);

        registrarMovimiento("PEDIDO", "Pedido recibido " + codigoPedido + " de "
                + pedido.getCliente() + " (" + cantidadKg + " kg de " + prod.getNombre() + ").", 0L);
        return pedido;
    }

    // Despacha el primer pedido en la lista en O(1) y asigna automaticamente el siguiente mensajero Round Robin.
    public Pedido despacharSiguientePedido(boolean asignarMensajero) {
        Pedido atendido = pedidosPendientes.despacharSiguiente();
        if (atendido == null) {
            return null;
        }

        String nombreMensajero = "Retiro en puesto";
        if (asignarMensajero && !mensajeros.estaVacia()) {
            Mensajero elegido = mensajeros.siguienteTurno();
            elegido.registrarViaje(atendido.getCodigoPedido() + " - " + atendido.getCliente());
            nombreMensajero = elegido.getNombre();
        }

        atendido.marcarDespachado(nombreMensajero);
        guardarPedidoEnHistorial(atendido);

        registrarMovimiento("DOMICILIO", "Despachado " + atendido.getCodigoPedido()
                + " para " + atendido.getCliente() + " con mensajero: " + nombreMensajero + ".", 0L);
        return atendido;
    }

    // Cancela un pedido pendiente por su codigo.
    public Pedido cancelarPedido(String codigoPedido) {
        Pedido cancelado = pedidosPendientes.cancelarPorCodigo(codigoPedido);
        if (cancelado != null) {
            guardarPedidoEnHistorial(cancelado);
            registrarMovimiento("PEDIDO", "Pedido cancelado: " + cancelado.getCodigoPedido()
                    + " (" + cancelado.getCliente() + ").", 0L);
        }
        return cancelado;
    }

    // Asigna un domicilio directo usando la lista circular Round Robin en O(1).
    public Mensajero asignarSiguienteMensajero(String referenciaDestino) {
        if (mensajeros.estaVacia()) {
            return null;
        }
        String destino = (referenciaDestino == null || referenciaDestino.trim().isEmpty())
                ? "Domicilio rapido" : referenciaDestino.trim();
        Mensajero elegido = mensajeros.siguienteTurno();
        elegido.registrarViaje(destino);

        registrarMovimiento("DOMICILIO", "Turno Round Robin asignado a " + elegido.getNombre()
                + " (" + elegido.getViajesRealizados() + " viajes) -> " + destino + ".", 0L);
        return elegido;
    }

    // --- 11. Valor total del inventario calculado recursivamente ---
    // No recibe parametros; suma recursivamente el valor de cada producto y de sus lotes.
    public long calcularValorTotalInventarioRecursivo() {
        return calcularValorCatalogoRecursivo(0);
    }

    // Recorre el catalogo recursivamente sumando el valor recursivo de los lotes de cada producto.
    private long calcularValorCatalogoRecursivo(int indice) {
        // Caso base: si llegamos al final del catalogo, devolvemos 0
        if (indice >= totalProductos) {
            return 0L;
        }
        // Caso recursivo: valor recursivo del producto actual + resto del catalogo
        long valorProducto = catalogo[indice].calcularValorInventarioRecursivo();
        return valorProducto + calcularValorCatalogoRecursivo(indice + 1);
    }

    // --- 12. Ranking de productos por margen de ganancia ---
    // Recibe el nombre del algoritmo ("burbuja", "burbuja_mejorada", "quicksort" o "mergesort");
    // devuelve los productos ordenados de mayor a menor ganancia junto con comparaciones y tiempo.
    public Ordenamientos.ResultadoOrdenamiento obtenerRankingPorGanancia(String algoritmo) {
        String clave = (algoritmo == null) ? "mergesort" : algoritmo.trim().toLowerCase();
        if (clave.equals("burbuja")) {
            return Ordenamientos.burbuja(catalogo, totalProductos);
        }
        if (clave.equals("burbuja_mejorada")) {
            return Ordenamientos.burbujaMejorada(catalogo, totalProductos);
        }
        if (clave.equals("quicksort")) {
            return Ordenamientos.quickSort(catalogo, totalProductos);
        }
        return Ordenamientos.mergeSort(catalogo, totalProductos);
    }

    // --- 13. Operaciones de conjuntos para clientes frecuentes ---
    // No recibe parametros; devuelve S1 n S2 (clientes que compraron en ambas semanas).
    public String[] obtenerClientesFrecuentes() {
        return ConjuntosArreglos.interseccion(clientesSemana1, totalClientesS1,
                clientesSemana2, totalClientesS2);
    }

    // No recibe parametros; devuelve S1 u S2 (total de clientes unicos en la quincena).
    public String[] obtenerClientesUnicos() {
        return ConjuntosArreglos.union(clientesSemana1, totalClientesS1,
                clientesSemana2, totalClientesS2);
    }

    // --- 14. Consulta consolidada de lotes ordenados por vencimiento FEFO ---
    // Recibe filtros opcionales de producto y estado ("TODOS", "VENCIDO", "PROXIMO", "VIGENTE") y limite;
    // devuelve los lotes mas urgentes ordenados ascendentemente por fecha de vencimiento.
    public LoteDetallado[] obtenerLotesOrdenadosFEFO(String codigoProductoFiltro,
                                                     String estadoFiltro, int limite) {
        // Paso 1: Contar cuantos lotes coinciden para dimensionar arreglo propio
        int capacidad = 256;
        LoteDetallado[] encontrados = new LoteDetallado[capacidad];
        int total = 0;

        boolean filtrarProd = codigoProductoFiltro != null && !codigoProductoFiltro.trim().isEmpty();
        boolean filtrarEstado = estadoFiltro != null && !estadoFiltro.trim().isEmpty()
                && !estadoFiltro.equalsIgnoreCase("TODOS");

        for (int i = 0; i < totalProductos; i++) {
            Producto p = catalogo[i];
            if (filtrarProd && !p.getCodigo().equalsIgnoreCase(codigoProductoFiltro.trim())) {
                continue;
            }
            Lote[] lotesProd = p.getLotes().aArreglo();
            for (int j = 0; j < lotesProd.length; j++) {
                Lote l = lotesProd[j];
                String est = l.obtenerEstado(fechaOperacion);
                if (filtrarEstado && !est.equalsIgnoreCase(estadoFiltro.trim())) {
                    continue;
                }
                if (total == encontrados.length) {
                    LoteDetallado[] ampliado = new LoteDetallado[encontrados.length * 2];
                    for (int k = 0; k < total; k++) {
                        ampliado[k] = encontrados[k];
                    }
                    encontrados = ampliado;
                }
                encontrados[total] = new LoteDetallado(l, p.getNombre(), p.getCategoria(),
                        est, l.calcularDiasParaVencer(fechaOperacion));
                total = total + 1;
            }
        }

        // Paso 2: Ordenar por fecha de vencimiento ascendente usando Merge Sort propio O(m log m)
        if (total > 1) {
            LoteDetallado[] aux = new LoteDetallado[total];
            ordenarLotesPorFecha(encontrados, aux, 0, total - 1);
        }

        // Paso 3: Recortar al limite solicitado
        int cantidadFinal = total;
        if (limite > 0 && limite < cantidadFinal) {
            cantidadFinal = limite;
        }
        LoteDetallado[] resultado = new LoteDetallado[cantidadFinal];
        for (int i = 0; i < cantidadFinal; i++) {
            resultado[i] = encontrados[i];
        }
        return resultado;
    }

    // Merge Sort propio sobre LoteDetallado[] por fecha de vencimiento ascendente.
    private void ordenarLotesPorFecha(LoteDetallado[] arr, LoteDetallado[] aux, int ini, int fin) {
        if (ini >= fin) {
            return;
        }
        int mid = ini + (fin - ini) / 2;
        ordenarLotesPorFecha(arr, aux, ini, mid);
        ordenarLotesPorFecha(arr, aux, mid + 1, fin);

        int i = ini;
        int j = mid + 1;
        int k = ini;
        while (i <= mid && j <= fin) {
            if (arr[i].lote.getFechaVencimiento().compareTo(arr[j].lote.getFechaVencimiento()) <= 0) {
                aux[k] = arr[i];
                i = i + 1;
            } else {
                aux[k] = arr[j];
                j = j + 1;
            }
            k = k + 1;
        }
        while (i <= mid) {
            aux[k] = arr[i];
            i = i + 1;
            k = k + 1;
        }
        while (j <= fin) {
            aux[k] = arr[j];
            j = j + 1;
            k = k + 1;
        }
        for (int p = ini; p <= fin; p++) {
            arr[p] = aux[p];
        }
    }

    // --- 15. Metodos auxiliares internos ---
    // Registra un movimiento en la ListaDoble<Movimiento> (bonificacion).
    private void registrarMovimiento(String tipo, String descripcion, long montoImpacto) {
        String id = String.format("MOV-%04d", consecutivoMovimiento);
        consecutivoMovimiento = consecutivoMovimiento + 1;
        Movimiento mov = new Movimiento(id, obtenerFechaHoraActual(), tipo, descripcion, montoImpacto);
        movimientosDelDia.insertarAlFinal(mov);
    }

    // Agrega un cliente sin duplicados al arreglo de la semana 1 o semana 2.
    private void registrarClienteEnSemana(String cliente, int semana) {
        if (cliente == null || cliente.trim().isEmpty()) {
            return;
        }
        if (semana == 1) {
            if (!ConjuntosArreglos.contiene(clientesSemana1, totalClientesS1, cliente)) {
                if (totalClientesS1 == clientesSemana1.length) {
                    String[] amp = new String[clientesSemana1.length * 2];
                    for (int i = 0; i < totalClientesS1; i++) {
                        amp[i] = clientesSemana1[i];
                    }
                    clientesSemana1 = amp;
                }
                clientesSemana1[totalClientesS1] = cliente;
                totalClientesS1 = totalClientesS1 + 1;
            }
        } else {
            if (!ConjuntosArreglos.contiene(clientesSemana2, totalClientesS2, cliente)) {
                if (totalClientesS2 == clientesSemana2.length) {
                    String[] amp = new String[clientesSemana2.length * 2];
                    for (int i = 0; i < totalClientesS2; i++) {
                        amp[i] = clientesSemana2[i];
                    }
                    clientesSemana2 = amp;
                }
                clientesSemana2[totalClientesS2] = cliente;
                totalClientesS2 = totalClientesS2 + 1;
            }
        }
    }

    // Guarda pedidos despachados o cancelados recientes al inicio de un arreglo propio.
    private void guardarPedidoEnHistorial(Pedido p) {
        if (totalPedidosAtendidos == pedidosAtendidos.length) {
            Pedido[] amp = new Pedido[pedidosAtendidos.length * 2];
            for (int i = 0; i < totalPedidosAtendidos; i++) {
                amp[i] = pedidosAtendidos[i];
            }
            pedidosAtendidos = amp;
        }
        pedidosAtendidos[totalPedidosAtendidos] = p;
        totalPedidosAtendidos = totalPedidosAtendidos + 1;
    }

    // Devuelve los mensajeros actuales en un arreglo propio.
    public Mensajero[] obtenerMensajerosArreglo() {
        Mensajero[] arr = new Mensajero[mensajeros.getTamanio()];
        mensajeros.llenarArreglo(arr);
        return arr;
    }

    // Devuelve los movimientos recorridos en el sentido elegido ("adelante" o "atras").
    public Movimiento[] obtenerMovimientos(String direccion) {
        int tam = movimientosDelDia.getTamanio();
        Movimiento[] arr = new Movimiento[tam];
        if (direccion != null && direccion.equalsIgnoreCase("atras")) {
            movimientosDelDia.recorrerAtras(arr);
        } else {
            movimientosDelDia.recorrerAdelante(arr);
        }
        return arr;
    }

    // Cuenta lotes totales activos actualmente en el puesto.
    public int contarLotesActivos() {
        int total = 0;
        for (int i = 0; i < totalProductos; i++) {
            total = total + catalogo[i].getLotes().getTamanio();
        }
        return total;
    }

    // Cuenta lotes por estado ("VENCIDO", "PROXIMO", "VIGENTE").
    public int contarLotesPorEstado(String estadoBuscado) {
        int total = 0;
        for (int i = 0; i < totalProductos; i++) {
            Lote[] lotesProd = catalogo[i].getLotes().aArreglo();
            for (int j = 0; j < lotesProd.length; j++) {
                if (lotesProd[j].obtenerEstado(fechaOperacion).equalsIgnoreCase(estadoBuscado)) {
                    total = total + 1;
                }
            }
        }
        return total;
    }

    private String obtenerFechaHoraActual() {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        return fechaOperacion + " " + hora;
    }

    // === 4. GETTERS Y SETTERS ===

    public String getNombrePuesto() {
        return nombrePuesto;
    }

    public String getFechaOperacion() {
        return fechaOperacion;
    }

    public void setFechaOperacion(String fechaOperacion) {
        this.fechaOperacion = fechaOperacion;
    }

    public Producto[] getCatalogo() {
        return catalogo;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public ListaSimplePedidos getPedidosPendientes() {
        return pedidosPendientes;
    }

    public ListaSimpleVentas getHistorialVentas() {
        return historialVentas;
    }

    public ListaCircular<Mensajero> getMensajeros() {
        return mensajeros;
    }

    public ListaDoble<Movimiento> getMovimientosDelDia() {
        return movimientosDelDia;
    }

    public Pedido[] getPedidosAtendidos() {
        Pedido[] res = new Pedido[totalPedidosAtendidos];
        for (int i = 0; i < totalPedidosAtendidos; i++) {
            res[i] = pedidosAtendidos[totalPedidosAtendidos - 1 - i];
        }
        return res;
    }

    public int getLotesCargadosCSV() {
        return lotesCargadosCSV;
    }

    public int getPedidosCargadosCSV() {
        return pedidosCargadosCSV;
    }
}
