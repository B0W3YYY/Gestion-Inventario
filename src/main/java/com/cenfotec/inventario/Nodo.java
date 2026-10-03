package com.cenfotec.inventario;

/**
 * Nodo de la lista enlazada simple. Almacena un Producto y la referencia
 * al siguiente nodo de la lista.
 */
public class Nodo {

    private Producto producto;
    private Nodo siguiente;

    /** Crea un nodo con el producto dado; el siguiente empieza en null. */
    public Nodo(Producto producto) {
        this.producto = producto;
        this.siguiente = null;
    }

    /** Retorna el producto almacenado en este nodo. */
    public Producto getProducto() {
        return producto;
    }

    /** Retorna el nodo siguiente, o null si es el ultimo. */
    public Nodo getSiguiente() {
        return siguiente;
    }

    /** Establece cual es el nodo siguiente. */
    public void setSiguiente(Nodo siguiente) {
        this.siguiente = siguiente;
    }
}
