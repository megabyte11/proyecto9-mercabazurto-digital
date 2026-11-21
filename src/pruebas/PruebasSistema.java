package pruebas;

import algoritmos.Busquedas;
import algoritmos.ConjuntosArreglos;
import algoritmos.Ordenamientos;
import algoritmos.SimuladorSemana;
import estructuras.ListaCircular;
import estructuras.ListaDoble;
import estructuras.ListaSimpleLotes;
import estructuras.ListaSimplePedidos;
import modelo.Lote;
import modelo.Mensajero;
import modelo.Movimiento;
import modelo.Pedido;
import modelo.Producto;
import modelo.Puesto;
import modelo.Venta;

/*
 * ============================================================================
 * CLASE: PruebasSistema
 * ============================================================================
 * (a) Que hace: Ejecuta 16 casos de prueba automatizados sin librerias
 *     externas (sin JUnit) para verificar todas las estructuras propias,
 *     algoritmos, carga de CSV, simulacion de Doña Rosa y la regla estricta
 *     de inmutabilidad historica al actualizar un producto.
 * (b) Tema de Estructuras de Datos: Verificacion integral de calidad (QA) de
 *     las Unidades 1, 2 y 3 exigida en la Rubrica de la Entrega 3.
 * (c) Complejidad Big-O principal: Ejecuta pruebas unitarias e integradas en
 *     tiempo lineal/logaritmico con salida clara [PASS] o [FAIL].
 * ============================================================================
 */
public class PruebasSistema {

    // Clase auxiliar que almacena el resultado de cada prueba individual
    public static class ResultadoPrueba {
        public final String codigo;
        public final String nombre;
        public final boolean paso;
        public final String detalle;

        public ResultadoPrueba(String codigo, String nombre, boolean paso, String detalle) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.paso = paso;
            this.detalle = detalle;
        }
    }

    // === 1. ATRIBUTOS ===
    private static final String CARPETA_DATOS = "datos";

    // === 2. CONSTRUCTOR ===
    public PruebasSistema() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Ejecutar las 16 pruebas completas del sistema ---
    // No recibe parametros; corre las 16 verificaciones y devuelve el arreglo de resultados.
    public static ResultadoPrueba[] ejecutarTodas() {
        ResultadoPrueba[] resultados = new ResultadoPrueba[16];
        resultados[0] = prueba01InsercionOrdenadaFEFO();
        resultados[1] = prueba02RetiroVencidosEnCabeza();
        resultados[2] = prueba03VentaFEFOMultilote();
        resultados[3] = prueba04VentaMayorAlStockSinNegativos();
        resultados[4] = prueba05PedidosOrdenDeLlegadaO1();
        resultados[5] = prueba06CancelacionPedidoPorCodigo();
        resultados[6] = prueba07ListaCircularRoundRobin7Domicilios();
        resultados[7] = prueba08ValorInventarioRecursivo();
        resultados[8] = prueba09BusquedaBinariaVsSecuencial200Productos();
        resultados[9] = prueba10CargaCSVOficialYSecuencialOrdenada();
        resultados[10] = prueba11CuatroOrdenamientosMismoRanking();
        resultados[11] = prueba12BurbujaMejoradaMejorCasoOn();
        resultados[12] = prueba13ConjuntosUnionEInterseccion();
        resultados[13] = prueba14ListaDobleRecorridoYEliminacion();
        resultados[14] = prueba15SimulacionSemanaDonaRosa();
        resultados[15] = prueba16InmutabilidadHistoricaAlActualizarProducto();
        return resultados;
    }

    // --- 2. Casos de prueba individuales (P01 a P16) ---

    // P01: Insercion ordenada FEFO en ListaSimpleLotes
    private static ResultadoPrueba prueba01InsercionOrdenadaFEFO() {
        ListaSimpleLotes lista = new ListaSimpleLotes();
        lista.insertarOrdenado(new Lote("L-101", "P001", "2026-11-14", 40, 3200));
        lista.insertarOrdenado(new Lote("L-102", "P001", "2026-11-12", 25, 3200));
        lista.insertarOrdenado(new Lote("L-103", "P001", "2026-11-18", 50, 3200));
        lista.insertarOrdenado(new Lote("L-100", "P001", "2026-11-10", 10, 3200));
        lista.insertarOrdenado(new Lote("L-104", "P001", "2026-11-15", 30, 3200));

        Lote[] arr = lista.aArreglo();
        boolean ok = arr.length == 5
                && arr[0].getCodigoLote().equals("L-100")
                && arr[1].getCodigoLote().equals("L-102")
                && arr[2].getCodigoLote().equals("L-101")
                && arr[3].getCodigoLote().equals("L-104")
                && arr[4].getCodigoLote().equals("L-103");

        return new ResultadoPrueba("P01", "Insercion ordenada FEFO en ListaSimpleLotes", ok,
                "Orden obtenido: L-100 -> L-102 -> L-101 -> L-104 -> L-103");
    }

    // P02: Retiro de lotes vencidos en cabeza O(1)
    private static ResultadoPrueba prueba02RetiroVencidosEnCabeza() {
        ListaSimpleLotes lista = new ListaSimpleLotes();
        lista.insertarOrdenado(new Lote("L-101", "P001", "2026-11-14", 40, 3200));
        lista.insertarOrdenado(new Lote("L-102", "P001", "2026-11-12", 25, 3200));
        lista.insertarOrdenado(new Lote("L-103", "P001", "2026-11-18", 50, 3200));
        lista.insertarOrdenado(new Lote("L-100", "P001", "2026-11-10", 10, 3200));
        lista.insertarOrdenado(new Lote("L-104", "P001", "2026-11-15", 30, 3200));

        int retirados = lista.retirarVencidos("2026-11-11");
        boolean ok = retirados == 10
                && lista.getTamanio() == 4
                && lista.obtenerPrimero().getCodigoLote().equals("L-102")
                && lista.calcularKgTotales() == 145;

        return new ResultadoPrueba("P02", "Retiro de lotes vencidos en cabeza O(1)", ok,
                "Retirados: " + retirados + " kg (L-100); cabeza actual: L-102; activos: 145 kg");
    }

    // P03: Venta FEFO agotando cabeza y descontando parcialmente del siguiente
    private static ResultadoPrueba prueba03VentaFEFOMultilote() {
        ListaSimpleLotes lista = new ListaSimpleLotes();
        lista.insertarOrdenado(new Lote("L-102", "P001", "2026-11-12", 25, 3200));
        lista.insertarOrdenado(new Lote("L-101", "P001", "2026-11-14", 40, 3200));
        lista.insertarOrdenado(new Lote("L-104", "P001", "2026-11-15", 30, 3200));
        lista.insertarOrdenado(new Lote("L-103", "P001", "2026-11-18", 50, 3200));

        int despachados = lista.venderDesdePrimero(35);
        boolean ok = despachados == 35
                && lista.obtenerPrimero().getCodigoLote().equals("L-101")
                && lista.obtenerPrimero().getCantidadKg() == 30
                && lista.calcularKgTotales() == 110;

        return new ResultadoPrueba("P03", "Venta FEFO consumiendo dos lotes consecutivos", ok,
                "Despachados 35 kg (25 de L-102 + 10 de L-101); quedan 110 kg");
    }

    // P04: Venta solicitando mas kilos de los disponibles
    private static ResultadoPrueba prueba04VentaMayorAlStockSinNegativos() {
        ListaSimpleLotes lista = new ListaSimpleLotes();
        lista.insertarOrdenado(new Lote("L-201", "P001", "2026-11-22", 20, 3200));

        int despachados = lista.venderDesdePrimero(50);
        boolean ok = despachados == 20 && lista.estaVacia() && lista.calcularKgTotales() == 0;

        return new ResultadoPrueba("P04", "Venta mayor al stock sin saldos negativos", ok,
                "Solicitados 50 kg, despachados " + despachados + " kg, lista vacia segura");
    }

    // P05: ListaSimplePedidos entra al final O(1) y sale al inicio O(1)
    private static ResultadoPrueba prueba05PedidosOrdenDeLlegadaO1() {
        ListaSimplePedidos cola = new ListaSimplePedidos();
        cola.registrarPedido(new Pedido("PED-01", "Rest. La Mulata", "P001", 35, 1, "05:00"));
        cola.registrarPedido(new Pedido("PED-02", "Tienda El Sol", "P003", 15, 1, "05:05"));
        cola.registrarPedido(new Pedido("PED-03", "Hotel Caribe", "P002", 20, 1, "05:10"));
        cola.registrarPedido(new Pedido("PED-04", "Comedor Bazurto", "P004", 10, 1, "05:15"));

        Pedido primero = cola.despacharSiguiente();
        Pedido segundo = cola.despacharSiguiente();
        boolean ok = primero.getCodigoPedido().equals("PED-01")
                && segundo.getCodigoPedido().equals("PED-02")
                && cola.verPrimero().getCodigoPedido().equals("PED-03")
                && cola.getTamanio() == 2;

        return new ResultadoPrueba("P05", "ListaSimplePedidos en orden de llegada O(1)", ok,
                "Salieron PED-01 y PED-02; siguiente en fila: PED-03");
    }

    // P06: Cancelacion de pedidos (inicio, medio y final) actualizando primero y ultimo
    private static ResultadoPrueba prueba06CancelacionPedidoPorCodigo() {
        ListaSimplePedidos cola = new ListaSimplePedidos();
        cola.registrarPedido(new Pedido("PED-01", "Cliente 1", "P001", 10, 1, "05:00"));
        cola.registrarPedido(new Pedido("PED-02", "Cliente 2", "P002", 10, 1, "05:05"));
        cola.registrarPedido(new Pedido("PED-03", "Cliente 3", "P003", 10, 1, "05:10"));

        Pedido cancMedio = cola.cancelarPorCodigo("PED-02");
        Pedido cancUltimo = cola.cancelarPorCodigo("PED-03");
        cola.registrarPedido(new Pedido("PED-04", "Cliente 4", "P004", 10, 1, "05:15"));

        boolean ok = cancMedio != null && cancUltimo != null
                && cola.getTamanio() == 2
                && cola.despacharSiguiente().getCodigoPedido().equals("PED-01")
                && cola.despacharSiguiente().getCodigoPedido().equals("PED-04")
                && cola.estaVacia();

        return new ResultadoPrueba("P06", "Cancelacion de pedido por codigo (medio y final)", ok,
                "Punteros primero/ultimo consistentes tras cancelar PED-02 y PED-03");
    }

    // P07: ListaCircular Round Robin con 7 domicilios entre 3 mensajeros
    private static ResultadoPrueba prueba07ListaCircularRoundRobin7Domicilios() {
        ListaCircular<Mensajero> anillo = new ListaCircular<>();
        Mensajero carlos = new Mensajero("M1", "Carlos", "300");
        Mensajero dairo = new Mensajero("M2", "Dairo", "301");
        Mensajero edgar = new Mensajero("M3", "Edgar", "302");
        anillo.insertar(carlos);
        anillo.insertar(dairo);
        anillo.insertar(edgar);

        for (int d = 1; d <= 7; d++) {
            Mensajero turno = anillo.siguienteTurno();
            turno.registrarViaje("Domicilio D" + d);
        }

        boolean ok = carlos.getViajesRealizados() == 3
                && dairo.getViajesRealizados() == 2
                && edgar.getViajesRealizados() == 2;

        return new ResultadoPrueba("P07", "ListaCircular Round Robin (7 domicilios / 3 mensajeros)", ok,
                "Carlos: " + carlos.getViajesRealizados() + ", Dairo: "
                        + dairo.getViajesRealizados() + ", Edgar: " + edgar.getViajesRealizados());
    }

    // P08: Calculo recursivo del valor del inventario ($464.000 COP)
    private static ResultadoPrueba prueba08ValorInventarioRecursivo() {
        ListaSimpleLotes lista = new ListaSimpleLotes();
        lista.insertarOrdenado(new Lote("L-102", "P001", "2026-11-12", 25, 3200));
        lista.insertarOrdenado(new Lote("L-101", "P001", "2026-11-14", 40, 3200));
        lista.insertarOrdenado(new Lote("L-104", "P001", "2026-11-15", 30, 3200));
        lista.insertarOrdenado(new Lote("L-103", "P001", "2026-11-18", 50, 3200));

        long valor = lista.calcularValorRecursivo();
        boolean ok = valor == 464000L;

        return new ResultadoPrueba("P08", "Calculo recursivo del valor del inventario", ok,
                "145 kg x $3.200 = $" + valor + " COP (coincide con traza recursiva)");
    }

    // P09: Busqueda binaria vs secuencial sobre los 200 productos de productos.csv
    private static ResultadoPrueba prueba09BusquedaBinariaVsSecuencial200Productos() {
        try {
            Puesto puesto = new Puesto("Prueba", "2026-11-20");
            puesto.cargarDesdeArchivosCSV(CARPETA_DATOS);

            Busquedas.ResultadoBusqueda resBin = puesto.buscarProductoBinaria("P200");
            Busquedas.ResultadoBusqueda resSec = puesto.buscarProductoSecuencial("P200");
            Busquedas.ResultadoBusqueda resNoExiste = puesto.buscarProductoBinaria("P999");

            boolean ok = puesto.getTotalProductos() == 200
                    && resBin.indice == 199 && resBin.comparaciones == 8
                    && resSec.indice == 199 && resSec.comparaciones == 200
                    && resNoExiste.indice == -1;

            return new ResultadoPrueba("P09", "Busqueda binaria (8 comp.) vs secuencial (200 comp.)", ok,
                    "P200 hallado en " + resBin.comparaciones + " comp. binaria vs "
                            + resSec.comparaciones + " secuencial (-96% ops)");
        } catch (Exception e) {
            return new ResultadoPrueba("P09", "Busqueda binaria vs secuencial", false, e.getMessage());
        }
    }

    // P10: Carga oficial de los 3 CSV (200 / 2.000 / 5.000) y busqueda secuencial ordenada
    private static ResultadoPrueba prueba10CargaCSVOficialYSecuencialOrdenada() {
        try {
            Puesto puesto = new Puesto("Prueba CSV", "2026-11-20");
            puesto.cargarDesdeArchivosCSV(CARPETA_DATOS);

            Busquedas.ResultadoBusqueda resOrd = puesto.buscarProductoSecuencialOrdenada("P050A");
            boolean ok = puesto.getTotalProductos() == 200
                    && puesto.getLotesCargadosCSV() == 2000
                    && puesto.getPedidosCargadosCSV() == 5000
                    && resOrd.indice == -1
                    && resOrd.comparaciones < 200;

            return new ResultadoPrueba("P10", "Carga 3 CSV oficiales (200/2000/5000) y sec. ordenada", ok,
                    "Cargados 200 prod, 2000 lotes, 5000 pedidos; P050A paro en "
                            + resOrd.comparaciones + " comp.");
        } catch (Exception e) {
            return new ResultadoPrueba("P10", "Carga 3 CSV oficiales", false, e.getMessage());
        }
    }

    // P11: Los 4 ordenamientos producen el mismo ranking exacto descendente
    private static ResultadoPrueba prueba11CuatroOrdenamientosMismoRanking() {
        Producto[] muestra = new Producto[5];
        muestra[0] = new Producto("P001", "Mango Tommy", "Fruta", 3200, 4700);       // 1500
        muestra[1] = new Producto("P002", "Pargo Rojo", "Pescado", 18000, 24000);    // 6000
        muestra[2] = new Producto("P003", "Tomate Chonto", "Verdura", 2500, 3300);   // 800
        muestra[3] = new Producto("P004", "Aguacate Papelillo", "Fruta", 5000, 7500);// 2500
        muestra[4] = new Producto("P005", "Sierra Fresca", "Pescado", 14000, 18500); // 4500

        Producto[] r1 = Ordenamientos.burbuja(muestra, 5).productosOrdenados;
        Producto[] r2 = Ordenamientos.burbujaMejorada(muestra, 5).productosOrdenados;
        Producto[] r3 = Ordenamientos.quickSort(muestra, 5).productosOrdenados;
        Producto[] r4 = Ordenamientos.mergeSort(muestra, 5).productosOrdenados;

        long[] esperado = {6000, 4500, 2500, 1500, 800};
        boolean ok = true;
        for (int i = 0; i < 5; i++) {
            if (r1[i].calcularGananciaPorKg() != esperado[i]
                    || r2[i].calcularGananciaPorKg() != esperado[i]
                    || r3[i].calcularGananciaPorKg() != esperado[i]
                    || r4[i].calcularGananciaPorKg() != esperado[i]) {
                ok = false;
            }
        }

        return new ResultadoPrueba("P11", "4 ordenamientos producen el mismo ranking de ganancia", ok,
                "Orden verificado: Pargo ($6000) > Sierra ($4500) > Aguacate ($2500) > Mango ($1500) > Tomate ($800)");
    }

    // P12: Burbuja mejorada realiza solo n - 1 comparaciones cuando el arreglo ya esta ordenado
    private static ResultadoPrueba prueba12BurbujaMejoradaMejorCasoOn() {
        Producto[] yaOrdenado = new Producto[5];
        yaOrdenado[0] = new Producto("P002", "Pargo Rojo", "Pescado", 18000, 24000);
        yaOrdenado[1] = new Producto("P005", "Sierra Fresca", "Pescado", 14000, 18500);
        yaOrdenado[2] = new Producto("P004", "Aguacate Papelillo", "Fruta", 5000, 7500);
        yaOrdenado[3] = new Producto("P001", "Mango Tommy", "Fruta", 3200, 4700);
        yaOrdenado[4] = new Producto("P003", "Tomate Chonto", "Verdura", 2500, 3300);

        Ordenamientos.ResultadoOrdenamiento resEstandar = Ordenamientos.burbuja(yaOrdenado, 5);
        Ordenamientos.ResultadoOrdenamiento resMejorada = Ordenamientos.burbujaMejorada(yaOrdenado, 5);

        boolean ok = resEstandar.comparaciones == 10
                && resMejorada.comparaciones == 4
                && resMejorada.intercambios == 0;

        return new ResultadoPrueba("P12", "Burbuja mejorada O(n) en arreglo ya ordenado", ok,
                "Mejorada: " + resMejorada.comparaciones + " comp. vs Estandar: "
                        + resEstandar.comparaciones + " comp. (-60%)");
    }

    // P13: Union e Interseccion de clientes con arreglos propios (Seccion 15.8)
    private static ResultadoPrueba prueba13ConjuntosUnionEInterseccion() {
        String[] s1 = {"C01 (Rest. La Mulata)", "C02 (Tienda El Sol)", "C03 (Hotel Caribe)",
                "C04 (Comedor Bazurto)", "C05 (Tienda San Diego)"};
        String[] s2 = {"C02 (Tienda El Sol)", "C04 (Comedor Bazurto)", "C05 (Tienda San Diego)",
                "C06 (Rest. El Muelle)", "C07 (Fruver Manga)"};

        String[] frecuentes = ConjuntosArreglos.interseccion(s1, s1.length, s2, s2.length);
        String[] unicos = ConjuntosArreglos.union(s1, s1.length, s2, s2.length);

        boolean ok = frecuentes.length == 3 && unicos.length == 7
                && (unicos.length == s1.length + s2.length - frecuentes.length);

        return new ResultadoPrueba("P13", "Conjuntos sobre arreglos (S1 n S2 y S1 u S2)", ok,
                "Frecuentes (interseccion): " + frecuentes.length
                        + "; Unicos (union): " + unicos.length + " (5 + 5 - 3 = 7)");
    }

    // P14: ListaDoble (bonificacion +0,3) - insercion, eliminacion y recorrido bidireccional
    private static ResultadoPrueba prueba14ListaDobleRecorridoYEliminacion() {
        ListaDoble<Movimiento> lista = new ListaDoble<>();
        Movimiento m1 = new Movimiento("MOV-1", "04:10", "LOTE_INGRESO", "Lote 1", 80000);
        Movimiento m2 = new Movimiento("MOV-2", "04:20", "VENTA", "Venta 1", 47000);
        Movimiento m3 = new Movimiento("MOV-3", "04:30", "PEDIDO", "Pedido 1", 0);
        Movimiento m4 = new Movimiento("MOV-4", "04:40", "DOMICILIO", "Domicilio 1", 0);

        lista.insertarAlFinal(m1);
        lista.insertarAlFinal(m2);
        lista.insertarAlFinal(m3);
        lista.insertarAlFinal(m4);
        boolean elimino = lista.eliminarDato(m2); // Eliminar en el medio actualizando ant y sig

        Movimiento[] adelante = new Movimiento[lista.getTamanio()];
        Movimiento[] atras = new Movimiento[lista.getTamanio()];
        lista.recorrerAdelante(adelante);
        lista.recorrerAtras(atras);

        boolean ok = elimino && lista.getTamanio() == 3
                && adelante[0].getIdMovimiento().equals("MOV-1")
                && adelante[1].getIdMovimiento().equals("MOV-3")
                && adelante[2].getIdMovimiento().equals("MOV-4")
                && atras[0].getIdMovimiento().equals("MOV-4")
                && atras[1].getIdMovimiento().equals("MOV-3")
                && atras[2].getIdMovimiento().equals("MOV-1");

        return new ResultadoPrueba("P14", "ListaDoble (bonificacion): ant/sig adelante y atras", ok,
                "Tras eliminar MOV-2: adelante [1,3,4] y atras [4,3,1] exactos");
    }

    // P15: Simulacion oficial de la semana de Doña Rosa (Tabla 21)
    private static ResultadoPrueba prueba15SimulacionSemanaDonaRosa() {
        SimuladorSemana.ResultadoSimulacion sim = SimuladorSemana.simularCasoDonaRosa();
        boolean ok = sim.kgInventarioInicial == 145
                && sim.valorInventarioInicial == 464000L
                && sim.kgPerdidosCuaderno == 55
                && sim.resultadoNetoCuaderno == -41000L
                && sim.kgPerdidosFefo == 0
                && sim.resultadoNetoFefo == 135000L
                && sim.pesosSalvados == 176000L;

        return new ResultadoPrueba("P15", "Simulacion semana de Doña Rosa (Cuaderno vs FEFO)", ok,
                "Cuaderno: 55 kg perdidos (-$41.000) vs FEFO: 0 kg (+$135.000) -> $176.000 salvados");
    }

    // P16: REGLA ESTRICTA - Actualizar un producto NO modifica ventas ni lotes historicos previos
    private static ResultadoPrueba prueba16InmutabilidadHistoricaAlActualizarProducto() {
        Puesto puesto = new Puesto("Prueba Inmutabilidad", "2026-11-20");
        puesto.agregarProducto("P001", "Mango Tommy", "Fruta", 3200L, 4700L);
        Lote loteAntiguo = puesto.ingresarLote("P001", "L-HIST-1", "2026-11-25", 40);

        // Registrar primera venta con precios originales ($3.200 compra / $4.700 venta)
        Venta ventaAntigua = puesto.registrarVenta("P001", 10, "Rest. La Mulata");

        // Actualizar el producto con nuevos precios ($4.000 compra / $6.000 venta) y nuevo nombre
        puesto.actualizarProducto("P001", "Mango Tommy Premium", "Fruta", 4000L, 6000L);

        // Ingresar lote nuevo (debe tomar el costo NUEVO $4.000)
        Lote loteNuevo = puesto.ingresarLote("P001", "L-NEW-2", "2026-11-29", 20);

        // Realizar segunda venta (consume del lote antiguo a costo $3.200 pero vende al precio NUEVO $6.000)
        Venta ventaNueva = puesto.registrarVenta("P001", 5, "Hotel Caribe");

        boolean lotePrevioIntacto = loteAntiguo.getPrecioCompraKg() == 3200L;
        boolean ventaPreviaIntacta = ventaAntigua.getNombreProductoSnapshot().equals("Mango Tommy")
                && ventaAntigua.getPrecioVentaAplicado() == 4700L
                && ventaAntigua.getTotalVenta() == 47000L
                && ventaAntigua.getGananciaReal() == 15000L;
        boolean nuevosAplicanCambio = loteNuevo.getPrecioCompraKg() == 4000L
                && ventaNueva.getNombreProductoSnapshot().equals("Mango Tommy Premium")
                && ventaNueva.getPrecioVentaAplicado() == 6000L
                && ventaNueva.getTotalVenta() == 30000L;

        boolean ok = lotePrevioIntacto && ventaPreviaIntacta && nuevosAplicanCambio;

        return new ResultadoPrueba("P16", "Inmutabilidad historica al actualizar producto", ok,
                "Venta previa intacta ($4.700, gan. $15.000); lote previo $3.200; nuevo lote $4.000 y venta nueva $6.000");
    }

    // --- 3. Punto de entrada para ejecutar desde terminal ---
    public static void main(String[] args) {
        ResultadoPrueba[] resultados = ejecutarTodas();
        int pasadas = 0;

        System.out.println("=========================================================================");
        System.out.println("  SUITE DE PRUEBAS AUTOMATIZADAS (16 CASOS) - MERCABAZURTO DIGITAL");
        System.out.println("=========================================================================");
        for (int i = 0; i < resultados.length; i++) {
            ResultadoPrueba r = resultados[i];
            String etiqueta = r.paso ? "[PASS]" : "[FAIL]";
            if (r.paso) {
                pasadas = pasadas + 1;
            }
            System.out.println(etiqueta + " " + r.codigo + " - " + r.nombre);
            System.out.println("       " + r.detalle);
        }
        System.out.println("=========================================================================");
        System.out.println("  RESULTADO FINAL: " + pasadas + " / " + resultados.length + " PRUEBAS SUPERADAS");
        System.out.println("=========================================================================");

        if (pasadas != resultados.length) {
            System.exit(1);
        }
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica.
}
