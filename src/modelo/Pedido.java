package modelo;

/*
 * ============================================================================
 * CLASE: Pedido
 * ============================================================================
 * (a) Que hace: Representa un encargo recibido por telefono o WhatsApp de un
 *     tendero o restaurante del mercado. Permite llevar control del cliente,
 *     producto solicitado, kilos, estado y mensajero asignado.
 * (b) Tema de Estructuras de Datos: Unidad 1 - Tipo de Dato Abstracto (TDA,
 *     tema 1.6), usado como dato en ListaSimplePedidos (Unidad 3).
 * (c) Complejidad Big-O principal: O(1) para crear y actualizar su estado.
 * ============================================================================
 */
public class Pedido {

    // === 1. ATRIBUTOS ===
    private final String codigoPedido;
    private final String cliente;
    private final String codigoProducto;
    private final int cantidadKg;
    private final int semana; // 1 o 2, usado para clientes frecuentes (S1 y S2)
    private final String fechaHora;
    private String estado; // "Pendiente", "Despachado" o "Cancelado"
    private String mensajeroAsignado;

    // === 2. CONSTRUCTOR ===
    // Recibe los datos del pedido entrante y lo deja inicialmente en estado "Pendiente".
    public Pedido(String codigoPedido, String cliente, String codigoProducto,
                  int cantidadKg, int semana, String fechaHora) {
        this.codigoPedido = codigoPedido;
        this.cliente = cliente;
        this.codigoProducto = codigoProducto;
        this.cantidadKg = cantidadKg;
        this.semana = semana;
        this.fechaHora = fechaHora;
        this.estado = "Pendiente";
        this.mensajeroAsignado = "Sin asignar";
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Marcar pedido como despachado ---
    // Recibe el nombre del mensajero asignado; cambia el estado a "Despachado".
    public void marcarDespachado(String nombreMensajero) {
        this.estado = "Despachado";
        if (nombreMensajero != null && !nombreMensajero.trim().isEmpty()) {
            this.mensajeroAsignado = nombreMensajero;
        }
    }

    // --- 2. Marcar pedido como cancelado ---
    // No recibe parametros; cambia el estado del pedido a "Cancelado".
    public void marcarCancelado() {
        this.estado = "Cancelado";
    }

    // --- 3. Representacion en texto ---
    // No recibe parametros; devuelve una linea resumen del pedido.
    @Override
    public String toString() {
        return codigoPedido + " | " + cliente + " | " + codigoProducto
                + " (" + cantidadKg + " kg) | " + estado;
    }

    // === 4. GETTERS Y SETTERS ===

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public String getCliente() {
        return cliente;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public int getCantidadKg() {
        return cantidadKg;
    }

    public int getSemana() {
        return semana;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensajeroAsignado() {
        return mensajeroAsignado;
    }

    public void setMensajeroAsignado(String mensajeroAsignado) {
        this.mensajeroAsignado = mensajeroAsignado;
    }
}
