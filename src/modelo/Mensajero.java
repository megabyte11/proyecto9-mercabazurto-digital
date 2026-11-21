package modelo;

/*
 * ============================================================================
 * CLASE: Mensajero
 * ============================================================================
 * (a) Que hace: Representa a un domiciliario o carretillero del puesto en el
 *     Mercado de Bazurto (como Carlos, Dairo y Edgar) y lleva el conteo exacto
 *     de cuantos viajes ha realizado en el dia para verificar la equidad.
 * (b) Tema de Estructuras de Datos: Unidad 1 - Tipo de Dato Abstracto (TDA,
 *     tema 1.6), gestionado dentro de ListaCircular (Unidad 3, Round Robin).
 * (c) Complejidad Big-O principal: O(1) para consultar o sumar un viaje.
 * ============================================================================
 */
public class Mensajero {

    // === 1. ATRIBUTOS ===
    private final String codigo;
    private final String nombre;
    private final String telefono;
    private int viajesRealizados;
    private String ultimoDestino;

    // === 2. CONSTRUCTOR ===
    // Recibe identificacion, nombre y telefono del mensajero; arranca con 0 viajes.
    public Mensajero(String codigo, String nombre, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.telefono = telefono;
        this.viajesRealizados = 0;
        this.ultimoDestino = "Disponible en el puesto";
    }

    // === 3. METODOS PRINCIPALES ===

    // --- 1. Registrar un viaje asignado ---
    // Recibe la referencia del pedido o destino; aumenta el contador de viajes en 1.
    public void registrarViaje(String destino) {
        this.viajesRealizados = this.viajesRealizados + 1;
        if (destino != null && !destino.trim().isEmpty()) {
            this.ultimoDestino = destino;
        }
    }

    // --- 2. Reiniciar contador de viajes ---
    // No recibe parametros; vuelve el conteo a 0 al iniciar una jornada nueva.
    public void reiniciarViajes() {
        this.viajesRealizados = 0;
        this.ultimoDestino = "Disponible en el puesto";
    }

    // --- 3. Representacion en texto ---
    // No recibe parametros; devuelve los datos basicos y viajes del mensajero.
    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + viajesRealizados + " viajes)";
    }

    // === 4. GETTERS Y SETTERS ===

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public int getViajesRealizados() {
        return viajesRealizados;
    }

    public void setViajesRealizados(int viajesRealizados) {
        this.viajesRealizados = viajesRealizados;
    }

    public String getUltimoDestino() {
        return ultimoDestino;
    }

    public void setUltimoDestino(String ultimoDestino) {
        this.ultimoDestino = ultimoDestino;
    }
}
