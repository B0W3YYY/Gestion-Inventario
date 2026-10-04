package com.cenfotec.inventario;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Clase principal del programa. Contiene la rutina main() y el menu() de consola
 * que permite usar la ListaProductos: insertar al inicio o al final, modificar,
 * agregar imagenes, eliminar, listar, buscar e imprimir el reporte de costos.
 * Desde el menu tambien se puede abrir la interfaz grafica, que trabaja sobre
 * la misma lista. Las reglas de los datos estan en ReglasProducto, compartidas
 * con la interfaz grafica.
 * La consola muestra todo sin tildes, porque algunas consolas no las muestran bien.
 */
public class Main {

    private static Scanner scanner;

    /** Crea la lista enlazada de productos y arranca el menu de consola. */
    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        ListaProductos listaProductos = new ListaProductos();
        menu(listaProductos);
    }

    /** Ciclo del menu principal; termina cuando el usuario elige salir. */
    private static void menu(ListaProductos lista) {
        int opcion;
        do {
            System.out.println();
            System.out.println("===== Gestion de Inventarios =====");
            System.out.println("Productos en la lista: " + lista.getTamanio() + "\n");
            System.out.println("1. Insertar producto al inicio");
            System.out.println("2. Insertar producto al final");
            System.out.println("3. Modificar producto");
            System.out.println("4. Agregar imagen a un producto");
            System.out.println("5. Eliminar producto");
            System.out.println("6. Listar productos");
            System.out.println("7. Buscar producto por nombre");
            System.out.println("8. Reporte de costos");
            System.out.println("9. Abrir interfaz grafica");
            System.out.println("0. Salir");
            opcion = leerValor("Seleccione una opcion", null, texto -> convertirNumero(texto, 0, 9));

            switch (opcion) {
                case 1:
                    insertarProducto(lista, true);
                    break;
                case 2:
                    insertarProducto(lista, false);
                    break;
                case 3:
                    modificarProducto(lista);
                    break;
                case 4:
                    agregarImagen(lista);
                    break;
                case 5:
                    eliminarProducto(lista);
                    break;
                case 6:
                    System.out.println("--- Lista de productos ---");
                    lista.imprimirProductos();
                    break;
                case 7:
                    buscarProducto(lista);
                    break;
                case 8:
                    lista.imprimirReporteCostos();
                    break;
                case 9:
                    abrirInterfazGrafica(lista);
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    break;
            }
        } while (opcion != 0);
    }

    /** Pide los datos de un producto nuevo y lo inserta al inicio o al final de la lista. */
    private static void insertarProducto(ListaProductos lista, boolean alInicio) {
        System.out.println(alInicio ? "--- Insertar producto al inicio ---" : "--- Insertar producto al final ---");
        int categoria = elegirOpcion("Categoria", ReglasProducto.CATEGORIAS, -1);
        String tipo = ReglasProducto.TIPOS[categoria][elegirOpcion("Tipo de producto", ReglasProducto.TIPOS[categoria], -1)];
        String nombre = leerValor("Detalle del nombre, va despues de \"" + paraConsola(tipo)
                + "\" (ej. entera 1 L; Enter si no tiene)", null, texto -> ReglasProducto.armarNombre(tipo, texto, lista, null));
        double precio = leerValor("Precio unitario en colones (ej. 1500.50)", null, ReglasProducto::convertirPrecio);
        int cantidad = leerValor("Cantidad", null, ReglasProducto::convertirCantidad);
        LocalDate fecha = leerValor("Fecha de vencimiento dd/mm/aaaa (Enter si no vence)", null, ReglasProducto::convertirFecha);

        Producto producto = new Producto(nombre, precio, ReglasProducto.CATEGORIAS[categoria], fecha, cantidad);
        if (alInicio) {
            lista.insertarAlInicio(producto);
        } else {
            lista.insertarAlFinal(producto);
        }
        System.out.println("Producto agregado " + (alInicio ? "al inicio" : "al final") + " de la lista: " + producto);
    }

    /** Elige un producto y cambia sus datos; Enter conserva el valor actual de cada dato. */
    private static void modificarProducto(ListaProductos lista) {
        System.out.println("--- Modificar producto ---");
        Producto producto = elegirProducto(lista);
        if (producto == null) {
            return;
        }
        System.out.println("Datos actuales: " + producto);
        System.out.println("Presione Enter para conservar el valor que aparece entre [ ].");

        // El nombre es tipo + detalle (ej. "Leche entera"): se separa para poder cambiar cada parte.
        String[] partes = producto.getNombre().split(" ", 2);
        int categoriaActual = Arrays.asList(ReglasProducto.CATEGORIAS).indexOf(producto.getCategoria());
        int categoria = elegirOpcion("Categoria", ReglasProducto.CATEGORIAS, categoriaActual);
        String[] tipos = ReglasProducto.TIPOS[categoria];
        // Si cambia la categoria, el tipo actual ya no aplica y hay que elegir uno nuevo.
        int tipoActual = (categoria == categoriaActual) ? Arrays.asList(tipos).indexOf(partes[0]) : -1;
        String tipo = tipos[elegirOpcion("Tipo de producto", tipos, tipoActual)];
        String nombre = leerValor("Detalle del nombre (N/A = sin detalle)", partes.length > 1 ? partes[1] : "N/A",
                texto -> ReglasProducto.armarNombre(tipo, texto, lista, producto));
        double precio = leerValor("Precio unitario en colones",
                String.format(Locale.US, "%.2f", producto.getPrecio()), ReglasProducto::convertirPrecio);
        int cantidad = leerValor("Cantidad", String.valueOf(producto.getCantidad()), ReglasProducto::convertirCantidad);
        LocalDate vence = producto.getFechaVencimiento();
        LocalDate fecha = leerValor("Fecha de vencimiento dd/mm/aaaa (N/A = no vence)",
                vence == null ? "N/A" : vence.format(ReglasProducto.FORMATO_FECHA), ReglasProducto::convertirFecha);

        lista.modificarProducto(producto.getNombre(), nombre, precio, ReglasProducto.CATEGORIAS[categoria], fecha, cantidad);
        System.out.println("Producto actualizado correctamente: " + producto);
    }

    /** Muestra las imagenes de la carpeta "imagenes" del proyecto y agrega la elegida al producto. */
    private static void agregarImagen(ListaProductos lista) {
        System.out.println("--- Agregar imagen a un producto ---");
        Producto producto = elegirProducto(lista);
        if (producto == null) {
            return;
        }
        File[] imagenes = new File(ReglasProducto.CARPETA_IMAGENES).listFiles(
                archivo -> archivo.isFile() && archivo.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif)"));
        if (imagenes == null || imagenes.length == 0) {
            System.out.println("No hay imagenes en la carpeta \"" + ReglasProducto.CARPETA_IMAGENES
                    + "\" del proyecto. Copie ahi la imagen y vuelva a intentarlo.");
            return;
        }
        Arrays.sort(imagenes);
        System.out.println("Imagenes de la carpeta \"" + ReglasProducto.CARPETA_IMAGENES + "\":");
        for (int i = 0; i < imagenes.length; i++) {
            System.out.println("  " + (i + 1) + ". " + paraConsola(imagenes[i].getName()));
        }
        int numero = leerValor("Numero de la imagen (0 para cancelar)", null,
                texto -> convertirNumero(texto, 0, imagenes.length));
        if (numero == 0) {
            return;
        }
        try {
            String ruta = ReglasProducto.convertirRutaImagen(imagenes[numero - 1], producto);
            lista.agregarImagenAProducto(producto.getNombre(), ruta);
            System.out.println("Imagen agregada correctamente: " + paraConsola(ruta));
        } catch (IllegalArgumentException e) {
            System.out.println(paraConsola(e.getMessage()));
        }
    }

    /** Elige un producto, pide confirmacion y lo elimina de la lista. */
    private static void eliminarProducto(ListaProductos lista) {
        System.out.println("--- Eliminar producto ---");
        Producto producto = elegirProducto(lista);
        if (producto == null) {
            return;
        }
        String nombre = paraConsola(producto.getNombre());
        boolean confirmado = leerValor("Esta seguro de que desea eliminar \"" + nombre + "\"? (s/n)",
                null, Main::convertirSiNo);
        if (!confirmado) {
            System.out.println("No se elimino el producto.");
            return;
        }
        lista.eliminarProducto(producto.getNombre());
        System.out.println("Producto \"" + nombre + "\" eliminado correctamente.");
    }

    /** Busca un producto con buscarPorNombre (no importan tildes, mayusculas ni espacios de mas). */
    private static void buscarProducto(ListaProductos lista) {
        System.out.println("--- Buscar producto ---");
        if (lista.estaVacia()) {
            System.out.println("La lista de productos esta vacia.");
            return;
        }
        String nombre = leerValor("Nombre completo del producto", null, texto -> {
            if (texto.isEmpty()) {
                throw new IllegalArgumentException("Escriba el nombre.");
            }
            return texto;
        });
        Producto producto = lista.buscarPorNombre(nombre);
        if (producto == null) {
            System.out.println("No se encontro ningun producto llamado \"" + paraConsola(nombre)
                    + "\". Use la opcion 6 para ver los nombres.");
        } else {
            System.out.println("Encontrado: " + producto);
        }
    }

    /**
     * Abre la interfaz grafica con la misma lista. La consola espera hasta que se cierre
     * la ventana, asi nunca se usan las dos a la vez; al cerrarla se vuelve a este menu.
     */
    private static void abrirInterfazGrafica(ListaProductos lista) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Este equipo no tiene pantalla grafica; use el menu de consola.");
            return;
        }
        System.out.println("Abriendo la interfaz grafica. Cierrela para volver a este menu.");
        CountDownLatch ventanaCerrada = new CountDownLatch(1);
        // Swing pide crear las ventanas en su propio hilo (Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarTema();
            VentanaPrincipal ventana = new VentanaPrincipal(lista);
            ventana.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    ventanaCerrada.countDown(); // avisa a la consola que puede continuar
                }
            });
            ventana.setVisible(true);
        });
        try {
            ventanaCerrada.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Interfaz grafica cerrada. Productos en la lista: " + lista.getTamanio());
    }

    // ------------------------------------------------------------------
    // Lectura de datos
    // ------------------------------------------------------------------

    /** Muestra la lista numerada y retorna el producto elegido, o null si esta vacia o se cancela con 0. */
    private static Producto elegirProducto(ListaProductos lista) {
        if (lista.estaVacia()) {
            System.out.println("La lista de productos esta vacia.");
            return null;
        }
        lista.imprimirProductos();
        int posicion = leerValor("Numero del producto (0 para cancelar)", null,
                texto -> convertirNumero(texto, 0, lista.getTamanio()));
        return lista.obtenerProducto(posicion); // la posicion 0 no existe: retorna null (cancelar)
    }

    /**
     * Muestra las opciones numeradas y retorna el indice elegido (0 = la primera).
     * Si actual es un indice valido (al modificar), Enter conserva esa opcion.
     */
    private static int elegirOpcion(String titulo, String[] opciones, int actual) {
        System.out.println(titulo + ":");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println("  " + (i + 1) + ". " + paraConsola(opciones[i]) + (i == actual ? "  (actual)" : ""));
        }
        String textoActual = (actual >= 0) ? String.valueOf(actual + 1) : null;
        return leerValor("Numero de la opcion", textoActual, texto -> convertirNumero(texto, 1, opciones.length)) - 1;
    }

    /**
     * Pregunta un dato hasta que sea valido. convertir revisa el texto y lo transforma, o lanza
     * IllegalArgumentException con el mensaje para el usuario. Al modificar, textoActual es el
     * valor que ya tenia el producto: si el usuario solo presiona Enter, se usa ese valor
     * (el original; en pantalla se muestra sin tildes).
     */
    private static <T> T leerValor(String pregunta, String textoActual, Function<String, T> convertir) {
        System.out.print(pregunta + (textoActual != null ? " [" + paraConsola(textoActual) + "]" : "") + ": ");
        while (true) {
            String texto = leerTexto();
            if (texto.isEmpty() && textoActual != null) {
                texto = textoActual; // Enter = conservar el valor actual
            }
            try {
                return convertir.apply(texto);
            } catch (IllegalArgumentException e) {
                System.out.print(paraConsola(e.getMessage()) + " Intente de nuevo: ");
            }
        }
    }

    /** Lee una linea de texto y le quita los espacios al inicio y al final. */
    private static String leerTexto() {
        // Si la entrada se termina (Ctrl+Z / Ctrl+D) se sale en orden,
        // en vez de caerse con una NoSuchElementException.
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("No hay mas datos de entrada. Saliendo del sistema...");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** Convierte el texto en un numero entero entre min y max (o lanza el error para volver a preguntar). */
    private static int convertirNumero(String texto, int min, int max) {
        int numero;
        try {
            numero = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            numero = min - 1; // no es un numero: se trata como fuera de rango
        }
        if (numero < min || numero > max) {
            throw new IllegalArgumentException("Escriba un numero del " + min + " al " + max + ".");
        }
        return numero;
    }

    /** Convierte "s" en true y "n" en false (sin importar mayusculas). */
    private static boolean convertirSiNo(String texto) {
        if (texto.equalsIgnoreCase("s")) {
            return true;
        }
        if (texto.equalsIgnoreCase("n")) {
            return false;
        }
        throw new IllegalArgumentException("Responda s (si) o n (no).");
    }

    /**
     * Prepara un texto que no se escribio aqui (nombres de productos, archivos y mensajes
     * de ReglasProducto, que tambien usa la ventana) para la consola: sin tildes y con
     * comillas normales en vez de « ».
     */
    private static String paraConsola(String texto) {
        return ListaProductos.sinTildes(texto).replace('«', '"').replace('»', '"');
    }
}
