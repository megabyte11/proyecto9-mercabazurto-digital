package algoritmos;

import estructuras.ListaCircular;
import estructuras.ListaSimpleLotes;
import modelo.Lote;
import modelo.Mensajero;

/*
 * ============================================================================
 * CLASE: SimuladorSemana
 * ============================================================================
 * (a) Que hace: Reproduce una semana real de ventas de Mango Tommy en el
 *     puesto de Doña Rosa (145 kg distribuidos en 4 lotes, demanda de 90 kg)
 *     comparando el metodo tradicional en cuaderno (apilar fruta nueva sobre
 *     la vieja) contra MercaBazurto Digital (ListaSimpleLotes FEFO + turnos
 *     Round Robin). Cuantifica exactamente kilos perdidos y pesos salvados.
 * (b) Tema de Estructuras de Datos: Integracion de Unidad 1 (TDA, recursividad),
 *     Unidad 3 (ListaSimpleLotes ordenada FEFO y ListaCircular Round Robin).
 * (c) Complejidad Big-O principal: O(n) para insertar lotes ordenados y
 *     calcular valor recursivo; O(1) por lote despachado y turno de mensajero.
 * ============================================================================
 */
public class SimuladorSemana {

    // Clase resultado con todos los indicadores de la Tabla 21 del Plan de Negocio
    public static class ResultadoSimulacion {
        public final int kgInventarioInicial;
        public final long valorInventarioInicial;
        public final int demandaKg;
        public final long costoKg;
        public final long precioVentaKg;
        public final long margenKg;

        // Escenario A: Cuaderno (apila nuevo sobre viejo)
        public final int kgVendidosCuaderno;
        public final int kgPerdidosCuaderno;
        public final long costoBotadoCuaderno;
        public final long gananciaBrutaCuaderno;
        public final long resultadoNetoCuaderno;
        public final String detalleDespachoCuaderno;
        public final String detalleVencidosCuaderno;

        // Escenario B: MercaBazurto Digital (ListaSimpleLotes FEFO)
        public final int kgVendidosFefo;
        public final int kgPerdidosFefo;
        public final long costoBotadoFefo;
        public final long gananciaBrutaFefo;
        public final long resultadoNetoFefo;
        public final String detalleDespachoFefo;
        public final String detalleSobranteFefo;

        // Impacto neto
        public final int kgSalvados;
        public final long pesosSalvados;
        public final String resumenTurnosMensajeros;

        public ResultadoSimulacion(int kgInventarioInicial, long valorInventarioInicial,
                                   int demandaKg, long costoKg, long precioVentaKg, long margenKg,
                                   int kgVendidosCuaderno, int kgPerdidosCuaderno,
                                   long costoBotadoCuaderno, long gananciaBrutaCuaderno,
                                   long resultadoNetoCuaderno, String detalleDespachoCuaderno,
                                   String detalleVencidosCuaderno,
                                   int kgVendidosFefo, int kgPerdidosFefo,
                                   long costoBotadoFefo, long gananciaBrutaFefo,
                                   long resultadoNetoFefo, String detalleDespachoFefo,
                                   String detalleSobranteFefo,
                                   int kgSalvados, long pesosSalvados,
                                   String resumenTurnosMensajeros) {
            this.kgInventarioInicial = kgInventarioInicial;
            this.valorInventarioInicial = valorInventarioInicial;
            this.demandaKg = demandaKg;
            this.costoKg = costoKg;
            this.precioVentaKg = precioVentaKg;
            this.margenKg = margenKg;
            this.kgVendidosCuaderno = kgVendidosCuaderno;
            this.kgPerdidosCuaderno = kgPerdidosCuaderno;
            this.costoBotadoCuaderno = costoBotadoCuaderno;
            this.gananciaBrutaCuaderno = gananciaBrutaCuaderno;
            this.resultadoNetoCuaderno = resultadoNetoCuaderno;
            this.detalleDespachoCuaderno = detalleDespachoCuaderno;
            this.detalleVencidosCuaderno = detalleVencidosCuaderno;
            this.kgVendidosFefo = kgVendidosFefo;
            this.kgPerdidosFefo = kgPerdidosFefo;
            this.costoBotadoFefo = costoBotadoFefo;
            this.gananciaBrutaFefo = gananciaBrutaFefo;
            this.resultadoNetoFefo = resultadoNetoFefo;
            this.detalleDespachoFefo = detalleDespachoFefo;
            this.detalleSobranteFefo = detalleSobranteFefo;
            this.kgSalvados = kgSalvados;
            this.pesosSalvados = pesosSalvados;
            this.resumenTurnosMensajeros = resumenTurnosMensajeros;
        }
    }

    // === 1. ATRIBUTOS ===
    public static final String FECHA_CORTE_VIERNES = "2026-11-15";
    public static final long COSTO_MANGO_KG = 3200L;
    public static final long VENTA_MANGO_KG = 4700L;
    public static final int DEMANDA_OFICIAL_KG = 90;

    // === 2. CONSTRUCTOR ===
    public SimuladorSemana() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Ejecutar caso oficial de Doña Rosa (145 kg en 4 lotes, demanda 90 kg) ---
    // No recibe parametros; corre la simulacion oficial verificada de la Tabla 21.
    public static ResultadoSimulacion simularCasoDonaRosa() {
        return simularConDemanda(DEMANDA_OFICIAL_KG);
    }

    // --- 2. Ejecutar simulacion con demanda configurable ---
    // Recibe los kilos demandados durante la semana; compara Cuaderno vs. MercaBazurto FEFO.
    public static ResultadoSimulacion simularConDemanda(int demandaKg) {
        if (demandaKg < 0) {
            demandaKg = 0;
        }
        long margenKg = VENTA_MANGO_KG - COSTO_MANGO_KG; // $1.500/kg

        // ====================================================================
        // ESCENARIO B: MERCABAZURTO DIGITAL (ListaSimpleLotes ordenada FEFO)
        // ====================================================================
        // Paso 1: Ingresar los 4 lotes de Doña Rosa en orden de llegada desordenado
        // (L-101, L-102, L-103, L-104); la lista los acomoda automaticamente por fecha:
        // L-102 (25 kg, 12-nov) -> L-101 (40 kg, 14-nov) -> L-104 (30 kg, 15-nov) -> L-103 (50 kg, 18-nov)
        ListaSimpleLotes listaFefo = new ListaSimpleLotes();
        listaFefo.insertarOrdenado(new Lote("L-101", "P001", "2026-11-14", 40, COSTO_MANGO_KG));
        listaFefo.insertarOrdenado(new Lote("L-102", "P001", "2026-11-12", 25, COSTO_MANGO_KG));
        listaFefo.insertarOrdenado(new Lote("L-103", "P001", "2026-11-18", 50, COSTO_MANGO_KG));
        listaFefo.insertarOrdenado(new Lote("L-104", "P001", "2026-11-15", 30, COSTO_MANGO_KG));

        int kgInicial = listaFefo.calcularKgTotales(); // 145 kg
        long valorInicial = listaFefo.calcularValorRecursivo(); // $464.000

        // Paso 2: Despachar la demanda semanal siempre desde el primero de la cabeza FEFO
        ListaSimpleLotes.ResultadoVentaLotes ventaFefo = listaFefo.venderDesdePrimeroConDetalle(demandaKg);
        int kgVendidosFefo = ventaFefo.kgDespachados;

        // Paso 3: Al llegar el viernes 2026-11-15, retirar los lotes ya vencidos (< 2026-11-15)
        int kgPerdidosFefo = listaFefo.retirarVencidos(FECHA_CORTE_VIERNES);
        long costoBotadoFefo = (long) kgPerdidosFefo * COSTO_MANGO_KG;
        long gananciaBrutaFefo = (long) kgVendidosFefo * margenKg;
        long resultadoNetoFefo = gananciaBrutaFefo - costoBotadoFefo;

        StringBuilder sobranteFefo = new StringBuilder();
        Lote[] restantesFefo = listaFefo.aArreglo();
        for (int i = 0; i < restantesFefo.length; i++) {
            if (i > 0) {
                sobranteFefo.append(", ");
            }
            sobranteFefo.append(restantesFefo[i].getCodigoLote())
                    .append(" (").append(restantesFefo[i].getCantidadKg())
                    .append(" kg, vence ").append(restantesFefo[i].getFechaVencimiento()).append(")");
        }
        if (sobranteFefo.length() == 0) {
            sobranteFefo.append("Sin stock sobrante");
        }

        // ====================================================================
        // ESCENARIO A: CUADERNO TRADICIONAL (apila nuevo sobre viejo)
        // ====================================================================
        // Los ayudantes despachan desde arriba de la pila: primero el mas reciente (L-103),
        // luego L-104, luego L-101 y por ultimo el mas viejo L-102 que quedo atrapado abajo.
        Lote[] pilaCuaderno = new Lote[4];
        pilaCuaderno[0] = new Lote("L-103", "P001", "2026-11-18", 50, COSTO_MANGO_KG);
        pilaCuaderno[1] = new Lote("L-104", "P001", "2026-11-15", 30, COSTO_MANGO_KG);
        pilaCuaderno[2] = new Lote("L-101", "P001", "2026-11-14", 40, COSTO_MANGO_KG);
        pilaCuaderno[3] = new Lote("L-102", "P001", "2026-11-12", 25, COSTO_MANGO_KG);

        int restanteDemanda = demandaKg;
        StringBuilder despachoCuaderno = new StringBuilder();
        for (int i = 0; i < pilaCuaderno.length && restanteDemanda > 0; i++) {
            int disp = pilaCuaderno[i].getCantidadKg();
            int tomado = disp <= restanteDemanda ? disp : restanteDemanda;
            pilaCuaderno[i].setCantidadKg(disp - tomado);
            restanteDemanda = restanteDemanda - tomado;
            if (despachoCuaderno.length() > 0) {
                despachoCuaderno.append(", ");
            }
            despachoCuaderno.append(pilaCuaderno[i].getCodigoLote())
                    .append(" (").append(tomado).append(" kg)");
        }
        int kgVendidosCuaderno = demandaKg - restanteDemanda;

        // Revisar el viernes 2026-11-15 que lotes quedaron vencidos en el fondo de la bodega
        int kgPerdidosCuaderno = 0;
        StringBuilder vencidosCuaderno = new StringBuilder();
        for (int i = pilaCuaderno.length - 1; i >= 0; i--) {
            if (pilaCuaderno[i].getCantidadKg() > 0 && pilaCuaderno[i].estaVencido(FECHA_CORTE_VIERNES)) {
                kgPerdidosCuaderno = kgPerdidosCuaderno + pilaCuaderno[i].getCantidadKg();
                if (vencidosCuaderno.length() > 0) {
                    vencidosCuaderno.append(" + ");
                }
                vencidosCuaderno.append(pilaCuaderno[i].getCantidadKg())
                        .append(" kg de ").append(pilaCuaderno[i].getCodigoLote())
                        .append(" (").append(pilaCuaderno[i].getFechaVencimiento()).append(")");
            }
        }
        if (vencidosCuaderno.length() == 0) {
            vencidosCuaderno.append("Ningun lote vencido");
        }

        long costoBotadoCuaderno = (long) kgPerdidosCuaderno * COSTO_MANGO_KG;
        long gananciaBrutaCuaderno = (long) kgVendidosCuaderno * margenKg;
        long resultadoNetoCuaderno = gananciaBrutaCuaderno - costoBotadoCuaderno;

        // ====================================================================
        // TURNOS ROUND ROBIN PARA LOS 3 PEDIDOS DE RESTAURANTES
        // ====================================================================
        ListaCircular<Mensajero> mensajeros = new ListaCircular<>();
        Mensajero m1 = new Mensajero("M1", "Carlos", "300-411-2201");
        Mensajero m2 = new Mensajero("M2", "Dairo", "301-522-3302");
        Mensajero m3 = new Mensajero("M3", "Édgar", "302-633-4403");
        mensajeros.insertar(m1);
        mensajeros.insertar(m2);
        mensajeros.insertar(m3);

        Mensajero t1 = mensajeros.siguienteTurno();
        t1.registrarViaje("Rest. La Mulata");
        Mensajero t2 = mensajeros.siguienteTurno();
        t2.registrarViaje("Hotel Caribe");
        Mensajero t3 = mensajeros.siguienteTurno();
        t3.registrarViaje("Comedor Bazurto");

        String resumenTurnos = t1.getNombre() + " (1 viaje), " + t2.getNombre()
                + " (1 viaje), " + t3.getNombre() + " (1 viaje)";

        int kgSalvados = kgPerdidosCuaderno - kgPerdidosFefo;
        long pesosSalvados = resultadoNetoFefo - resultadoNetoCuaderno;

        return new ResultadoSimulacion(
                kgInicial, valorInicial, demandaKg, COSTO_MANGO_KG, VENTA_MANGO_KG, margenKg,
                kgVendidosCuaderno, kgPerdidosCuaderno, costoBotadoCuaderno,
                gananciaBrutaCuaderno, resultadoNetoCuaderno,
                despachoCuaderno.toString(), vencidosCuaderno.toString(),
                kgVendidosFefo, kgPerdidosFefo, costoBotadoFefo,
                gananciaBrutaFefo, resultadoNetoFefo,
                ventaFefo.detalleLotes, sobranteFefo.toString(),
                kgSalvados, pesosSalvados, resumenTurnos
        );
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica.
}
