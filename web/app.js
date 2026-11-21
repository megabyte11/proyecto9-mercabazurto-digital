/* ============================================================================
 * MERCABAZURTO DIGITAL - Logica de interfaz local conectada a ServidorApp Java
 * Cero dependencias externas, lenguaje cotidiano de mercado, actualizaciones en vivo.
 * ============================================================================ */

const estadoApp = {
  vistaActual: "inicio",
  productosCache: [],
  algoritmoMargen: "mergesort",
  direccionListaDoble: "adelante"
};

// --- Utilidades de formato colombiano (pesos COP y porcentajes) ---

function formatoPesos(valor) {
  const num = Number(valor) || 0;
  const signo = num < 0 ? "-" : "";
  const abs = Math.abs(Math.round(num));
  return signo + "$" + abs.toLocaleString("es-CO");
}

function escaparHtml(texto) {
  return String(texto == null ? "" : texto)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function mostrarAviso(mensaje, esError) {
  const contenedor = document.getElementById("toast-container");
  const aviso = document.createElement("div");
  aviso.className = "toast" + (esError ? " error" : "");
  aviso.textContent = mensaje;
  contenedor.appendChild(aviso);
  setTimeout(function () {
    aviso.remove();
  }, 4200);
}

async function pedirApi(url, opciones) {
  let resp;
  try {
    resp = await fetch(url, opciones || {});
  } catch (err) {
    throw new Error("Sin conexión con el servidor Java. Verifica que Main.java siga en ejecución en IntelliJ IDEA.");
  }
  const datos = await resp.json();
  if (!resp.ok || datos.ok === false) {
    throw new Error(datos.error || "No se pudo completar la operación.");
  }
  return datos;
}

function etiquetaEstadoLote(estado, dias) {
  if (estado === "VENCIDO") {
    return '<span class="pill pill-vencido">Vencido</span>';
  }
  if (estado === "PROXIMO") {
    const sufijo = dias === 0 ? " (hoy)" : (dias === 1 ? " (mañana)" : " (" + dias + " d)");
    return '<span class="pill pill-proximo">Próximo a vencer' + sufijo + "</span>";
  }
  return '<span class="pill pill-vigente">Vigente</span>';
}

// --- Navegacion principal entre vistas ---

function cambiarVista(nombreVista) {
  estadoApp.vistaActual = nombreVista;

  document.querySelectorAll(".view-panel").forEach(function (sec) {
    sec.classList.toggle("active", sec.id === "view-" + nombreVista);
  });

  document.querySelectorAll(".nav-btn").forEach(function (btn) {
    btn.classList.toggle("active", btn.getAttribute("data-view") === nombreVista);
  });

  if (nombreVista === "inicio") cargarResumen();
  if (nombreVista === "ventas") cargarVistaVentas();
  if (nombreVista === "productos") cargarVistaProductos();
  if (nombreVista === "vencimiento") cargarVistaLotes();
  if (nombreVista === "pedidos") cargarVistaPedidos();
  if (nombreVista === "margen") cargarVistaMargenYClientes();
  if (nombreVista === "auditoria") cargarVistaAuditoriaInicial();
}

// --- Poblar selectores de productos en Ventas, Lotes y Pedidos ---

async function refrescarCatalogoCache() {
  const data = await pedirApi("/api/productos");
  estadoApp.productosCache = data.productos || [];

  const selectores = ["venta-producto", "lote-producto", "ped-producto"];
  selectores.forEach(function (id) {
    const sel = document.getElementById(id);
    if (!sel) return;
    const valorPrevio = sel.value;
    sel.innerHTML = estadoApp.productosCache.map(function (p) {
      return '<option value="' + escaparHtml(p.codigo) + '">'
        + escaparHtml(p.codigo + " — " + p.nombre + " (" + p.kgDisponibles + " kg)")
        + "</option>";
    }).join("");
    if (valorPrevio) sel.value = valorPrevio;
  });

  const selFiltroLote = document.getElementById("filtro-lote-producto");
  if (selFiltroLote) {
    const valPrev = selFiltroLote.value;
    selFiltroLote.innerHTML = '<option value="">Todos los productos (más urgentes primero)</option>'
      + estadoApp.productosCache.map(function (p) {
        return '<option value="' + escaparHtml(p.codigo) + '">'
          + escaparHtml(p.codigo + " — " + p.nombre)
          + "</option>";
      }).join("");
    selFiltroLote.value = valPrev;
  }

  actualizarPrevisualizacionVenta();
}

// ============================================================================
// 1. INICIO / RESUMEN DEL DIA
// ============================================================================

async function cargarResumen() {
  try {
    const d = await pedirApi("/api/resumen");
    document.getElementById("sidebar-fecha").textContent = d.fechaOperacion;
    document.getElementById("kpi-vendido").textContent = formatoPesos(d.totalVendidoHoy);
    document.getElementById("kpi-ventas-count").textContent = d.cantidadVentasHoy + " ventas registradas";
    document.getElementById("kpi-ganancia").textContent = formatoPesos(d.gananciaNetaHoy);
    document.getElementById("kpi-inventario").textContent = formatoPesos(d.valorInventarioRecursivo);
    document.getElementById("kpi-lotes-count").textContent = d.totalLotesActivos + " lotes activos en bodega";

    const totalRevisar = d.lotesVencidos + d.lotesProximos;
    document.getElementById("kpi-alertas").textContent = totalRevisar.toLocaleString("es-CO");
    document.getElementById("kpi-alertas-detalle").textContent =
      d.lotesVencidos + " vencidos · " + d.lotesProximos + " próximos a vencer";

    document.getElementById("badge-ventas").textContent = d.cantidadVentasHoy;
    document.getElementById("badge-vencidos").textContent = d.lotesVencidos;
    document.getElementById("badge-pedidos").textContent = d.pedidosPendientes;
    document.getElementById("inicio-turno-resumen").textContent =
      "Próximo mensajero en turno: " + d.proximoMensajero + " (" + d.pedidosPendientes + " pedidos en fila)";

    const cuerpo = document.getElementById("inicio-alertas-body");
    if (!d.alertasFefo || d.alertasFefo.length === 0) {
      cuerpo.innerHTML = '<tr><td colspan="5" class="empty-state">No hay lotes registrados todavía.</td></tr>';
      return;
    }

    cuerpo.innerHTML = d.alertasFefo.map(function (l) {
      return "<tr>"
        + "<td><strong>" + escaparHtml(l.nombreProducto) + '</strong><span class="sub-cell">' + escaparHtml(l.codigoProducto) + "</span></td>"
        + '<td class="mono">' + escaparHtml(l.codigoLote) + "</td>"
        + '<td class="mono">' + escaparHtml(l.fechaVencimiento) + "</td>"
        + '<td class="num">' + l.cantidadKg + " kg</td>"
        + "<td>" + etiquetaEstadoLote(l.estado, l.diasParaVencer) + "</td>"
        + "</tr>";
    }).join("");
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

// ============================================================================
// 2. VENTAS (REGISTRAR + HISTORIAL INMUTABLE)
// ============================================================================

function actualizarPrevisualizacionVenta() {
  const codigo = document.getElementById("venta-producto").value;
  const kilos = parseInt(document.getElementById("venta-kilos").value, 10) || 0;
  const prod = estadoApp.productosCache.find(function (p) { return p.codigo === codigo; });
  if (!prod) return;

  document.getElementById("venta-stock-help").textContent =
    "Disponible en bodega: " + prod.kgDisponibles + " kg (" + prod.cantidadLotes + " lotes)";
  document.getElementById("venta-prev-precio").textContent = formatoPesos(prod.precioVentaKg) + "/kg";
  document.getElementById("venta-prev-vence").textContent = prod.proximoVencimiento;
  document.getElementById("venta-prev-total").textContent = formatoPesos(prod.precioVentaKg * kilos);
}

async function cargarVistaVentas() {
  try {
    await refrescarCatalogoCache();
    const q = document.getElementById("filtro-ventas").value.trim();
    const data = await pedirApi("/api/ventas?q=" + encodeURIComponent(q));

    document.getElementById("ventas-totales-resumen").textContent =
      "Total vendido: " + formatoPesos(data.totalVendido)
      + " · Ganancia real: " + formatoPesos(data.gananciaTotal)
      + " (" + data.cantidadVentas + " ventas)";

    const cuerpo = document.getElementById("ventas-body");
    if (!data.ventas || data.ventas.length === 0) {
      cuerpo.innerHTML = '<tr><td colspan="7" class="empty-state">Aún no hay ventas que coincidan con el filtro.</td></tr>';
      return;
    }

    cuerpo.innerHTML = data.ventas.map(function (v) {
      return "<tr>"
        + '<td><strong class="mono">' + escaparHtml(v.idVenta) + '</strong><span class="sub-cell">' + escaparHtml(v.fechaHora) + "</span></td>"
        + "<td><strong>" + escaparHtml(v.nombreProducto) + '</strong><span class="sub-cell">Lotes: ' + escaparHtml(v.detalleLotesUsados) + "</span></td>"
        + "<td>" + escaparHtml(v.cliente) + "</td>"
        + '<td class="num">' + v.cantidadKg + " kg</td>"
        + '<td class="num">' + formatoPesos(v.precioVentaAplicado) + "</td>"
        + '<td class="num"><strong>' + formatoPesos(v.totalVenta) + "</strong></td>"
        + '<td class="num" style="color:var(--brand-green); font-weight:600;">' + formatoPesos(v.gananciaReal) + "</td>"
        + "</tr>";
    }).join("");
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

// ============================================================================
// 3. PRODUCTOS Y PRECIOS (AGREGAR Y EDITAR SIN DAÑAR HISTORIAL)
// ============================================================================

function actualizarPrevisualizacionProducto() {
  const compra = parseInt(document.getElementById("prod-compra").value, 10) || 0;
  const venta = parseInt(document.getElementById("prod-venta").value, 10) || 0;
  const ganancia = venta - compra;
  const margen = venta > 0 ? (ganancia * 100) / venta : 0;

  document.getElementById("prod-prev-ganancia").textContent = formatoPesos(ganancia) + "/kg";
  document.getElementById("prod-prev-margen").textContent = margen.toFixed(1).replace(".", ",") + "%";
}

function prepararEdicionProducto(codigo) {
  const prod = estadoApp.productosCache.find(function (p) { return p.codigo === codigo; });
  if (!prod) return;

  document.getElementById("prod-modo").value = "editar";
  document.getElementById("prod-form-titulo").textContent = "Actualizar producto " + prod.codigo;
  document.getElementById("prod-form-sub").textContent = "Los cambios aplican solo para lotes y ventas nuevas.";
  document.getElementById("prod-codigo").value = prod.codigo;
  document.getElementById("prod-codigo").readOnly = true;
  document.getElementById("prod-nombre").value = prod.nombre;
  document.getElementById("prod-categoria").value = prod.categoria;
  document.getElementById("prod-compra").value = prod.precioCompraKg;
  document.getElementById("prod-venta").value = prod.precioVentaKg;
  document.getElementById("btn-prod-guardar").querySelector("span").textContent = "Guardar cambios de precio";
  document.getElementById("btn-prod-limpiar").style.display = "inline-flex";
  actualizarPrevisualizacionProducto();
  document.getElementById("prod-nombre").focus();
}

function limpiarFormularioProducto() {
  document.getElementById("prod-modo").value = "crear";
  document.getElementById("prod-form-titulo").textContent = "Agregar nuevo producto";
  document.getElementById("prod-form-sub").textContent = "El margen de ganancia se calcula automáticamente.";
  document.getElementById("prod-codigo").value = "";
  document.getElementById("prod-codigo").readOnly = false;
  document.getElementById("prod-nombre").value = "";
  document.getElementById("prod-categoria").value = "Fruta";
  document.getElementById("prod-compra").value = "3200";
  document.getElementById("prod-venta").value = "4700";
  document.getElementById("btn-prod-guardar").querySelector("span").textContent = "Guardar producto";
  document.getElementById("btn-prod-limpiar").style.display = "none";
  actualizarPrevisualizacionProducto();
}

async function cargarVistaProductos() {
  try {
    const q = document.getElementById("filtro-productos").value.trim();
    const cat = document.getElementById("filtro-categoria").value;
    const data = await pedirApi("/api/productos?q=" + encodeURIComponent(q) + "&cat=" + encodeURIComponent(cat));
    if (!q && cat === "todas") {
      estadoApp.productosCache = data.productos || [];
    }

    document.getElementById("productos-conteo").textContent =
      "Mostrando " + data.productos.length + " de " + data.totalCatalogo + " productos";

    const cuerpo = document.getElementById("productos-body");
    if (data.productos.length === 0) {
      cuerpo.innerHTML = '<tr><td colspan="7" class="empty-state">No se encontraron productos con ese filtro.</td></tr>';
      return;
    }

    // Mostrar hasta 80 filas en tabla para mantener respuesta instantanea
    const visibles = data.productos.slice(0, 80);
    cuerpo.innerHTML = visibles.map(function (p) {
      return "<tr>"
        + '<td class="mono"><strong>' + escaparHtml(p.codigo) + "</strong></td>"
        + "<td><strong>" + escaparHtml(p.nombre) + '</strong><span class="sub-cell">' + escaparHtml(p.categoria) + "</span></td>"
        + '<td class="num">' + formatoPesos(p.precioCompraKg) + "</td>"
        + '<td class="num">' + formatoPesos(p.precioVentaKg) + "</td>"
        + '<td class="num" style="color:var(--brand-green); font-weight:600;">' + formatoPesos(p.gananciaKg)
        + '<span class="sub-cell">' + Number(p.margenPorcentaje).toFixed(1).replace(".", ",") + "%</span></td>"
        + '<td class="num">' + p.kgDisponibles + " kg</td>"
        + '<td><button type="button" class="btn btn-secondary btn-sm btn-editar-prod" data-cod="' + escaparHtml(p.codigo) + '">Editar</button></td>'
        + "</tr>";
    }).join("");

    cuerpo.querySelectorAll(".btn-editar-prod").forEach(function (btn) {
      btn.addEventListener("click", function () {
        prepararEdicionProducto(btn.getAttribute("data-cod"));
      });
    });
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

// ============================================================================
// 4. INVENTARIO POR VENCIMIENTO (LO QUE VENCE PRIMERO - FEFO)
// ============================================================================

async function cargarVistaLotes() {
  try {
    const cod = document.getElementById("filtro-lote-producto").value;
    const est = document.getElementById("filtro-lote-estado").value;
    const data = await pedirApi("/api/lotes?codigoProducto=" + encodeURIComponent(cod)
      + "&estado=" + encodeURIComponent(est) + "&limite=80");

    document.getElementById("lotes-resumen-conteo").textContent =
      "Vencidos: " + data.lotesVencidos
      + " · Próximos a vencer: " + data.lotesProximos
      + " · Vigentes: " + data.lotesVigentes;

    const cuerpo = document.getElementById("lotes-body");
    if (!data.lotes || data.lotes.length === 0) {
      cuerpo.innerHTML = '<tr><td colspan="6" class="empty-state">No hay lotes que coincidan con este filtro.</td></tr>';
      return;
    }

    cuerpo.innerHTML = data.lotes.map(function (l) {
      return "<tr>"
        + '<td class="mono"><strong>' + escaparHtml(l.codigoLote) + "</strong></td>"
        + "<td><strong>" + escaparHtml(l.nombreProducto) + '</strong><span class="sub-cell">' + escaparHtml(l.codigoProducto + " · " + l.categoria) + "</span></td>"
        + '<td class="mono">' + escaparHtml(l.fechaVencimiento) + "</td>"
        + '<td class="num"><strong>' + l.cantidadKg + " kg</strong></td>"
        + '<td class="num">' + formatoPesos(l.precioCompraKg) + "</td>"
        + "<td>" + etiquetaEstadoLote(l.estado, l.diasParaVencer) + "</td>"
        + "</tr>";
    }).join("");
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

async function retirarVencidosAccion() {
  try {
    const res = await pedirApi("/api/lotes/retirar-vencidos", { method: "POST" });
    mostrarAviso(res.mensaje, false);
    await refrescarCatalogoCache();
    cargarResumen();
    if (estadoApp.vistaActual === "vencimiento") cargarVistaLotes();
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

// ============================================================================
// 5. PEDIDOS Y MENSAJEROS (FILA O(1) + ROUND ROBIN O(1))
// ============================================================================

async function cargarVistaPedidos() {
  try {
    const [pedData, mensData] = await Promise.all([
      pedirApi("/api/pedidos?limite=30"),
      pedirApi("/api/mensajeros")
    ]);

    // Renderizar tarjetas de mensajeros
    const grid = document.getElementById("mensajeros-grid");
    grid.innerHTML = mensData.mensajeros.map(function (m) {
      const esSiguiente = m.nombre === mensData.proximoTurno;
      return '<div class="messenger-card' + (esSiguiente ? " next-turn" : "") + '">'
        + '<div class="messenger-name"><span>' + escaparHtml(m.nombre) + "</span>"
        + (esSiguiente ? '<span class="pill pill-vigente">Sigue en turno</span>' : '<span class="pill pill-neutral">En espera</span>')
        + "</div>"
        + '<div class="messenger-trips">' + m.viajesRealizados + " viajes</div>"
        + '<div class="messenger-meta">Tel: ' + escaparHtml(m.telefono) + "</div>"
        + '<div class="messenger-meta">Último: ' + escaparHtml(m.ultimoDestino) + "</div>"
        + "</div>";
    }).join("");

    document.getElementById("pedidos-conteo").textContent =
      pedData.totalPendientes.toLocaleString("es-CO") + " pedidos en fila (se muestran los primeros 30 en orden de llegada)";

    const cuerpo = document.getElementById("pedidos-body");
    if (!pedData.pendientes || pedData.pendientes.length === 0) {
      cuerpo.innerHTML = '<tr><td colspan="6" class="empty-state">No quedan pedidos pendientes por despachar.</td></tr>';
    } else {
      cuerpo.innerHTML = pedData.pendientes.map(function (p, idx) {
        return "<tr>"
          + '<td class="mono"><strong>' + escaparHtml(p.codigoPedido) + "</strong>"
          + (idx === 0 ? '<span class="sub-cell" style="color:var(--brand-green); font-weight:600;">Próximo en salir</span>' : "") + "</td>"
          + "<td>" + escaparHtml(p.cliente) + "</td>"
          + "<td>" + escaparHtml(p.nombreProducto) + ' <span class="mono">(' + escaparHtml(p.codigoProducto) + ")</span></td>"
          + '<td class="num">' + p.cantidadKg + " kg</td>"
          + '<td class="mono">' + escaparHtml(p.fechaHora) + "</td>"
          + '<td><button type="button" class="btn btn-danger-outline btn-sm btn-cancelar-ped" data-cod="' + escaparHtml(p.codigoPedido) + '">Cancelar</button></td>'
          + "</tr>";
      }).join("");

      cuerpo.querySelectorAll(".btn-cancelar-ped").forEach(function (btn) {
        btn.addEventListener("click", async function () {
          try {
            const res = await pedirApi("/api/pedidos/cancelar", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ codigoPedido: btn.getAttribute("data-cod") })
            });
            mostrarAviso(res.mensaje, false);
            cargarVistaPedidos();
            cargarResumen();
          } catch (err) {
            mostrarAviso(err.message, true);
          }
        });
      });
    }

    const wrapAt = document.getElementById("pedidos-atendidos-wrap");
    if (pedData.atendidos && pedData.atendidos.length > 0) {
      wrapAt.style.display = "block";
      document.getElementById("pedidos-atendidos-body").innerHTML = pedData.atendidos.map(function (p) {
        const pill = p.estado === "Despachado"
          ? '<span class="pill pill-vigente">Despachado</span>'
          : '<span class="pill pill-vencido">Cancelado</span>';
        return "<tr>"
          + '<td class="mono">' + escaparHtml(p.codigoPedido) + "</td>"
          + "<td>" + escaparHtml(p.cliente) + "</td>"
          + "<td>" + pill + "</td>"
          + "<td>" + escaparHtml(p.mensajeroAsignado) + "</td>"
          + "</tr>";
      }).join("");
    } else {
      wrapAt.style.display = "none";
    }
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

// ============================================================================
// 6. MARGEN DE GANANCIA Y CLIENTES FRECUENTES
// ============================================================================

async function cargarVistaMargenYClientes() {
  try {
    const [margenData, clientesData] = await Promise.all([
      pedirApi("/api/margen?algoritmo=" + encodeURIComponent(estadoApp.algoritmoMargen)),
      pedirApi("/api/clientes")
    ]);

    document.getElementById("margen-metrica").textContent =
      "Algoritmo: " + margenData.algoritmo
      + " · Comparaciones: " + margenData.comparaciones.toLocaleString("es-CO")
      + " · Intercambios: " + margenData.intercambios.toLocaleString("es-CO")
      + " · Tiempo: " + margenData.tiempoMs + " ms";

    const top = (margenData.productos || []).slice(0, 30);
    document.getElementById("margen-body").innerHTML = top.map(function (p, i) {
      return "<tr>"
        + '<td class="mono"><strong>#' + (i + 1) + "</strong></td>"
        + '<td class="mono">' + escaparHtml(p.codigo) + "</td>"
        + "<td><strong>" + escaparHtml(p.nombre) + "</strong></td>"
        + "<td>" + escaparHtml(p.categoria) + "</td>"
        + '<td class="num">' + formatoPesos(p.precioCompraKg) + "</td>"
        + '<td class="num">' + formatoPesos(p.precioVentaKg) + "</td>"
        + '<td class="num" style="color:var(--brand-green); font-weight:700;">' + formatoPesos(p.gananciaKg) + "</td>"
        + '<td class="num">' + Number(p.margenPorcentaje).toFixed(1).replace(".", ",") + "%</td>"
        + "</tr>";
    }).join("");

    // Renderizar clientes frecuentes (interseccion) y unicos (union)
    const frec = clientesData.frecuentes || [];
    const unic = clientesData.unicos || [];
    document.getElementById("clientes-frec-sub").textContent =
      frec.length + " clientes fieles que compraron en Semana 1 y Semana 2 (S1 ∩ S2)";
    document.getElementById("clientes-uni-sub").textContent =
      unic.length + " clientes distintos atendidos en la quincena sin duplicados (S1 ∪ S2)";

    document.getElementById("clientes-frecuentes-list").innerHTML = frec.map(function (c) {
      return '<span class="client-chip frecuente">' + escaparHtml(c) + "</span>";
    }).join("");

    document.getElementById("clientes-unicos-list").innerHTML = unic.map(function (c) {
      return '<span class="client-chip">' + escaparHtml(c) + "</span>";
    }).join("");
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

// ============================================================================
// 7. AUDITORIA TECNICA / EXPOSICION EN VIVO
// ============================================================================

async function cargarVistaAuditoriaInicial() {
  await correrSimulacionSemana();
  await compararBusquedas();
  await cargarMovimientosListaDoble();
  await correrPruebasSistema();
}

async function correrSimulacionSemana() {
  try {
    const demanda = parseInt(document.getElementById("sim-demanda").value, 10) || 90;
    const s = await pedirApi("/api/auditoria/simulacion?demanda=" + demanda);

    document.getElementById("sim-resultado").innerHTML =
      '<div class="sim-compare">'
      + '  <div class="sim-panel cuaderno-loss">'
      + '    <h3 style="margin:0 0 12px 0; color:var(--red-ink);">Sin el sistema (Cuaderno: apila nuevo sobre viejo)</h3>'
      + '    <div class="sim-row"><span>Inventario inicial (4 lotes)</span><strong>' + s.kgInventarioInicial + " kg (" + formatoPesos(s.valorInventarioInicial) + ")</strong></div>"
      + '    <div class="sim-row"><span>Orden de salida (' + s.kgVendidosCuaderno + ' kg)</span><span class="mono">' + escaparHtml(s.detalleDespachoCuaderno) + "</span></div>"
      + '    <div class="sim-row"><span>Kilos perdidos el viernes</span><strong style="color:var(--red);">' + s.kgPerdidosCuaderno + " kg</strong></div>"
      + '    <div class="sim-row"><span>Lotes vencidos botados</span><span>' + escaparHtml(s.detalleVencidosCuaderno) + "</span></div>"
      + '    <div class="sim-row"><span>Costo mercancía botada</span><strong style="color:var(--red);">-' + formatoPesos(s.costoBotadoCuaderno) + "</strong></div>"
      + '    <div class="sim-row"><span>Ganancia bruta vendida</span><strong>' + formatoPesos(s.gananciaBrutaCuaderno) + "</strong></div>"
      + '    <div class="sim-row"><span><strong>Resultado neto de la semana</strong></span><strong style="color:var(--red); font-size:1.1rem;">' + formatoPesos(s.resultadoNetoCuaderno) + "</strong></div>"
      + "  </div>"
      + '  <div class="sim-panel fefo-win">'
      + '    <h3 style="margin:0 0 12px 0; color:var(--brand-green-ink);">Con MercaBazurto Digital (ListaSimple FEFO + Turnos)</h3>'
      + '    <div class="sim-row"><span>Inventario inicial (4 lotes)</span><strong>' + s.kgInventarioInicial + " kg (" + formatoPesos(s.valorInventarioInicial) + ")</strong></div>"
      + '    <div class="sim-row"><span>Orden de salida FEFO (' + s.kgVendidosFefo + ' kg)</span><span class="mono">' + escaparHtml(s.detalleDespachoFefo) + "</span></div>"
      + '    <div class="sim-row"><span>Kilos perdidos el viernes</span><strong style="color:var(--brand-green);">' + s.kgPerdidosFefo + " kg</strong></div>"
      + '    <div class="sim-row"><span>Stock vigente sobrante</span><span>' + escaparHtml(s.detalleSobranteFefo) + "</span></div>"
      + '    <div class="sim-row"><span>Costo mercancía botada</span><strong style="color:var(--brand-green);">' + formatoPesos(s.costoBotadoFefo) + "</strong></div>"
      + '    <div class="sim-row"><span>Ganancia bruta vendida</span><strong>' + formatoPesos(s.gananciaBrutaFefo) + "</strong></div>"
      + '    <div class="sim-row"><span><strong>Resultado neto de la semana</strong></span><strong style="color:var(--brand-green); font-size:1.1rem;">+' + formatoPesos(s.resultadoNetoFefo) + "</strong></div>"
      + "  </div>"
      + "</div>"
      + '<div class="sim-banner">'
      + "  <div><span>Impacto verificado en 1 semana de Mango Tommy</span><div style='font-size:0.85rem; opacity:0.9;'>Turnos Round Robin: " + escaparHtml(s.resumenTurnosMensajeros) + "</div></div>"
      + "  <strong>" + s.kgSalvados + " kg salvados · +" + formatoPesos(s.pesosSalvados) + " COP ahorrados</strong>"
      + "</div>";
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

async function compararBusquedas() {
  try {
    const cod = document.getElementById("busq-codigo").value.trim() || "P200";
    const d = await pedirApi("/api/auditoria/buscar?codigo=" + encodeURIComponent(cod));
    const sec = d.secuencial;
    const ord = d.secuencialOrdenada;
    const bin = d.binaria;
    const reduccion = sec.comparaciones > 0
      ? (((sec.comparaciones - bin.comparaciones) * 100) / sec.comparaciones).toFixed(1).replace(".", ",")
      : "0,0";

    const prodTexto = bin.encontrado
      ? (bin.producto.codigo + " — " + bin.producto.nombre + " (venta " + formatoPesos(bin.producto.precioVentaKg) + "/kg)")
      : "Código no encontrado en los " + d.totalCatalogo + " productos";

    document.getElementById("busq-resultado").innerHTML =
      '<div class="ledger-strip" style="grid-template-columns:repeat(3,1fr); margin-bottom:16px;">'
      + '  <div class="ledger-cell"><span class="ledger-label">Búsqueda Secuencial O(n)</span><span class="ledger-value danger">' + sec.comparaciones + ' comp.</span><span class="ledger-note">' + sec.tiempoMicros + ' µs</span></div>'
      + '  <div class="ledger-cell"><span class="ledger-label">Secuencial Ordenada O(n)</span><span class="ledger-value warn">' + ord.comparaciones + ' comp.</span><span class="ledger-note">' + ord.tiempoMicros + ' µs</span></div>'
      + '  <div class="ledger-cell"><span class="ledger-label">Búsqueda Binaria O(log2 n)</span><span class="ledger-value positive">' + bin.comparaciones + ' comp.</span><span class="ledger-note">Ahorro del ' + reduccion + '% en pasos</span></div>'
      + "</div>"
      + '<p style="margin:0 0 8px 0;"><strong>Resultado:</strong> ' + escaparHtml(prodTexto) + "</p>"
      + '<p style="margin:0; font-size:0.875rem; color:var(--text-secondary);">Traza paso a paso de la Búsqueda Binaria:</p>'
      + '<ul class="trace-list">'
      + bin.pasos.map(function (paso) { return "<li>" + escaparHtml(paso) + "</li>"; }).join("")
      + "</ul>";
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

async function correrMedidorEnVivo() {
  const cuerpo = document.getElementById("medidor-body");
  cuerpo.innerHTML = '<tr><td colspan="6" class="empty-state">Ejecutando mediciones reales en Java 17 para n = 200, 2.000 y 5.000...</td></tr>';
  try {
    const d = await pedirApi("/api/auditoria/medidor");
    cuerpo.innerHTML = d.filas.map(function (f) {
      return "<tr>"
        + "<td><strong>" + escaparHtml(f.operacion) + "</strong></td>"
        + '<td class="mono"><strong>' + escaparHtml(f.bigO) + "</strong></td>"
        + '<td class="mono">' + escaparHtml(f.formula) + "</td>"
        + '<td class="num mono">' + Number(f.ms200).toFixed(4) + " ms</td>"
        + '<td class="num mono">' + Number(f.ms2000).toFixed(4) + " ms</td>"
        + '<td class="num mono"><strong>' + Number(f.ms5000).toFixed(4) + " ms</strong></td>"
        + "</tr>";
    }).join("");
    mostrarAviso("Medición Big-O completada con System.nanoTime().", false);
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

async function cargarMovimientosListaDoble() {
  try {
    const d = await pedirApi("/api/auditoria/movimientos?direccion=" + encodeURIComponent(estadoApp.direccionListaDoble));
    const cuerpo = document.getElementById("movimientos-body");
    if (!d.movimientos || d.movimientos.length === 0) {
      cuerpo.innerHTML = '<tr><td colspan="5" class="empty-state">Aún no hay movimientos registrados.</td></tr>';
      return;
    }
    cuerpo.innerHTML = d.movimientos.map(function (m) {
      return "<tr>"
        + '<td class="mono"><strong>' + escaparHtml(m.id) + "</strong></td>"
        + '<td class="mono">' + escaparHtml(m.fechaHora) + "</td>"
        + '<td><span class="pill pill-neutral">' + escaparHtml(m.tipo) + "</span></td>"
        + "<td>" + escaparHtml(m.descripcion) + "</td>"
        + '<td class="num">' + (m.montoImpacto > 0 ? formatoPesos(m.montoImpacto) : "-") + "</td>"
        + "</tr>";
    }).join("");
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

async function correrPruebasSistema() {
  try {
    const d = await pedirApi("/api/auditoria/pruebas");
    document.getElementById("pruebas-resumen").textContent =
      "Resultado verificado en Java 17: " + d.pasadas + " / " + d.total + " casos superados.";
    document.getElementById("pruebas-body").innerHTML = d.pruebas.map(function (p) {
      const badge = p.paso
        ? '<span class="pill pill-vigente">[PASS]</span>'
        : '<span class="pill pill-vencido">[FAIL]</span>';
      return "<tr>"
        + "<td>" + badge + "</td>"
        + '<td class="mono"><strong>' + escaparHtml(p.codigo) + "</strong></td>"
        + "<td><strong>" + escaparHtml(p.nombre) + "</strong></td>"
        + "<td>" + escaparHtml(p.detalle) + "</td>"
        + "</tr>";
    }).join("");
  } catch (err) {
    mostrarAviso(err.message, true);
  }
}

// ============================================================================
// INICIALIZACION DE EVENTOS
// ============================================================================

document.addEventListener("DOMContentLoaded", async function () {
  // Navegacion lateral
  document.querySelectorAll(".nav-btn").forEach(function (btn) {
    btn.addEventListener("click", function () {
      cambiarVista(btn.getAttribute("data-view"));
    });
  });

  // Botones de salto rapido en Inicio
  document.querySelectorAll("[data-jump]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      cambiarVista(btn.getAttribute("data-jump"));
    });
  });

  // Retirar vencidos (Inicio y Vencimiento)
  document.getElementById("btn-inicio-retirar").addEventListener("click", retirarVencidosAccion);
  document.getElementById("btn-retirar-vencidos").addEventListener("click", retirarVencidosAccion);

  // Formulario de Venta
  document.getElementById("venta-producto").addEventListener("change", actualizarPrevisualizacionVenta);
  document.getElementById("venta-kilos").addEventListener("input", actualizarPrevisualizacionVenta);
  document.getElementById("filtro-ventas").addEventListener("input", cargarVistaVentas);

  document.getElementById("form-venta").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        codigoProducto: document.getElementById("venta-producto").value,
        cantidadKg: parseInt(document.getElementById("venta-kilos").value, 10),
        cliente: document.getElementById("venta-cliente").value
      };
      const res = await pedirApi("/api/ventas", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      mostrarAviso(res.mensaje + " Total: " + formatoPesos(res.venta.totalVenta), false);
      document.getElementById("venta-cliente").value = "";
      await cargarVistaVentas();
      cargarResumen();
    } catch (err) {
      mostrarAviso(err.message, true);
    }
  });

  // Formulario de Producto (Crear / Actualizar sin modificar historial)
  document.getElementById("prod-compra").addEventListener("input", actualizarPrevisualizacionProducto);
  document.getElementById("prod-venta").addEventListener("input", actualizarPrevisualizacionProducto);
  document.getElementById("btn-prod-limpiar").addEventListener("click", limpiarFormularioProducto);
  document.getElementById("filtro-productos").addEventListener("input", cargarVistaProductos);
  document.getElementById("filtro-categoria").addEventListener("change", cargarVistaProductos);

  document.getElementById("form-producto").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const modo = document.getElementById("prod-modo").value;
      const payload = {
        codigo: document.getElementById("prod-codigo").value,
        nombre: document.getElementById("prod-nombre").value,
        categoria: document.getElementById("prod-categoria").value,
        precioCompraKg: parseInt(document.getElementById("prod-compra").value, 10),
        precioVentaKg: parseInt(document.getElementById("prod-venta").value, 10)
      };
      const res = await pedirApi("/api/productos", {
        method: modo === "editar" ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      mostrarAviso(res.mensaje, false);
      limpiarFormularioProducto();
      await refrescarCatalogoCache();
      cargarVistaProductos();
    } catch (err) {
      mostrarAviso(err.message, true);
    }
  });

  // Formulario de Ingreso de Lote FEFO
  document.getElementById("filtro-lote-producto").addEventListener("change", cargarVistaLotes);
  document.getElementById("filtro-lote-estado").addEventListener("change", cargarVistaLotes);

  document.getElementById("form-lote").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        codigoProducto: document.getElementById("lote-producto").value,
        fechaVencimiento: document.getElementById("lote-fecha").value,
        cantidadKg: parseInt(document.getElementById("lote-kilos").value, 10),
        codigoLote: document.getElementById("lote-codigo").value
      };
      const res = await pedirApi("/api/lotes", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      mostrarAviso(res.mensaje, false);
      document.getElementById("lote-codigo").value = "";
      await refrescarCatalogoCache();
      cargarVistaLotes();
      cargarResumen();
    } catch (err) {
      mostrarAviso(err.message, true);
    }
  });

  // Pedidos y Mensajeros
  document.getElementById("form-pedido").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        cliente: document.getElementById("ped-cliente").value,
        codigoProducto: document.getElementById("ped-producto").value,
        cantidadKg: parseInt(document.getElementById("ped-kilos").value, 10),
        semana: parseInt(document.getElementById("ped-semana").value, 10)
      };
      const res = await pedirApi("/api/pedidos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      mostrarAviso(res.mensaje, false);
      document.getElementById("ped-cliente").value = "";
      cargarVistaPedidos();
      cargarResumen();
    } catch (err) {
      mostrarAviso(err.message, true);
    }
  });

  document.getElementById("btn-despachar-pedido").addEventListener("click", async function () {
    try {
      const res = await pedirApi("/api/pedidos/despachar", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ asignarMensajero: true })
      });
      mostrarAviso(res.mensaje, false);
      cargarVistaPedidos();
      cargarResumen();
    } catch (err) {
      mostrarAviso(err.message, true);
    }
  });

  document.getElementById("btn-asignar-mensajero").addEventListener("click", async function () {
    try {
      const destino = document.getElementById("turno-destino").value;
      const res = await pedirApi("/api/mensajeros/turno", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ destino: destino })
      });
      mostrarAviso(res.mensaje, false);
      document.getElementById("turno-destino").value = "";
      cargarVistaPedidos();
      cargarResumen();
    } catch (err) {
      mostrarAviso(err.message, true);
    }
  });

  // Selector de algoritmo de ordenamiento
  document.querySelectorAll("[data-alg]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      estadoApp.algoritmoMargen = btn.getAttribute("data-alg");
      document.querySelectorAll("[data-alg]").forEach(function (b) {
        b.classList.toggle("active", b === btn);
      });
      cargarVistaMargenYClientes();
    });
  });

  // Subpestañas de Auditoria Tecnica
  document.querySelectorAll("[data-audit-tab]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      const objetivo = btn.getAttribute("data-audit-tab");
      document.querySelectorAll("[data-audit-tab]").forEach(function (b) {
        b.classList.toggle("active", b === btn);
      });
      document.querySelectorAll(".audit-subpanel").forEach(function (p) {
        p.classList.toggle("active", p.id === "audit-" + objetivo);
      });
    });
  });

  document.getElementById("btn-recargar-csv").addEventListener("click", async function () {
    try {
      const res = await pedirApi("/api/auditoria/recargar-csv", { method: "POST" });
      mostrarAviso(res.mensaje, false);
      await refrescarCatalogoCache();
      cargarResumen();
      cargarMovimientosListaDoble();
    } catch (err) {
      mostrarAviso(err.message, true);
    }
  });

  document.getElementById("btn-correr-sim").addEventListener("click", correrSimulacionSemana);
  document.getElementById("btn-comparar-busq").addEventListener("click", compararBusquedas);
  document.getElementById("btn-correr-medidor").addEventListener("click", correrMedidorEnVivo);
  document.getElementById("btn-correr-pruebas").addEventListener("click", correrPruebasSistema);

  document.querySelectorAll("[data-dir]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      estadoApp.direccionListaDoble = btn.getAttribute("data-dir");
      document.querySelectorAll("[data-dir]").forEach(function (b) {
        b.classList.toggle("active", b === btn);
      });
      cargarMovimientosListaDoble();
    });
  });

  // Carga inicial al abrir la aplicacion
  await refrescarCatalogoCache();
  actualizarPrevisualizacionProducto();
  cargarResumen();
});
