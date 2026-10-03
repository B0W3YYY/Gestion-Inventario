package com.cenfotec.inventario;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Lista enlazada simple de Productos. Implementa las operaciones habituales:
 * insercion al inicio y al final, busqueda, modificacion, eliminacion y los
 * calculos del reporte de costos (todos recorriendo la lista nodo por nodo).
 */
public class ListaProductos {

    private Nodo cabeza;
    private int tamanio;

    /** Crea una lista vacia. */
    public ListaProductos() {
        this.cabeza = null;
        this.tamanio = 0;
    }

    /** Retorna cuantos productos hay en la lista. */
    public int getTamanio() {
        return tamanio;
    }

    /** Indica si la lista no tiene productos. */
    public boolean estaVacia() {
        return cabeza == null;
    }

    /** Retorna el primer nodo (o null si la lista esta vacia) para poder recorrer la lista. */
    public Nodo getCabeza() {
        return cabeza;
    }

    /** Inserta un producto al inicio de la lista. */
    public void insertarAlInicio(Producto producto) {
        Nodo nuevo = new Nodo(producto);
        nuevo.setSiguiente(cabeza);
        cabeza = nuevo;
        tamanio++;
    }

    /** Inserta un producto al final de la lista. */
    public void insertarAlFinal(Producto producto) {
        Nodo nuevo = new Nodo(producto);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            Nodo actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        tamanio++;
    }

    /**
     * Quita las tildes de un texto (ej. "Lácteos" -> "Lacteos"). Se usa para comparar
     * nombres y para que la consola muestre todo sin tildes.
     * La ñ se conserva porque es otra letra ("año" no es lo mismo que "ano").
     */
    public static String sinTildes(String texto) {
        String conTilde = "áéíóúüÁÉÍÓÚÜ";
        String sinTilde = "aeiouuAEIOUU";
        for (int i = 0; i < conTilde.length(); i++) {
            texto = texto.replace(conTilde.charAt(i), sinTilde.charAt(i));
        }
        return texto;
    }

    /**
     * Prepara un texto para compararlo: quita espacios al inicio y al final,
     * deja un solo espacio entre palabras, pasa a minusculas y quita las tildes.
     * Asi "Cafe      molido" y "café molido" se consideran el mismo nombre.
     */
    public static String normalizar(String texto) {
        return sinTildes(texto.trim().replaceAll("\\s+", " ").toLowerCase());
    }

    /** Compara dos nombres sin importar mayusculas, tildes ni espacios repetidos. */
    private static boolean mismoNombre(String nombre, String otro) {
        return otro != null && normalizar(nombre).equals(normalizar(otro));
    }

    /** Busca un producto por nombre (sin importar mayusculas, tildes ni espacios). Retorna null si no se encuentra. */
    public Producto buscarPorNombre(String nombre) {
        Nodo actual = cabeza;
        while (actual != null) {
            if (mismoNombre(actual.getProducto().getNombre(), nombre)) {
                return actual.getProducto();
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    /**
     * Modifica los datos de un producto existente (identificado por nombre),
     * incluido su nombre. Quien llama debe verificar que nuevoNombre no este repetido.
     * Un valor null en nuevaFechaVencimiento significa "no aplica" y se
     * establece explicitamente; para conservar la fecha actual sin cambios,
     * el llamador debe pasar el valor que ya tenia el producto.
     * Retorna true si se encontro y modifico, false en caso contrario.
     */
    public boolean modificarProducto(String nombre, String nuevoNombre, double nuevoPrecio, String nuevaCategoria,
                                      LocalDate nuevaFechaVencimiento, int nuevaCantidad) {
        Producto producto = buscarPorNombre(nombre);
        if (producto == null) {
            return false;
        }
        producto.setNombre(nuevoNombre);
        producto.setPrecio(nuevoPrecio);
        producto.setCategoria(nuevaCategoria);
        producto.setFechaVencimiento(nuevaFechaVencimiento);
        producto.setCantidad(nuevaCantidad);
        return true;
    }

    /**
     * Agrega una imagen a un producto existente (identificado por nombre).
     * Retorna true si se encontro el producto, false en caso contrario.
     */
    public boolean agregarImagenAProducto(String nombre, String rutaImagen) {
        Producto producto = buscarPorNombre(nombre);
        if (producto == null) {
            return false;
        }
        producto.agregarImagen(rutaImagen);
        return true;
    }

    /**
     * Elimina un producto de la lista por nombre.
     * Retorna true si se elimino, false si no se encontro.
     */
    public boolean eliminarProducto(String nombre) {
        if (estaVacia()) {
            return false;
        }

        if (mismoNombre(cabeza.getProducto().getNombre(), nombre)) {
            cabeza = cabeza.getSiguiente();
            tamanio--;
            return true;
        }

        Nodo anterior = cabeza;
        Nodo actual = cabeza.getSiguiente();
        while (actual != null) {
            if (mismoNombre(actual.getProducto().getNombre(), nombre)) {
                anterior.setSiguiente(actual.getSiguiente());
                tamanio--;
                return true;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return false;
    }

    /** Retorna el producto que esta en la posicion dada (1 = cabeza), o null si esa posicion no existe. */
    public Producto obtenerProducto(int posicion) {
        if (posicion < 1) {
            return null;
        }
        Nodo actual = cabeza;
        for (int i = 1; i < posicion && actual != null; i++) {
            actual = actual.getSiguiente();
        }
        return actual == null ? null : actual.getProducto();
    }

    /** Recorre la lista e imprime cada producto numerado segun su posicion. */
    public void imprimirProductos() {
        if (estaVacia()) {
            System.out.println("La lista de productos esta vacia.");
            return;
        }
        int posicion = 1;
        Nodo actual = cabeza;
        while (actual != null) {
            System.out.println(posicion + ". " + actual.getProducto());
            actual = actual.getSiguiente();
            posicion++;
        }
    }

    /**
     * Recorre la lista e imprime el reporte de costos: el costo total de cada producto
     * en funcion de su cantidad (precio x cantidad) y el costo total acumulado de la lista.
     */
    public void imprimirReporteCostos() {
        if (estaVacia()) {
            System.out.println("La lista de productos esta vacia.");
            return;
        }
        System.out.println("=== Reporte de costos (montos en colones) ===");
        double costoAcumulado = 0.0;
        int posicion = 1;
        Nodo actual = cabeza;
        while (actual != null) {
            Producto p = actual.getProducto();
            double costoProducto = p.calcularCostoTotal();
            System.out.printf(Locale.US, "%d. %s%n   %,d %s x %,.2f = costo total %,.2f%n",
                    posicion, sinTildes(p.getNombre()), p.getCantidad(), p.getCantidad() == 1 ? "unidad" : "unidades",
                    p.getPrecio(), costoProducto);
            costoAcumulado += costoProducto;
            actual = actual.getSiguiente();
            posicion++;
        }
        System.out.printf(Locale.US, "Costo total acumulado de la lista: %,.2f%n", costoAcumulado);
    }

    /** Recorre la lista y suma el costo total (precio x cantidad) de todos los productos. */
    public double calcularCostoTotal() {
        double costoAcumulado = 0.0;
        Nodo actual = cabeza;
        while (actual != null) {
            costoAcumulado += actual.getProducto().calcularCostoTotal();
            actual = actual.getSiguiente();
        }
        return costoAcumulado;
    }

    /** Recorre la lista y suma las unidades de todos los productos. */
    public long contarUnidades() {
        long unidades = 0;
        Nodo actual = cabeza;
        while (actual != null) {
            unidades += actual.getProducto().getCantidad();
            actual = actual.getSiguiente();
        }
        return unidades;
    }

    /** Cuenta los productos que vencen en fechaLimite o antes (incluye los ya vencidos). */
    public int contarPorVencer(LocalDate fechaLimite) {
        int contador = 0;
        Nodo actual = cabeza;
        while (actual != null) {
            LocalDate fecha = actual.getProducto().getFechaVencimiento();
            if (fecha != null && !fecha.isAfter(fechaLimite)) {
                contador++;
            }
            actual = actual.getSiguiente();
        }
        return contador;
    }
}
