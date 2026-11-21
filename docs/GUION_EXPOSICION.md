# Guion Oficial de Exposición (30 minutos exactos) — MercaBazurto Digital

> **Institución:** Universidad Mayor de Cartagena / Institución Universitaria Colegio Mayor de Bolívar  
> **Asignatura:** Estructuras de Datos (Java 17) — Proyecto Integrador Corte 3  
> **Docente:** Ing. Odymar Augusto Pardo Echeverría  
> **Fecha de sustentación:** Sábado 21 de noviembre de 2026 (30 minutos cronometrados)

## Equipo Expositor y Reparto de Tiempos

| Integrante | Rol | Bloques asignados | Tiempo estimado |
| :--- | :--- | :--- | :--- |
| **Juan Diego Tobio Villadiego** | Analista de Calidad (QA) y Storyteller | Bloque 1 (Storybook Escenas 1–5) + Bloque 4 (Eficiencia `Medidor` y 16 pruebas) | ~8 min |
| **Miguel Andrés Baza Contreras** | Líder del proyecto y Arquitecto de TDA | Bloque 1 (cierre negocio 35 s) + Bloque 2 (Arquitectura visual del programa y TDA) + Cierre Storybook | ~7 min |
| **Jesús Manuel Mendoza Gamarra** | Desarrollador de Listas Enlazadas | Bloque 3 (Demo en vivo: Ventas FEFO, Lotes, Retiro vencidos, Pedidos, Round Robin y ListaDoble) | ~5,5 min |
| **Rafael Enrique Muñoz Devoz** | Desarrollador de Arreglos y Algoritmos | Bloque 3 (Demo en vivo: Productos/precios inmutables, Búsqueda binaria, 4 Ordenamientos y Conjuntos) | ~4,5 min |
| **Todo el equipo (4 integrantes)** | Defensa abierta ante el docente | Bloque 5 (Preguntas del jurado) | 5 min |

> **Regla de oro para los 4 expositores (alineada con el audio del profesor):**  
> - **NO leer sintaxis ni código Java línea por línea** (salvo que el profesor pida abrir un archivo específico en las preguntas).  
> - **NO profundizar en el Plan de Negocio escrito** — solo 30–45 segundos mencionando qué es *MercaBazurto Digital* como negocio tecnológico.  
> - **Explicar el PROGRAMA en acción:** qué hace cada pantalla para un comerciante real de Bazurto que no sabe de sistemas, qué problema cotidiano resuelve cada botón y qué estructura de datos propia trabaja por detrás en lenguaje natural y claro.

---

## BLOQUE 1 — Storybook: El Problema Real en Bazurto (Minutos 0:00 – 5:00)

*Proyectar `docs/Storybook_MercaBazurto_Digital.pdf` en pantalla completa.*

### 0:00 – 4:15 | Juan Diego Tobio Villadiego (Portada y Escenas 1 a 5)

- **Portada y Escena 1 (`MercaBazurto Digital`):**  
  > "Buenos días profesor y compañeros. Hoy vamos a contarles cómo aplicamos las estructuras de datos que aprendimos este semestre en Java para resolver un problema real que ocurre cada madrugada aquí en Cartagena, en el Mercado de Bazurto. Nuestro proyecto se llama **MercaBazurto Digital**."

- **Escena 2 (4:00 a. m. — Doña Rosa abre su puesto):**  
  > "Ella es **Doña Rosa**, una comerciante de 52 años que lleva 20 años vendiendo frutas en el Mercado de Bazurto. Todos los días a las 4:00 de la mañana, apenas despunta el sol, se pone su delantal naranja y abre su puesto para surtir tiendas y restaurantes de toda la ciudad."

- **Escena 3 (Camiones de abasto — se apila lo nuevo sobre lo viejo):**  
  > "El problema empieza cuando llegan los camiones de abasto. Con el afán de la madrugada, los ayudantes apilan las canastas de mango recién llegadas **justo encima** de las canastas que entraron hace cinco días. El producto más antiguo queda atrapado abajo y nadie lleva control de fechas."

- **Escena 4 (Viernes por la tarde — el desastre):**  
  > "Cuando llega el viernes por la tarde, ocurre el golpe al bolsillo: al mover las cajas del fondo, Doña Rosa descubre que tiene que botar **tres canastas completas de mango dañado**. Y como anota todo en papeles sueltos manchados de pulpa, ese mismo día se le traspapela y olvida un pedido crítico de un restaurante. Además, reparte los domicilios al mensajero que esté más cerca, generando peleas entre sus tres domiciliarios."

- **Escena 5 (La pregunta problema en la pizarra):**  
  > "Frente a esas cajas arruinadas nace la pregunta problema que guía toda nuestra ingeniería: **¿Cómo vender primero lo que vence primero y entregar cada pedido a tiempo en un puesto de perecederos del mercado?**"

### 4:15 – 5:00 | Miguel Andrés Baza Contreras (Escena 6 y encuadre breve del negocio — 40 segundos)

- **Escena 6 (Los cuatro estudiantes llegan a Bazurto):**  
  > "Ahí entramos nosotros cuatro —Miguel, Rafael, Jesús y Juan Diego—, estudiantes de Ingeniería Informática de la Universidad Mayor de Cartagena. También estructuramos esto como un negocio llamado **MercaBazurto Digital**, cuyo propósito básicamente es aplicar nuestros conocimientos como ingenieros resolviendo problemas algorítmicos para automatizar procesos reales basados en estructuras de datos. Como el plan de negocio ya está entregado por escrito, hoy nos vamos a concentrar en el corazón de la materia: **cómo resolvimos el problema con nuestras estructuras de datos propias y cómo funciona el programa paso a paso.**"

---

## BLOQUE 2 — Diseño de la Solución y Estructuras de Datos (Minutos 5:00 – 10:00)

*Pasar brevemente por Escenas 7, 8 y 9 del Storybook y proyectar `docs/UML_MercaBazurto_Digital.md` (1 minuto) para conectar cada problema cotidiano con su estructura.*

### 5:00 – 10:00 | Miguel Andrés Baza Contreras (apoyado por gestos hacia el Storybook y el diagrama UML)

- **Filosofía del programa (5:00 – 5:45):**  
  > "Lo primero que entendimos al hablar con comerciantes de Bazurto es que **el programa NO podía parecer un programa de sistemas** ni un software contable lleno de términos raros. Doña Rosa sabe usar WhatsApp, pero a las 4:00 a. m. necesita botones grandes, claros y lenguaje de puesto de mercado: *'Registrar venta'*, *'Lo que vence primero'*, *'Pedidos y mensajeros'*. Por eso diseñamos una interfaz local muy limpia, sin internet, respaldada 100 % por un núcleo en **Java 17 puro** donde prohibimos totalmente `ArrayList`, `LinkedList`, `Queue`, `HashMap` y `Arrays.sort`: todo se construyó desde cero con nodos, punteros propios y arreglos."

- **Cómo cada estructura ataca un síntoma real (5:45 – 9:15):**
  1. **Escena 7 — Inventario ordenado por vencimiento (`ListaSimpleLotes`):**  
     > "Para que Doña Rosa nunca vuelva a botar fruta, cada producto tiene su propia **lista enlazada simple ordenada por fecha de vencimiento** —como explica Jesús en la escena 7: `L-102` (12 nov) va adelante, seguido de `L-101` (14 nov), `L-104` (15 nov) y `L-103` (18 nov). Cuando entra un lote nuevo, la lista lo acomoda en el lugar exacto de su fecha. Cuando se vende, siempre se descuenta del primer nodo de la cabeza (regla FEFO: primero vence, primero sale). Y si algún lote ya venció, se retira desde la cabeza en tiempo constante `O(1)`."
  2. **Escena 8 — Catálogo rápido y rentabilidad (Arreglo ordenado, Búsqueda Binaria, Recursividad y 4 Ordenamientos):**  
     > "Para manejar 200 productos sin perder tiempo, el catálogo vive en un **arreglo propio ordenado por código** (`P001` a `P200`). Así, buscar un producto con **búsqueda binaria** toma máximo 8 comparaciones en vez de 200. El valor total de la plata en bodega se suma con **recursividad** recorriendo cada nodo, y el ranking de qué producto deja más ganancia se ordena comparando **Burbuja, Burbuja Mejorada, Quick Sort y Merge Sort**. Además, para saber qué tiendas compran todas las semanas, aplicamos **intersección y unión de conjuntos** sobre arreglos propios."
  3. **Escena 9 — Pedidos en orden y domicilios justos (`ListaSimplePedidos` y `ListaCircular`):**  
     > "Los pedidos telefónicos de restaurantes entran al final y salen al inicio usando una **lista simple con dos punteros (`primero` y `ultimo`)**, logrando registrar y despachar en `O(1)` sin usar `Queue`. Y para los tres mensajeros —Carlos, Dairo y Édgar—, implementamos una **lista circular con Round Robin**: cada despacho mueve un solo puntero en `O(1)` y garantiza matemáticamente que nadie haga más de un viaje de diferencia frente a otro."
  4. **Regla de protección histórica y bonificación `ListaDoble` (9:15 – 10:00):**  
     > "Finalmente agregamos dos garantías de ingeniería real: primero, **inmutabilidad histórica**, es decir, si Doña Rosa cambia hoy el precio del mango, todas las ventas anteriores y lotes ya comprados conservan su foto original de precios; y segundo, como componente de bonificación, una **lista doblemente enlazada (`ListaDoble`)** con punteros `ant` y `sig` para auditar los movimientos del día hacia adelante o hacia atrás."

---

## BLOQUE 3 — Demostración en Vivo del Programa (Minutos 10:00 – 20:00)

*Cambiar al navegador con `http://localhost:8080` ya abierto (arrancado desde `Main.java` con los 3 CSV oficiales cargados: 200 productos, 2.000 lotes y 5.000 pedidos).*

### Parte A (10:00 – 15:30) | Jesús Manuel Mendoza Gamarra — Operación diaria con Listas Enlazadas

1. **Pantalla `Inicio del día` (10:00 – 10:45):**  
   - Señalar los 4 indicadores superiores: **Total vendido hoy**, **Ganancia neta hoy**, **Plata en bodega** (calculada recursivamente sobre los 2.000 lotes reales del CSV) y **Lotes por revisar**.
   - Decir: *"A las 4:00 a. m. Doña Rosa abre esto y sin saber nada de informática ya ve cuánto vendió, cuánta plata tiene en mercancía y en la tabla de la izquierda ve inmediatamente qué canastas vencen primero."*

2. **Pantalla `Lo que vence primero` — `ListaSimpleLotes` FEFO (10:45 – 12:15):**  
   - Filtrar por **`P001 — Mango Tommy`** para mostrar en vivo los lotes cargados en orden automático por fecha (`L-100` vencido, `L-102`, `L-101`, `L-104`, `L-103`).
   - Hacer clic en **"Retirar lotes vencidos"**: explicar que el programa desconecta los vencidos directamente desde la cabeza de cada lista en `O(1)` y limpia el inventario disponible.
   - Ingresar un lote nuevo de `P001` con fecha **`2026-11-23`** y `20 kg` en el formulario **"Ingresar lote nuevo"**: mostrar que el sistema lo ubica automáticamente **entre `L-102` (22 nov) y `L-101` (24 nov)**, probando la inserción ordenada FEFO en vivo.

3. **Pantalla `Ventas` — Descuento automático FEFO (12:15 – 13:30):**  
   - Seleccionar **`P001 — Mango Tommy`**, escribir **`35 kg`** y cliente **`Restaurante La Mulata`**.
   - Señalar el recuadro de previsualización en vivo antes de confirmar: muestra el precio vigente (`$4.700/kg`), qué fecha vence primero y el total a cobrar (`$164.500`).
   - Confirmar venta y mostrar la nueva fila en **"Registro de ventas realizadas"**: resaltar la columna de lotes usados (ej. agotó `L-102` 25 kg y tomó 10 kg del lote siguiente automáticamente) y la ganancia real obtenida (`$52.500`).

4. **Pantalla `Pedidos y mensajeros` — `ListaSimplePedidos` + `ListaCircular` Round Robin (13:30 – 14:45):**  
   - Mostrar la fila cargada con los **5.000 pedidos** del CSV arrancando en `PED-0001 (Rest. La Mulata)`.
   - Registrar un pedido nuevo rápido (ej. *Tienda El Sol, 15 kg de Tomate*) explicando que entra al final en `O(1)` gracias al puntero `ultimo`.
   - Hacer clic **3 veces** en **"Despachar siguiente pedido"** (o en **"Asignar mensajero (1 clic)"**): mostrar cómo el turno rota automáticamente `Carlos -> Dairo -> Édgar -> Carlos` y el contador de viajes se mantiene perfectamente equilibrado.

5. **Pestaña `Auditoría técnica` → `ListaDoble (Bonificación +0,3)` (14:45 – 15:30):**  
   - Abrir el subpanel **`ListaDoble`** y mostrar todos los movimientos que acabamos de hacer en vivo (retiro de vencidos, lote nuevo, venta de mango, despachos).
   - Pulsar **"Recorrer adelante (sig)"** y luego **"Recorrer atrás (ant)"**: demostrar cómo el recorrido se invierte al instante caminando por las referencias `ant` y `sig`.

### Parte B (15:30 – 20:00) | Rafael Enrique Muñoz Devoz — Catálogo, Protección Histórica, Búsquedas, Ordenamientos y Conjuntos

1. **Pantalla `Productos y precios` — Inmutabilidad Histórica en vivo (15:30 – 17:00):**  
   - Buscar **`P001`** (Mango Tommy) y pulsar **"Editar"**.
   - Subir el precio de venta de `$4.700` a **`$5.500/kg`** (mostrar cómo el margen se recalcula al instante a `$2.300/kg`) y leer en voz alta el aviso **"Historial protegido"**.
   - Guardar el cambio, volver a la pantalla **`Ventas`** y señalar la venta que acaba de registrar Jesús: **sigue mostrando exactamente `$4.700/kg`, total `$164.500` y ganancia `$52.500`**.
   - Decir: *"Si un software cambiara las ventas del pasado cuando sube el precio del mango, le dañaría toda la contabilidad al comerciante. Aquí cada venta y cada lote guarda su foto histórica y el nuevo precio solo afecta de ahora en adelante."*

2. **Pantalla `Ganancias y clientes` — 4 Ordenamientos y Conjuntos (17:00 – 18:30):**  
   - Mostrar el ranking de los productos que dejan mayor ganancia por kilo.
   - Alternar en vivo entre **Merge Sort**, **Quick Sort**, **Burbuja Mejorada** y **Burbuja**: señalar arriba de la tabla cómo el ranking es idéntico, pero las comparaciones pasan de **19.900 en Burbuja** a cerca de **1.300–1.500 en Merge/Quick Sort**.
   - Bajar a la sección de **Clientes frecuentes**: mostrar las etiquetas verdes (`S1 ∩ S2`, quienes compraron en las dos semanas) y los clientes únicos de la quincena (`S1 ∪ S2`), ambos calculados con arreglos propios sin usar `HashSet`.

3. **Pestaña `Auditoría técnica` → `Binaria vs. Secuencial` (18:30 – 20:00):**  
   - Escribir **`P200`** (peor caso del catálogo de 200 productos) y pulsar **"Comparar búsquedas"**.
   - Mostrar el contraste real: **200 comparaciones** en Búsqueda Secuencial vs. **solo 8 comparaciones** en Búsqueda Binaria (**96,0 % menos pasos**), señalando la traza paso a paso (`medio = 99 -> 149 -> 174 -> 187 -> 193 -> 196 -> 198 -> 199`).
   - Probar opcionalmente **`P050A`** para mostrar cómo la Secuencial Ordenada se detiene en la comparación 51 sin recorrer los 200 productos.

---

## BLOQUE 4 — Simulación, Eficiencia `Medidor` y 16 Pruebas (Minutos 20:00 – 25:00)

*Permanecer en `Auditoría técnica` y cerrar con las Escenas 10, 11 y 12 del Storybook.*

### 20:00 – 24:15 | Juan Diego Tobio Villadiego

1. **Subpanel `Simulación Doña Rosa` (20:00 – 21:30):**  
   - Mostrar en pantalla la comparación exacta de una semana de ventas de Mango Tommy (145 kg en 4 lotes = `$464.000`, demanda semanal `90 kg`):
     - **Sin el sistema (Cuaderno — apila nuevo sobre viejo):** se despachan primero los lotes más frescos (`L-103` y `L-104`), y el viernes se pudren `L-102` (25 kg) y 30 kg de `L-101` = **55 kg perdidos** (`-$176.000` a la basura). Aunque vendió bien (`+$135.000` de margen), termina la semana con **`-$41.000` de pérdida neta**.
     - **Con MercaBazurto Digital (FEFO + Turnos):** salen primero `L-102` (25 kg), `L-101` (40 kg) y 25 kg de `L-104`. El viernes quedan **0 kg perdidos** (`$0` botado) y una **utilidad neta de `+$135.000`**.
     - **Ahorro real verificado en un solo producto en una semana:** **55 kg salvados y `+$176.000 COP` recuperados**.

2. **Subpanel `Medidor Big-O en vivo` (21:30 – 23:00):**  
   - Pulsar **"Medir tiempos reales ahora"** (ejecuta `System.nanoTime()` en vivo para `n = 200`, `2.000` y `5.000`).
   - Comentar los resultados reales medidos en el equipo de pruebas:

| Operación / Algoritmo | Complejidad Big-O | `n = 200` | `n = 2.000` | `n = 5.000` | Lectura práctica |
| :--- | :---: | ---: | ---: | ---: | :--- |
| Despachar lote / Pedido / Turno Round Robin | `O(1)` | `0,0001 ms` | `0,0001 ms` | `0,0001 ms` | Constante aunque haya 5.000 pedidos |
| Búsqueda binaria por código (peor caso) | `O(log2 n)` | `0,0102 ms` | `0,0048 ms` | `0,0091 ms` | Instantánea (8, 11 y 13 pasos) |
| Búsqueda secuencial (peor caso) | `O(n)` | `0,0094 ms` | `0,0352 ms` | `0,2541 ms` | Crece linealmente con el catálogo |
| Valor recursivo del inventario | `O(n)` | `0,0038 ms` | `0,0442 ms` | `0,1791 ms` | Suma miles de lotes en < 0,2 ms |
| Burbuja mejorada (arreglo ya ordenado) | `O(n)` | `0,0014 ms` | `0,0179 ms` | `0,1138 ms` | Para en 1 pasada gracias a `huboCambio` |
| Quick Sort (caso promedio) | `O(n log2 n)` | `0,0250 ms` | `0,3353 ms` | `1,0198 ms` | ~180× más rápido que Burbuja en `n = 5.000` |
| Merge Sort (caso promedio) | `O(n log2 n)` | `0,0427 ms` | `0,5540 ms` | `2,6749 ms` | Estable y garantizado `O(n log n)` |
| Burbuja estándar (peor caso) | `O(n²)` | `0,3291 ms` | `21,4520 ms` | `182,8747 ms` | Se dispara cuadráticamente al crecer `n` |

3. **Subpanel `16 Pruebas automatizadas` (23:00 – 24:15):**  
   - Pulsar **"Ejecutar 16 pruebas"** y mostrar **`16 / 16 casos superados [PASS]`**.
   - Destacar las pruebas clave: `P01–P04` (FEFO y borde de stock), `P05–P06` (pedidos `O(1)` y cancelación), `P07` (Round Robin 7/3), `P08` (valor recursivo `$464.000`), `P09–P13` (búsquedas, CSV 200/2.000/5.000, 4 ordenamientos y conjuntos), `P14` (`ListaDoble` adelante/atrás), `P15` (simulación Doña Rosa) y `P16` (inmutabilidad histórica al cambiar precio).

### 24:15 – 25:00 | Miguel Andrés Baza Contreras (Cierre con Escenas 10, 11 y 12 del Storybook)

- Volver 45 segundos a las 3 últimas láminas del Storybook:
  - **Escena 10:** *"Aquí vemos resumido lo que acabamos de comprobar en vivo: la merma bajó de 55 kg a 0 kg y nuestras estructuras mantienen respuesta en microsegundos."*
  - **Escena 11:** *"Gracias a MercaBazurto Digital, Doña Rosa ya no bota fruta, atiende todos los encargos en orden y sus mensajeros trabajan en armonía."*
  - **Escena 12:** *"Muchas gracias profesor. Quedamos atentos a sus preguntas y si desea revisar cualquier clase Java o probar cualquier escenario en vivo, estamos listos."*

---

## BLOQUE 5 — Banco de 8 Preguntas Probables del Profesor y Respuestas Cortas (Minutos 25:00 – 30:00)

*Todos los integrantes deben repasar las 8 respuestas; cada una está redactada para contestarse en 20–30 segundos con seguridad.*

1. **Pregunta: ¿Por qué usaron una lista simple ordenada para los lotes y no un arreglo normal?**  
   - **Respuesta (Jesús / Miguel):** *"Porque en un puesto de mercado los lotes entran y se agotan todo el día. Si fuera un arreglo, retirar el primer lote que se vendió obligaría a correr todos los demás elementos en `O(n)`. Con nuestra `ListaSimpleLotes`, vender o retirar un lote vencido en la cabeza solo mueve la referencia `cabeza = cabeza.sig` en `O(1)` y el tamaño crece dinámicamente sin desperdiciar memoria."*

2. **Pregunta: En la guía estaba prohibido usar `Queue` de Java. ¿Cómo lograron que los pedidos entren y salgan en `O(1)`?**  
   - **Respuesta (Jesús):** *"Programamos `ListaSimplePedidos` con dos referencias propias: `primero` y `ultimo`. Cuando entra un pedido nuevo lo conectamos directamente a `ultimo.sig` y actualizamos `ultimo` en `O(1)` sin recorrer la lista. Cuando se despacha, sacamos `primero.dato` y avanzamos `primero = primero.sig`, también en `O(1)`. Y si la lista queda vacía, ponemos `ultimo = null`."*

3. **Pregunta: ¿Cómo garantiza la lista circular que ningún mensajero trabaje de más?**  
   - **Respuesta (Juan Diego / Jesús):** *"En `ListaCircular<Mensajero>`, el puntero `ultimo.sig` siempre señala de vuelta al primer mensajero formando un anillo cerrado (`Carlos -> Dairo -> Édgar -> Carlos`). Cada vez que llamamos a `siguienteTurno()`, avanzamos un paso (`ultimo = ultimo.sig`) en `O(1)`. Así, después de `D` domicilios entre `M` mensajeros, todos reciben `D / M` viajes y la diferencia máxima nunca supera 1 viaje."*

4. **Pregunta: ¿Por qué la búsqueda binaria necesita el arreglo ordenado y qué pasa si agregan un producto nuevo desde la pantalla?**  
   - **Respuesta (Rafael):** *"La búsqueda binaria parte el catálogo a la mitad comparando el elemento del centro; eso solo funciona si los códigos están ordenados ascendentemente (`P001` a `P200`). Por eso, cuando el comerciante agrega un producto nuevo en `Puesto.agregarProducto()`, desplazamos los códigos mayores e insertamos el nuevo en su posición ordenada exacta, manteniendo siempre válida la búsqueda binaria en `O(log2 n)`."*

5. **Pregunta: ¿Cuál es la diferencia real entre Burbuja estándar, Burbuja mejorada, Quick Sort y Merge Sort en su programa?**  
   - **Respuesta (Rafael):** *"Burbuja estándar siempre hace todas las pasadas en `O(n²)` (en `n = 5.000` nos dio ~183 ms). Burbuja mejorada agrega la bandera `huboCambio`: si el catálogo ya estaba ordenado, detecta 0 cambios en la primera pasada y se detiene en `O(n)` (`0,11 ms`). Quick Sort y Merge Sort aplican Divide y Vencerás en `O(n log2 n)` (~1 a 2,7 ms en `n = 5.000`), y Merge Sort además garantiza ese tiempo incluso en el peor caso y es estable."*

6. **Pregunta: ¿Cómo funciona la recursividad para calcular el valor del inventario? ¿Cuál es su caso base?**  
   - **Respuesta (Miguel / Rafael):** *"En `ListaSimpleLotes.calcularValorRecursivo(Nodo<Lote> actual)`, el **caso base** es cuando `actual == null`, donde retorna `0`. En el **caso recursivo**, multiplica `cantidadKg * precioCompraKg` del nodo actual y le suma la llamada recursiva con `actual.sig`. Sobre los 4 lotes de Doña Rosa (145 kg × `$3.200`), la pila expande 4 llamadas y al regresar acumula exactamente `$464.000 COP`."*

7. **Pregunta: ¿Por qué cambiar el precio de un producto no daña las ventas que ya se hicieron antes?**  
   - **Respuesta (Miguel):** *"Porque diseñamos `Lote` y `Venta` con inmutabilidad histórica (snapshot). Cada `Lote` congela como `final` el precio de compra que tenía el producto al ingresar a bodega, y cada `Venta` congela como `final` el nombre del producto, el precio de venta aplicado y el costo real de los lotes FEFO descontados. Cuando `actualizarProducto()` cambia el precio en `Producto`, los objetos históricos ya creados ni siquiera tienen setters, así que nunca se alteran."*

8. **Pregunta: ¿Qué aporta la `ListaDoble` de bonificación y qué cuidado especial tuvieron al eliminar un nodo?**  
   - **Respuesta (Jesús / Juan Diego):** *"La `ListaDoble<Movimiento>` guarda la bitácora del día usando `NodoDoble<T>` con dos referencias: `ant` y `sig`. Eso permite al comerciante o auditor revisar los movimientos desde las 4:00 a. m. hacia adelante (`sig`) o desde el último evento hacia atrás (`ant`). Al eliminar un nodo en el medio, puenteamos ambas direcciones (`actual.ant.sig = actual.sig` y `actual.sig.ant = actual.ant`) para no romper ninguno de los dos recorridos, verificado en nuestra prueba `P14`."*
