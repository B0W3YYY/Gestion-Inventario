package com.cenfotec.inventario;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Busqueda guiada: un menu de casillas por niveles. Primero la categoria,
 * despues el tipo y luego las demas palabras del nombre
 * (ej. Bebidas > Café > molido). Al marcar una opcion desaparecen las demas
 * de ese nivel y aparecen las del siguiente. Se puede seguir hasta llegar a
 * un producto o detenerse a medias: la tabla muestra los que coinciden.
 * Las opciones se calculan recorriendo la lista enlazada desde la cabeza.
 */
public class PanelBusquedaGuiada extends JPanel {

    private static final String[] TITULOS_NIVEL = {"Categoría", "Tipo", "Siguiente palabra"};

    private final ListaProductos lista;
    private final Runnable alCambiar;         // avisa a la ventana que debe volver a dibujar la tabla
    private String[] marcadas = new String[0]; // opciones marcadas, normalizadas (ej. {"bebidas", "cafe"})

    private final JPanel panelCasillas = new JPanel();
    private final JLabel etiquetaResultado = Estilo.crearEtiqueta(" ", Font.PLAIN, 12, Estilo.TEXTO_SECUNDARIO);
    private final JButton botonQuitar = Estilo.crearBoton("Quitar filtro", Estilo.PANEL, Estilo.TEXTO);

    public PanelBusquedaGuiada(ListaProductos lista, Runnable alCambiar) {
        super(new BorderLayout(0, 10));
        this.lista = lista;
        this.alCambiar = alCambiar;
        setBackground(new Color(0xF9FAFB));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 1, Estilo.BORDE),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        setPreferredSize(new Dimension(220, 0));

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);
        titulos.add(Estilo.crearEtiqueta("Búsqueda guiada", Font.BOLD, 14, Estilo.TEXTO));
        titulos.add(Estilo.crearEtiqueta("Filtre marcando opciones.", Font.PLAIN, 12, Estilo.TEXTO_SECUNDARIO));

        // Las casillas van arriba de un contenedor para que no se estiren hacia abajo.
        panelCasillas.setLayout(new BoxLayout(panelCasillas, BoxLayout.Y_AXIS));
        panelCasillas.setOpaque(false);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(panelCasillas, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(contenedor, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        botonQuitar.addActionListener(e -> {
            limpiar();
            alCambiar.run();
        });
        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.setOpaque(false);
        inferior.add(etiquetaResultado, BorderLayout.NORTH);
        inferior.add(botonQuitar, BorderLayout.SOUTH);

        add(titulos, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
    }

    /** Niveles del menu para un producto: la categoria y despues cada palabra del nombre. */
    private static String[] niveles(Producto producto) {
        String[] palabras = producto.getNombre().split(" ");
        String[] niveles = new String[palabras.length + 1];
        niveles[0] = producto.getCategoria();
        System.arraycopy(palabras, 0, niveles, 1, palabras.length);
        return niveles;
    }

    /** true si el producto tiene todas las opciones marcadas (o si no hay nada marcado). */
    public boolean coincide(Producto producto) {
        String[] niveles = niveles(producto);
        if (niveles.length < marcadas.length) {
            return false;
        }
        for (int i = 0; i < marcadas.length; i++) {
            if (!ListaProductos.normalizar(niveles[i]).equals(marcadas[i])) {
                return false;
            }
        }
        return true;
    }

    /** true si el usuario marco al menos una opcion. */
    public boolean estaActivo() {
        return marcadas.length > 0;
    }

    /** Desmarca todas las opciones. */
    public void limpiar() {
        marcadas = new String[0];
    }

    /** Vuelve a armar las casillas segun lo marcado y lo que hay en la lista. */
    public void actualizar() {
        // Si se elimino o renombro el producto, se quitan niveles hasta que vuelva a coincidir alguno.
        while (estaActivo() && contarCoincidencias() == 0) {
            marcadas = Arrays.copyOf(marcadas, marcadas.length - 1);
        }

        // Se recorre la lista: de cada producto que coincide se toma la opcion del siguiente nivel.
        // El TreeMap guarda "opcion -> cuantos productos la tienen", ordenado con compararPalabras
        // y sin importar tildes ni mayusculas ("Café" y "cafe" son la misma opcion).
        // Solo sirve para armar el menu: los productos siguen guardados en la lista enlazada.
        TreeMap<String, Integer> siguientes = new TreeMap<>(PanelBusquedaGuiada::compararPalabras);
        String[] nivelesEscritos = null; // las opciones marcadas tal como estan escritas (con tildes)
        Nodo actual = lista.getCabeza();
        while (actual != null) {
            if (coincide(actual.getProducto())) {
                String[] niveles = niveles(actual.getProducto());
                if (nivelesEscritos == null) {
                    nivelesEscritos = niveles;
                }
                if (niveles.length > marcadas.length) {
                    String siguiente = niveles[marcadas.length];
                    siguientes.put(siguiente, siguientes.getOrDefault(siguiente, 0) + 1);
                }
            }
            actual = actual.getSiguiente();
        }

        panelCasillas.removeAll();
        // Opciones ya marcadas: desmarcar una vuelve a ese nivel del menu.
        for (int i = 0; i < marcadas.length; i++) {
            int nivel = i;
            JCheckBox casilla = crearCasilla(nivelesEscritos[i], nivel, true);
            casilla.addActionListener(e -> {
                marcadas = Arrays.copyOf(marcadas, nivel);
                alCambiar.run();
            });
            panelCasillas.add(casilla);
        }
        // Opciones del siguiente nivel.
        if (!siguientes.isEmpty()) {
            String titulo = TITULOS_NIVEL[Math.min(marcadas.length, TITULOS_NIVEL.length - 1)];
            panelCasillas.add(crearSubtitulo(titulo, marcadas.length, Estilo.TEXTO_SECUNDARIO));
            for (Map.Entry<String, Integer> opcion : siguientes.entrySet()) {
                String texto = opcion.getKey();
                JCheckBox casilla = crearCasilla(texto + "  (" + opcion.getValue() + ")", marcadas.length, false);
                casilla.addActionListener(e -> {
                    marcadas = Arrays.copyOf(marcadas, marcadas.length + 1);
                    marcadas[marcadas.length - 1] = ListaProductos.normalizar(texto);
                    alCambiar.run();
                });
                panelCasillas.add(casilla);
            }
        } else if (estaActivo()) {
            panelCasillas.add(crearSubtitulo("Producto encontrado", marcadas.length, Estilo.EXITO));
        }

        etiquetaResultado.setText(estaActivo()
                ? "Mostrando " + contarCoincidencias() + " de " + lista.getTamanio() + " productos"
                : lista.getTamanio() + " productos en la lista");
        botonQuitar.setEnabled(estaActivo());
        panelCasillas.revalidate();
        panelCasillas.repaint();
    }

    /** Orden alfabetico sin tildes; si las dos palabras son numeros, orden numerico (2 antes que 10). */
    private static int compararPalabras(String a, String b) {
        String x = ListaProductos.normalizar(a);
        String y = ListaProductos.normalizar(b);
        if (x.matches("\\d+") && y.matches("\\d+") && x.length() != y.length()) {
            return x.length() - y.length(); // el numero con menos cifras es menor
        }
        return x.compareTo(y);
    }

    /** Recorre la lista y cuenta los productos que coinciden con lo marcado. */
    private int contarCoincidencias() {
        int contador = 0;
        Nodo actual = lista.getCabeza();
        while (actual != null) {
            if (coincide(actual.getProducto())) {
                contador++;
            }
            actual = actual.getSiguiente();
        }
        return contador;
    }

    /** Casilla con sangria segun el nivel, para que se vea como un arbol. */
    private JCheckBox crearCasilla(String texto, int nivel, boolean marcada) {
        JCheckBox casilla = new JCheckBox(texto, marcada);
        casilla.setFont(Estilo.fuente(marcada ? Font.BOLD : Font.PLAIN, 14));
        casilla.setForeground(Estilo.TEXTO);
        casilla.setOpaque(false);
        casilla.setFocusPainted(false);
        casilla.setToolTipText(texto);
        casilla.setBorder(BorderFactory.createEmptyBorder(3, 14 * nivel, 3, 0));
        casilla.setAlignmentX(Component.LEFT_ALIGNMENT);
        return casilla;
    }

    private JLabel crearSubtitulo(String texto, int nivel, Color color) {
        JLabel subtitulo = Estilo.crearEtiqueta(texto, Font.BOLD, 12, color);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(8, 14 * nivel + 2, 2, 0));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return subtitulo;
    }
}
