package com.cenfotec.inventario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Representa un producto del sistema de ventas en linea.
 * La cantidad se interpreta segun el contexto: unidades en el carrito del
 * cliente o unidades en el inventario de la tienda (segun se use en avances
 * posteriores del proyecto).
 */
public class Producto {

    private String nombre;
    private double precio;
    private String categoria;
    private LocalDate fechaVencimiento; // puede ser null si no aplica
    private int cantidad;
    private final ArrayList<String> listaImagenes;

    /** Crea un producto con sus datos; la lista de imagenes empieza vacia. */
    public Producto(String nombre, double precio, String categoria, LocalDate fechaVencimiento, int cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.fechaVencimiento = fechaVencimiento;
        this.cantidad = cantidad;
        this.listaImagenes = new ArrayList<>();
    }

    /** Retorna el nombre del producto. */
    public String getNombre() {
        return nombre;
    }

    /** Cambia el nombre del producto. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Retorna el precio unitario del producto. */
    public double getPrecio() {
        return precio;
    }

    /** Cambia el precio unitario del producto. */
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /** Retorna la categoria del producto. */
    public String getCategoria() {
        return categoria;
    }

    /** Cambia la categoria del producto. */
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    /** Retorna la fecha de vencimiento, o null si no aplica. */
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    /** Cambia la fecha de vencimiento. Puede ser null si no aplica. */
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    /** Retorna la cantidad de unidades del producto. */
    public int getCantidad() {
        return cantidad;
    }

    /** Cambia la cantidad de unidades del producto. */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /** Retorna la lista de rutas de imagenes del producto. */
    public ArrayList<String> getListaImagenes() {
        return listaImagenes;
    }

    /** Agrega una ruta de imagen a la lista de imagenes del producto. */
    public void agregarImagen(String rutaImagen) {
        this.listaImagenes.add(rutaImagen);
    }

    /** Calcula el costo total de este producto segun su cantidad (precio * cantidad). */
    public double calcularCostoTotal() {
        return precio * cantidad;
    }

    /** Datos del producto en una linea y sin tildes, para mostrarlos en la consola (precio en colones). */
    @Override
    public String toString() {
        String vence = (fechaVencimiento != null) ? fechaVencimiento.format(ReglasProducto.FORMATO_FECHA) : "no vence";
        return String.format(Locale.US, "%s (%s) | Precio: %,.2f | Cantidad: %,d | Vence: %s | Imagenes: %d",
                ListaProductos.sinTildes(nombre), ListaProductos.sinTildes(categoria), precio, cantidad, vence,
                listaImagenes.size());
    }
}
