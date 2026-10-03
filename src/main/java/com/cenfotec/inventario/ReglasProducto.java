package com.cenfotec.inventario;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Reglas de los productos que comparten el menu de consola y la interfaz grafica:
 * el catalogo de categorias y tipos, los limites y la conversion/validacion de
 * cada dato. Al estar en un solo lugar, las dos interfaces aceptan y rechazan
 * exactamente lo mismo.
 * Cada metodo "convertir" retorna el dato listo para guardar o lanza una
 * IllegalArgumentException con un mensaje claro para el usuario.
 */
public class ReglasProducto {

    // Catalogo: CATEGORIAS[i] tiene los tipos de producto TIPOS[i].
    // Cada tipo esta en una sola categoria, asi "Leche" solo puede ser "Lácteos".
    public static final String[] CATEGORIAS =
            {"Bebidas", "Carnes", "Enlatados", "Frutas y verduras", "Granos", "Lácteos", "Limpieza", "Panadería"};
    public static final String[][] TIPOS = {
            {"Agua", "Café", "Jugo", "Refresco", "Té"},
            {"Cerdo", "Pescado", "Pollo", "Res"},
            {"Atún", "Sardinas", "Sopa"},
            {"Banano", "Cebolla", "Manzana", "Papa", "Tomate"},
            {"Arroz", "Frijoles", "Garbanzos", "Lentejas"},
            {"Leche", "Mantequilla", "Natilla", "Queso", "Yogurt"},
            {"Cloro", "Detergente", "Jabón"},
            {"Galletas", "Pan", "Tortillas"}};

    // Limites razonables: evitan valores absurdos (ej. 1e308) y mantienen el
    // costo total (precio x cantidad) exacto al centimo usando double.
    public static final int PRECIO_MAXIMO = 10_000_000;
    public static final int CANTIDAD_MAXIMA = 100_000;
    public static final int LARGO_MAXIMO = 50;

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Carpeta del proyecto donde deben estar las imagenes (lo pide la consigna). */
    public static final String CARPETA_IMAGENES = "imagenes";

    /**
     * Arma el nombre con el tipo y el detalle (ej. "Leche" + "entera 1 L") y revisa que sea valido.
     * Un detalle vacio o "N/A" significa que el nombre es solo el tipo.
     * productoEditado es el producto que se esta modificando (o null si es nuevo), para que
     * no choque consigo mismo al revisar si el nombre ya existe.
     */
    public static String armarNombre(String tipo, String detalle, ListaProductos lista, Producto productoEditado) {
        detalle = detalle.trim().replaceAll("\\s+", " "); // sin espacios de mas
        if (detalle.equalsIgnoreCase("N/A")) {
            detalle = "";
        }
        String detalleNormalizado = ListaProductos.normalizar(detalle);
        String tipoNormalizado = ListaProductos.normalizar(tipo);
        if (detalleNormalizado.equals(tipoNormalizado) || detalleNormalizado.startsWith(tipoNormalizado + " ")) {
            throw new IllegalArgumentException("No repita «" + tipo + "»: escriba solo el detalle, ej. «entera 1 L».");
        }
        String nombre = detalle.isEmpty() ? tipo : tipo + " " + detalle;
        if (nombre.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("El nombre completo puede tener como máximo " + LARGO_MAXIMO + " caracteres.");
        }
        // El nombre identifica al producto al buscar, editar y eliminar, por eso no se repite.
        // buscarPorNombre no distingue tildes ni mayusculas: "Cafe" y "Café" serian el mismo.
        Producto existente = lista.buscarPorNombre(nombre);
        if (existente != null && existente != productoEditado) {
            throw new IllegalArgumentException("Ya existe «" + existente.getNombre() + "» (no se distinguen tildes ni mayúsculas).");
        }
        return nombre;
    }

    /** Convierte el texto en un precio entre 0 y PRECIO_MAXIMO, redondeado a 2 decimales (centimos). */
    public static double convertirPrecio(String texto) {
        texto = texto.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Ingrese el precio.");
        }
        double valor;
        try {
            valor = Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un número válido.");
        }
        // NaN ("not a number") no es menor ni mayor que nada, por eso se revisa aparte.
        if (Double.isNaN(valor)) {
            throw new IllegalArgumentException("El precio debe ser un número válido.");
        }
        if (valor < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (valor > PRECIO_MAXIMO) {
            throw new IllegalArgumentException("El precio máximo es "
                    + String.format(Locale.US, "%,d", PRECIO_MAXIMO) + " colones.");
        }
        return Math.round(valor * 100) / 100.0;
    }

    /** Convierte el texto en una cantidad entera entre 0 y CANTIDAD_MAXIMA. */
    public static int convertirCantidad(String texto) {
        texto = texto.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Ingrese la cantidad.");
        }
        long valor;
        try {
            valor = Long.parseLong(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero.");
        }
        if (valor < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        if (valor > CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException("La cantidad máxima es " + String.format(Locale.US, "%,d", CANTIDAD_MAXIMA) + ".");
        }
        return (int) valor;
    }

    /** Convierte el texto (dd/MM/yyyy) en fecha. Vacio o "N/A" = el producto no vence (null). */
    public static LocalDate convertirFecha(String texto) {
        texto = texto.trim();
        if (texto.isEmpty() || texto.equalsIgnoreCase("N/A")) {
            return null;
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use el formato dd/mm/aaaa, por ejemplo 15/08/2027.");
        }
        // parse "ajusta" fechas que no existen (31/02/2025 -> 28/02/2025).
        // Si al volver a escribirla no queda igual, la fecha no existia.
        if (!fecha.format(FORMATO_FECHA).equals(texto)) {
            throw new IllegalArgumentException("Esa fecha no existe en el calendario.");
        }
        return fecha;
    }

    /**
     * Revisa que el archivo pueda agregarse como imagen del producto y retorna la ruta
     * que se guarda, relativa al proyecto (ej. "imagenes/queso.jpg").
     * Reglas: debe estar dentro de la carpeta "imagenes" del proyecto, ser una imagen
     * real y no estar repetida en el producto.
     */
    public static String convertirRutaImagen(File archivo, Producto producto) {
        Path proyecto = Path.of("").toAbsolutePath();
        Path imagen = archivo.toPath().toAbsolutePath().normalize();
        if (!imagen.startsWith(proyecto.resolve(CARPETA_IMAGENES))) {
            throw new IllegalArgumentException("La imagen debe estar dentro de la carpeta «" + CARPETA_IMAGENES + "» del proyecto.");
        }
        if (leerImagen(archivo) == null) {
            throw new IllegalArgumentException("El archivo no existe o no es una imagen válida.");
        }
        // Siempre con "/" para que imagenes\a.jpg e imagenes/a.jpg sean la misma ruta.
        String ruta = proyecto.relativize(imagen).toString().replace('\\', '/');
        // Sin distinguir mayusculas, porque en Windows IMAGENES/A.JPG es el mismo archivo que imagenes/a.jpg.
        for (String existente : producto.getListaImagenes()) {
            if (existente.equalsIgnoreCase(ruta)) {
                throw new IllegalArgumentException("El producto ya tiene esa imagen.");
            }
        }
        return ruta;
    }

    /** Lee el archivo como imagen; retorna null si no existe o no es una imagen. */
    public static BufferedImage leerImagen(File archivo) {
        try {
            return archivo.isFile() ? ImageIO.read(archivo) : null;
        } catch (IOException e) {
            return null;
        }
    }
}
