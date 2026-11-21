package algoritmos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Random;
import modelo.Lote;
import modelo.Pedido;
import modelo.Producto;

/*
 * ============================================================================
 * CLASE: CargadorCSV
 * ============================================================================
 * (a) Que hace: Lee los tres archivos oficiales de datos del mercado
 *     (productos.csv con 200 productos, lotes.csv con 2.000 lotes y
 *     pedidos.csv con 5.000 pedidos) usando arreglos propios sin ArrayList.
 *     Ademas puede generar o regenerar dichos archivos CSV con datos
 *     realistas del Mercado de Bazurto en Cartagena.
 * (b) Tema de Estructuras de Datos: Unidad 1 - Modularidad y administracion
 *     de arreglos dinamicos construidos manualmente (temas 1.1 a 1.3).
 * (c) Complejidad Big-O principal: O(n) lineal respecto al numero de filas
 *     leidas o escritas en cada archivo CSV.
 * ============================================================================
 */
public class CargadorCSV {

    // === 1. ATRIBUTOS ===
    public static final int TOTAL_PRODUCTOS_OFICIAL = 200;
    public static final int TOTAL_LOTES_OFICIAL = 2000;
    public static final int TOTAL_PEDIDOS_OFICIAL = 5000;

    // === 2. CONSTRUCTOR ===
    public CargadorCSV() {
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Cargar productos desde productos.csv ---
    // Recibe la ruta del archivo productos.csv; devuelve un arreglo Producto[] exacto.
    public static Producto[] cargarProductos(String rutaArchivo) throws IOException {
        Producto[] buffer = new Producto[256];
        int contador = 0;

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8))) {
            String linea = lector.readLine(); // Saltar encabezado CSV
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length < 5) {
                    continue;
                }

                String codigo = partes[0].trim();
                String nombre = partes[1].trim();
                String categoria = partes[2].trim();
                long precioCompra = Long.parseLong(partes[3].trim());
                long precioVenta = Long.parseLong(partes[4].trim());

                // Si el arreglo se lleno, duplicamos su capacidad manualmente con un bucle for
                if (contador == buffer.length) {
                    Producto[] ampliado = new Producto[buffer.length * 2];
                    for (int i = 0; i < contador; i++) {
                        ampliado[i] = buffer[i];
                    }
                    buffer = ampliado;
                }

                buffer[contador] = new Producto(codigo, nombre, categoria, precioCompra, precioVenta);
                contador = contador + 1;
            }
        }

        // Recortar al numero exacto de productos leidos
        Producto[] resultado = new Producto[contador];
        for (int i = 0; i < contador; i++) {
            resultado[i] = buffer[i];
        }
        return resultado;
    }

    // --- 2. Cargar lotes desde lotes.csv ---
    // Recibe la ruta de lotes.csv; devuelve un arreglo Lote[] con los registros leidos.
    public static Lote[] cargarLotes(String rutaArchivo) throws IOException {
        Lote[] buffer = new Lote[2048];
        int contador = 0;

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8))) {
            String linea = lector.readLine(); // Saltar encabezado CSV
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length < 5) {
                    continue;
                }

                String codigoLote = partes[0].trim();
                String codigoProducto = partes[1].trim();
                String fechaVencimiento = partes[2].trim();
                int cantidadKg = Integer.parseInt(partes[3].trim());
                long precioCompra = Long.parseLong(partes[4].trim());

                if (contador == buffer.length) {
                    Lote[] ampliado = new Lote[buffer.length * 2];
                    for (int i = 0; i < contador; i++) {
                        ampliado[i] = buffer[i];
                    }
                    buffer = ampliado;
                }

                buffer[contador] = new Lote(codigoLote, codigoProducto, fechaVencimiento,
                        cantidadKg, precioCompra);
                contador = contador + 1;
            }
        }

        Lote[] resultado = new Lote[contador];
        for (int i = 0; i < contador; i++) {
            resultado[i] = buffer[i];
        }
        return resultado;
    }

    // --- 3. Cargar pedidos desde pedidos.csv ---
    // Recibe la ruta de pedidos.csv; devuelve un arreglo Pedido[] en orden de llegada.
    public static Pedido[] cargarPedidos(String rutaArchivo) throws IOException {
        Pedido[] buffer = new Pedido[5120];
        int contador = 0;

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaArchivo), StandardCharsets.UTF_8))) {
            String linea = lector.readLine(); // Saltar encabezado CSV
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length < 6) {
                    continue;
                }

                String codigoPedido = partes[0].trim();
                String cliente = partes[1].trim();
                String codigoProducto = partes[2].trim();
                int cantidadKg = Integer.parseInt(partes[3].trim());
                int semana = Integer.parseInt(partes[4].trim());
                String fechaHora = partes[5].trim();

                if (contador == buffer.length) {
                    Pedido[] ampliado = new Pedido[buffer.length * 2];
                    for (int i = 0; i < contador; i++) {
                        ampliado[i] = buffer[i];
                    }
                    buffer = ampliado;
                }

                buffer[contador] = new Pedido(codigoPedido, cliente, codigoProducto,
                        cantidadKg, semana, fechaHora);
                contador = contador + 1;
            }
        }

        Pedido[] resultado = new Pedido[contador];
        for (int i = 0; i < contador; i++) {
            resultado[i] = buffer[i];
        }
        return resultado;
    }

    // --- 4. Generar o regenerar los 3 archivos CSV oficiales ---
    // Recibe la carpeta destino (ej. "datos"); crea productos.csv (200), lotes.csv (2.000) y pedidos.csv (5.000).
    public static void generarArchivosOficiales(String carpetaDatos) throws IOException {
        File dir = new File(carpetaDatos);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Producto[] productos = generarCatalogoOficial200();
        escribirProductosCSV(new File(dir, "productos.csv").getPath(), productos);
        escribirLotesCSV(new File(dir, "lotes.csv").getPath(), productos);
        escribirPedidosCSV(new File(dir, "pedidos.csv").getPath(), productos);
    }

    // Construye exactamente 200 productos ordenados de P001 a P200 con precios reales de Bazurto.
    private static Producto[] generarCatalogoOficial200() {
        Producto[] catalogo = new Producto[TOTAL_PRODUCTOS_OFICIAL];

        // Los 5 productos oficiales del Plan de Negocio y la Guia
        catalogo[0] = new Producto("P001", "Mango Tommy", "Fruta", 3200, 4700);
        catalogo[1] = new Producto("P002", "Pargo Rojo", "Pescado", 18000, 24000);
        catalogo[2] = new Producto("P003", "Tomate Chonto", "Verdura", 2500, 3300);
        catalogo[3] = new Producto("P004", "Aguacate Papelillo", "Fruta", 5000, 7500);
        catalogo[4] = new Producto("P005", "Sierra Fresca", "Pescado", 14000, 18500);

        // Catalogo base de perecederos tipicos del Mercado de Bazurto (Cartagena)
        String[] frutasBase = {
            "Mango de Azucar", "Papaya Maradol", "Piña Oro Miel", "Patilla Rayada",
            "Guayaba Pera", "Maracuya Criollo", "Limon Tahini", "Naranja Tangelo",
            "Banano Uraba", "Platano Harton Maduro", "Melon Cantalupo", "Lulo Seleccion",
            "Mora de Castilla", "Guanabana Fresca", "Zapote Costeño", "Nispero de Turbaco",
            "Tamarindo Pulpa", "Coco Biche", "Corozo Fresco", "Uva Red Globe",
            "Manzana Roja", "Pera Verde", "Fresa Sabana", "Mandarina Arrayana",
            "Ciruela Criolla"
        };

        String[] verdurasBase = {
            "Cebolla Roja Ocañera", "Cebolla Blanca", "Aji Topito Dulce", "Pimenton Rojo",
            "Zanahoria Bogotana", "Papa Pastusa", "Papa Criolla", "Yuca Venezolana",
            "Ñame Espino", "Ñame Diamante", "Ahuyama Criolla", "Pepino Cohombro",
            "Lechuga Batavia", "Repollo Blanco", "Cilantro Fresco", "Cebollin Largo",
            "Berenjena Morada", "Habichuela Tierna", "Remolacha Roja", "Ajo Nacional",
            "Platano Verde", "Mazorca Tierna", "Espinaca Hoja", "Tomate Milano",
            "Apio Rama"
        };

        String[] pescadosBase = {
            "Mojarra Roja", "Mojarra Negra", "Robalo Fresco", "Lebranche Entero",
            "Bocachico de Rio", "Bagre Pintado", "Jurel Fresco", "Atun en Posta",
            "Corvina Blanca", "Camaron Tigre", "Langostino Entero", "Calamar Limpio",
            "Caracol de Pala", "Cojinua Fresca", "Pargo Pluma", "Mero en Filete",
            "Sardina Plateada", "Pulpo Fresco", "Tilapia Roja", "Trucha Arco Iris",
            "Cachama Fresca", "Sabalete Costeño", "Bonito Entero", "Pez Espada Posta",
            "Lisa de Mar"
        };

        String[] calidades = {"Extra", "Primera", "Mayorista", "Seleccion"};
        Random rnd = new Random(20261120L);

        int idx = 5;
        int ronda = 0;
        while (idx < TOTAL_PRODUCTOS_OFICIAL) {
            String categoria;
            String nombreBase;
            long compra;
            long ganancia;

            int tipo = idx % 3;
            if (tipo == 0) {
                categoria = "Fruta";
                nombreBase = frutasBase[(idx / 3) % frutasBase.length];
                compra = 2000L + (rnd.nextInt(38) * 200L);
                ganancia = 700L + (rnd.nextInt(18) * 150L);
            } else if (tipo == 1) {
                categoria = "Verdura";
                nombreBase = verdurasBase[(idx / 3) % verdurasBase.length];
                compra = 1600L + (rnd.nextInt(26) * 200L);
                ganancia = 500L + (rnd.nextInt(14) * 150L);
            } else {
                categoria = "Pescado";
                nombreBase = pescadosBase[(idx / 3) % pescadosBase.length];
                compra = 9000L + (rnd.nextInt(32) * 500L);
                ganancia = 2500L + (rnd.nextInt(18) * 300L);
            }

            ronda = idx / 75;
            String nombreFinal = ronda == 0 ? nombreBase : (nombreBase + " " + calidades[(idx + ronda) % calidades.length]);
            String codigo = String.format("P%03d", idx + 1);
            catalogo[idx] = new Producto(codigo, nombreFinal, categoria, compra, compra + ganancia);
            idx = idx + 1;
        }

        return catalogo;
    }

    // Escribe productos.csv con codificacion UTF-8.
    private static void escribirProductosCSV(String ruta, Producto[] productos) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
            escritor.write("codigo,nombre,categoria,precioCompraKg,precioVentaKg");
            escritor.newLine();
            for (int i = 0; i < productos.length; i++) {
                Producto p = productos[i];
                escritor.write(p.getCodigo() + "," + p.getNombre() + "," + p.getCategoria()
                        + "," + p.getPrecioCompraKg() + "," + p.getPrecioVentaKg());
                escritor.newLine();
            }
        }
    }

    // Escribe lotes.csv con exactamente 2.000 lotes en formato ISO AAAA-MM-DD.
    private static void escribirLotesCSV(String ruta, Producto[] productos) throws IOException {
        Random rnd = new Random(20261121L);
        LocalDate baseHoy = LocalDate.of(2026, 11, 20);

        try (BufferedWriter escritor = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
            escritor.write("codigoLote,codigoProducto,fechaVencimiento,cantidadKg,precioCompraKg");
            escritor.newLine();

            // Primeros 5 lotes emblematicos de Doña Rosa para P001 (Mango Tommy)
            // Se escriben en orden desordenado para demostrar que ListaSimpleLotes los ordena al cargar
            escritor.write("L-101,P001,2026-11-24,40,3200");
            escritor.newLine();
            escritor.write("L-102,P001,2026-11-22,25,3200");
            escritor.newLine();
            escritor.write("L-103,P001,2026-11-28,50,3200");
            escritor.newLine();
            escritor.write("L-100,P001,2026-11-18,10,3200");
            escritor.newLine();
            escritor.write("L-104,P001,2026-11-25,30,3200");
            escritor.newLine();

            // Restantes 1.995 lotes repartidos entre P002 y P200 (total = 2.000 exactos)
            for (int i = 5; i < TOTAL_LOTES_OFICIAL; i++) {
                int idxProd = 1 + ((i - 5) % (productos.length - 1)); // P002..P200
                Producto prod = productos[idxProd];

                // Mezcla realista: ~4% vencidos (ayer/antier), ~20% proximos (0 a 3 dias), ~76% vigentes
                int sorteado = rnd.nextInt(100);
                int diasDelta;
                if (sorteado < 4) {
                    diasDelta = -1 - rnd.nextInt(2); // 2026-11-18 o 2026-11-19
                } else if (sorteado < 24) {
                    diasDelta = 1 + rnd.nextInt(3);  // 2026-11-21 a 2026-11-23
                } else {
                    diasDelta = 4 + rnd.nextInt(18); // 2026-11-24 a 2026-12-11
                }

                String fechaVenc = baseHoy.plusDays(diasDelta).toString();
                int cantidadKg = 10 + (rnd.nextInt(12) * 5); // 10 a 65 kg
                String codigoLote = String.format("L-%04d", i + 200);

                escritor.write(codigoLote + "," + prod.getCodigo() + "," + fechaVenc
                        + "," + cantidadKg + "," + prod.getPrecioCompraKg());
                escritor.newLine();
            }
        }
    }

    // Escribe pedidos.csv con exactamente 5.000 pedidos de tenderos y restaurantes.
    private static void escribirPedidosCSV(String ruta, Producto[] productos) throws IOException {
        Random rnd = new Random(20261122L);
        String[] clientesSemana1 = {
            "C01 (Rest. La Mulata)", "C02 (Tienda El Sol)", "C03 (Hotel Caribe)",
            "C04 (Comedor Bazurto)", "C05 (Tienda San Diego)", "C08 (Rest. La Cevicheria)",
            "C09 (Fruver Bocagrande)", "C10 (Asadero El Prado)", "C11 (Tienda Pie de la Popa)",
            "C12 (Hotel Santa Clara)"
        };
        String[] clientesSemana2 = {
            "C02 (Tienda El Sol)", "C04 (Comedor Bazurto)", "C05 (Tienda San Diego)",
            "C06 (Rest. El Muelle)", "C07 (Fruver Manga)", "C09 (Fruver Bocagrande)",
            "C13 (Rest. Coroncoro)", "C14 (Tienda Los Ejecutivos)", "C15 (Fruver Santa Lucia)",
            "C16 (Comedor La Castellana)"
        };

        try (BufferedWriter escritor = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
            escritor.write("codigoPedido,cliente,codigoProducto,cantidadKg,semana,fechaHora");
            escritor.newLine();

            // Primeros 4 pedidos emblematicos del Plan de Negocio (Seccion 15.2)
            escritor.write("PED-0001,C01 (Rest. La Mulata),P001,35,1,2026-11-20 05:00");
            escritor.newLine();
            escritor.write("PED-0002,C02 (Tienda El Sol),P003,15,1,2026-11-20 05:06");
            escritor.newLine();
            escritor.write("PED-0003,C03 (Hotel Caribe),P002,20,1,2026-11-20 05:12");
            escritor.newLine();
            escritor.write("PED-0004,C04 (Comedor Bazurto),P004,10,1,2026-11-20 05:20");
            escritor.newLine();

            // Restantes 4.996 pedidos hasta completar exactamente 5.000
            for (int i = 4; i < TOTAL_PEDIDOS_OFICIAL; i++) {
                int semana = (i % 2 == 0) ? 1 : 2;
                String cliente = (semana == 1)
                        ? clientesSemana1[rnd.nextInt(clientesSemana1.length)]
                        : clientesSemana2[rnd.nextInt(clientesSemana2.length)];
                Producto prod = productos[rnd.nextInt(productos.length)];
                int kg = 5 + (rnd.nextInt(10) * 5); // 5 a 50 kg
                int hora = 4 + (i % 10);
                int minuto = (i * 7) % 60;
                String fechaHora = String.format("2026-11-20 %02d:%02d", hora, minuto);
                String codigoPedido = String.format("PED-%04d", i + 1);

                escritor.write(codigoPedido + "," + cliente + "," + prod.getCodigo()
                        + "," + kg + "," + semana + "," + fechaHora);
                escritor.newLine();
            }
        }
    }

    // === 4. GETTERS Y SETTERS ===
    // No aplica.
}
