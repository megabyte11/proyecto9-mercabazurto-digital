# MercaBazurto Digital — Proyecto Integrador 9 (Estructuras de Datos en Java 17)

Sistema de gestión de inventario por fecha de vencimiento (**FEFO**), atención de pedidos en orden de llegada y reparto equitativo de domicilios (**Round Robin**) diseñado para los puestos de productos perecederos del **Mercado de Bazurto (Cartagena de Indias)**.

---

## 1. Problema que Resuelve

En los puestos de frutas, verduras y pescados del Mercado de Bazurto (como el puesto de **Doña Rosa**, que abre todos los días a las 4:00 a. m.), llevar el inventario en cuadernos de papel provoca cuatro pérdidas constantes:
1. **Desperdicio por vencimiento:** las canastas nuevas se apilan encima de las viejas y al final de la semana se pierden decenas de kilos de alimento.
2. **Pedidos telefónicos olvidados:** encargos de tiendas y restaurantes anotados en papeles sueltos que se traspapelan en horas pico.
3. **Reparto desigual de domicilios:** se asigna el viaje al mensajero más cercano al mostrador, generando conflictos por carga desigual.
4. **Decisiones de compra a ciegas:** el comerciante no conoce al instante cuánta plata tiene en bodega ni qué producto le deja mayor margen real.

**MercaBazurto Digital** resuelve estos cuatro problemas mediante un núcleo **100 % en Java 17 puro** con estructuras de datos propias (sin usar `ArrayList`, `LinkedList`, `Queue`, `Stack`, `HashMap`, `HashSet`, `Collections.sort` ni `Arrays.sort`) y una interfaz visual local minimalista pensada para operar sin conocimientos técnicos a las 4:00 a. m.

---

## 2. Integrantes y Roles del Equipo

| N.º | Integrante | Rol en el Proyecto | Responsabilidad Principal |
| :-: | :--- | :--- | :--- |
| 1 | **Miguel** | Líder del proyecto y Arquitecto de TDA | Diseño UML, TDA (`Producto`, `Lote`, `Venta`, `Pedido`, `Mensajero`, `Movimiento`, `Puesto`), modularidad, inmutabilidad histórica e integración. |
| 2 | **Rafael** | Desarrollador de arreglos y algoritmos | Carga de CSV con arreglos propios (`CargadorCSV`), `Busquedas` (secuencial, ordenada y binaria), `Ordenamientos` (4 algoritmos) y `ConjuntosArreglos` (`S1 ∩ S2`, `S1 ∪ S2`). |
| 3 | **Jesús** | Desarrollador de listas enlazadas | `Nodo<T>`, `NodoDoble<T>`, `ListaSimpleLotes` (FEFO + recursividad), `ListaSimplePedidos` (`O(1)`), `ListaSimpleVentas`, `ListaCircular` (Round Robin) y `ListaDoble` (bonificación +0,3). |
| 4 | **Juan** | Analista de calidad (QA) y Storyteller | 16 casos de prueba (`PruebasSistema`), `Medidor` Big-O, `SimuladorSemana`, Storybook de 12 escenas (`docs/Storybook_MercaBazurto_Digital.pdf`) y guion de exposición. |

---

## 3. Funcionalidades Principales

### Vista del Comerciante (interfaz minimalista offline en `http://localhost:8080`)
- **Inicio del día:** Total vendido hoy, ganancia neta acumulada, valor total del inventario calculado recursivamente, alertas de lotes próximos a vencer y accesos rápidos de 1 clic.
- **Ventas:** Registro rápido de ventas descontando automáticamente desde el lote que vence primero (`ListaSimpleLotes.venderDesdePrimero`) e historial filtrable con ganancia real.
- **Productos y precios (con protección de historial):** Agregar productos nuevos o actualizar precios vigentes **sin modificar jamás** las ventas anteriores ni los lotes ya ingresados (cada `Venta` y cada `Lote` conserva su snapshot histórico inmutable).
- **Lo que vence primero (FEFO):** Listado por vencimiento con etiquetas visuales claras (*Vencido*, *Próximo a vencer*, *Vigente*), ingreso de lotes en orden exacto por fecha ISO `AAAA-MM-DD` y botón de retiro de vencidos en cabeza (`O(1)`).
- **Pedidos y mensajeros:** Fila de pedidos con punteros `primero`/`ultimo` (`O(1)`), cancelación por código y asignación equitativa Round Robin (`ListaCircular<Mensajero>`) entre Carlos, Dairo y Édgar.
- **Ganancias y clientes frecuentes:** Ranking de rentabilidad seleccionando entre Burbuja, Burbuja Mejorada, Quick Sort o Merge Sort, más clientes frecuentes (`S1 ∩ S2`) y clientes únicos (`S1 ∪ S2`).

### Pestaña Discreta "Auditoría Técnica" (para sustentación en vivo)
- Carga/regeneración en 1 clic de los 3 CSV oficiales (`productos.csv` 200, `lotes.csv` 2.000, `pedidos.csv` 5.000).
- Comparador visual Búsqueda Secuencial vs. Secuencial Ordenada vs. Binaria con traza paso a paso (`200` vs. `8` comparaciones para `P200`).
- Ejecutor en vivo de la clase `Medidor` (`System.nanoTime()` para `n = 200`, `2.000` y `5.000`).
- Simulación interactiva de la semana de ventas de Doña Rosa (`SimuladorSemana`: **55 kg perdidos / `-$41.000`** con cuaderno vs. **0 kg perdidos / `+$135.000`** con FEFO = **`+$176.000 COP` salvados**).
- Visor bidireccional de `ListaDoble<Movimiento>` (recorrer adelante con `sig` y atrás con `ant` — bonificación +0,3).
- Ejecución en vivo de las **16 pruebas automatizadas** con indicador `[PASS]`.

---

## 4. Cómo Compilar y Ejecutar en 1 Paso (Java 17+)

No requiere Maven, Gradle ni internet. Desde la raíz de `proyecto9-mercabazurto-digital/`:

### En Windows (PowerShell)
```powershell
mkdir bin -Force | Out-Null
javac --release 17 -encoding UTF-8 -d bin (Get-ChildItem src -Filter *.java -Recurse).FullName
java -cp bin app.Main
```

### En Linux / macOS (Bash)
```bash
mkdir -p bin
javac --release 17 -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin app.Main
```

Al ejecutar `app.Main`, el programa carga automáticamente los 3 CSV oficiales, arranca el servidor local en **`http://localhost:8080`**, abre el navegador automáticamente y deja activo un **menú interactivo de consola** como respaldo.

### Ejecutar las 16 pruebas automatizadas y el Medidor Big-O por terminal
```powershell
java -cp bin pruebas.PruebasSistema
java -cp bin algoritmos.Medidor
```

---

## 5. Tabla de Complejidad Big-O y Tiempos Reales Medidos (`Medidor`)

Tiempos reales medidos con `System.nanoTime()` en Java (JDK Temurin, compilado con `--release 17`) sobre los tres tamaños oficiales de la guía (`n = 200`, `2.000` y `5.000`):

| Operación / Algoritmo | Complejidad Big-O | Fórmula Teórica | Ops (`n=200`) | Ops (`n=2.000`) | Ops (`n=5.000`) | Tiempo `n = 200` | Tiempo `n = 2.000` | Tiempo `n = 5.000` |
| :--- | :---: | :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| Despachar lote FEFO / Pedido / Turno Round Robin | `O(1)` | `1` cambio de puntero | 1 | 1 | 1 | `0,0001 ms` | `0,0001 ms` | `0,0001 ms` |
| Búsqueda binaria por código (peor caso) | `O(log2 n)` | `⌊log2(n)⌋ + 1` | 8 | 11 | 13 | `0,0102 ms` | `0,0048 ms` | `0,0091 ms` |
| Búsqueda secuencial por código (peor caso) | `O(n)` | `n` comparaciones | 200 | 2.000 | 5.000 | `0,0094 ms` | `0,0352 ms` | `0,2541 ms` |
| Valor recursivo del inventario | `O(n)` | `n` llamadas recursivas | 200 | 2.000 | 5.000 | `0,0038 ms` | `0,0442 ms` | `0,1791 ms` |
| Inserción ordenada de lote (peor caso) | `O(n)` | `n` comparaciones | 200 | 2.000 | 5.000 | `0,1938 ms` | `0,1367 ms` | `0,1283 ms` |
| Burbuja mejorada (mejor caso: ya ordenado) | `O(n)` | `n − 1` comparaciones | 199 | 1.999 | 4.999 | `0,0014 ms` | `0,0179 ms` | `0,1138 ms` |
| Merge Sort (caso promedio) | `O(n log2 n)` | `≈ n · log2(n)` | 1.529 | 21.932 | 61.439 | `0,0427 ms` | `0,5540 ms` | `2,6749 ms` |
| Quick Sort (caso promedio) | `O(n log2 n)` | `≈ n · log2(n)` | 1.529 | 21.932 | 61.439 | `0,0250 ms` | `0,3353 ms` | `1,0198 ms` |
| Burbuja estándar (peor caso) | `O(n²)` | `n(n − 1) / 2` | 19.900 | 1.999.000 | 12.497.500 | `0,3291 ms` | `21,4520 ms` | `182,8747 ms` |

---

## 6. Estructura del Repositorio

```text
proyecto9-mercabazurto-digital/
├── README.md
├── .gitignore
├── docs/
│   ├── Storybook_MercaBazurto_Digital.pdf   (Portada + 12 escenas ilustradas)
│   ├── UML_MercaBazurto_Digital.md          (Diagrama UML Mermaid y TDA)
│   ├── GUION_EXPOSICION.md                  (Guion exacto de 30 min + 8 preguntas)
│   └── COMANDOS_GITHUB.md                   (Guía rápida para subir con 28 commits)
├── datos/
│   ├── productos.csv                        (200 productos ordenados P001–P200)
│   ├── lotes.csv                            (2.000 lotes en formato ISO AAAA-MM-DD)
│   └── pedidos.csv                          (5.000 pedidos de tiendas y restaurantes)
├── web/
│   ├── index.html                           (Interfaz minimalista 100 % offline)
│   ├── styles.css                           (Paleta sobria de mercado y alto contraste)
│   └── app.js                               (Conexión con API REST nativa Java 17)
└── src/
    ├── modelo/                              (Producto, Lote, Venta, Pedido, Mensajero, Movimiento, Puesto)
    ├── estructuras/                         (Nodo, NodoDoble, ListaSimpleLotes, ListaSimplePedidos, ListaSimpleVentas, ListaCircular, ListaDoble)
    ├── algoritmos/                          (CargadorCSV, Busquedas, Ordenamientos, ConjuntosArreglos, Medidor, SimuladorSemana)
    ├── app/                                 (Main, ServidorApp)
    └── pruebas/                             (PruebasSistema — 16/16 pruebas [PASS])
```
