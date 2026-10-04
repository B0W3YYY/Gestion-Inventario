package com.cenfotec.inventario;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Muestra las imagenes de un producto como miniaturas y permite agregar nuevas.
 * Igual que antes, cada imagen se guarda en el producto como una ruta de archivo.
 */
public class DialogoImagenes extends JDialog {

    private static final int ANCHO_MINIATURA = 180;
    private static final int ALTO_MINIATURA = 130;

    private final ListaProductos lista;
    private final Producto producto;
    private final JPanel panelMiniaturas = new JPanel(new GridLayout(0, 3, 12, 12));
    private final JLabel etiquetaSinImagenes = Estilo.crearEtiqueta(
            "Este producto todavía no tiene imágenes.", Font.PLAIN, 14, Estilo.TEXTO_SECUNDARIO);
    private final JLabel etiquetaMensaje = new JLabel(" ");

    public DialogoImagenes(JFrame padre, ListaProductos lista, Producto producto) {
        super(padre, "Imágenes del producto", true);
        this.lista = lista;
        this.producto = producto;

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);
        titulos.add(Estilo.crearEtiqueta("Imágenes de «" + producto.getNombre() + "»", Font.BOLD, 20, Estilo.TEXTO));
        titulos.add(Estilo.crearEtiqueta("Las imágenes deben estar en la carpeta «" + ReglasProducto.CARPETA_IMAGENES
                + "» del proyecto; se guarda la ruta del archivo.",
                Font.PLAIN, 13, Estilo.TEXTO_SECUNDARIO));

        // La cuadricula va arriba de un panel contenedor para que no se estire hacia abajo.
        panelMiniaturas.setOpaque(false);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Estilo.FONDO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenedor.add(panelMiniaturas, BorderLayout.NORTH);
        etiquetaSinImagenes.setHorizontalAlignment(SwingConstants.CENTER);
        contenedor.add(etiquetaSinImagenes, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton botonCerrar = Estilo.crearBoton("Cerrar", Estilo.PANEL, Estilo.TEXTO);
        JButton botonAgregar = Estilo.crearBoton("+ Agregar imagen", Estilo.PRIMARIO, Color.WHITE);
        botonCerrar.addActionListener(e -> dispose());
        botonAgregar.addActionListener(e -> agregarImagen());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(botonCerrar);
        botones.add(botonAgregar);
        etiquetaMensaje.setFont(Estilo.fuente(Font.PLAIN, 13));
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setOpaque(false);
        inferior.add(etiquetaMensaje, BorderLayout.CENTER);
        inferior.add(botones, BorderLayout.EAST);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBackground(Estilo.PANEL);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        contenido.add(titulos, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(inferior, BorderLayout.SOUTH);
        setContentPane(contenido);

        mostrarMiniaturas();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 560);
        setMinimumSize(new Dimension(800, 420)); // siempre caben 3 miniaturas por fila
        setLocationRelativeTo(padre);
    }

    /** Dibuja una miniatura por cada ruta guardada en el producto. */
    private void mostrarMiniaturas() {
        panelMiniaturas.removeAll();
        etiquetaSinImagenes.setVisible(producto.getListaImagenes().isEmpty());
        for (String ruta : producto.getListaImagenes()) {
            panelMiniaturas.add(crearMiniatura(ruta));
        }
        panelMiniaturas.revalidate();
        panelMiniaturas.repaint();
    }

    private JPanel crearMiniatura(String ruta) {
        JLabel imagen = new JLabel("", SwingConstants.CENTER);
        imagen.setPreferredSize(new Dimension(ANCHO_MINIATURA, ALTO_MINIATURA));
        BufferedImage original = ReglasProducto.leerImagen(ReglasProducto.archivoDeRuta(ruta));
        if (original != null) {
            // Se reduce la imagen manteniendo su proporcion para que quepa en la miniatura.
            double escala = Math.min(1.0, Math.min((double) ANCHO_MINIATURA / original.getWidth(),
                    (double) ALTO_MINIATURA / original.getHeight()));
            int ancho = Math.max(1, (int) (original.getWidth() * escala));
            int alto = Math.max(1, (int) (original.getHeight() * escala));
            imagen.setIcon(crearIcono(original, ancho, alto));
        } else {
            imagen.setText("Imagen no disponible"); // el archivo se movio o se borro
            imagen.setForeground(Estilo.TEXTO_SECUNDARIO);
        }

        JLabel nombreArchivo = Estilo.crearEtiqueta(new File(ruta).getName(), Font.PLAIN, 12, Estilo.TEXTO_SECUNDARIO);
        nombreArchivo.setToolTipText(ruta);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 6));
        tarjeta.setBackground(Estilo.PANEL);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        tarjeta.add(imagen, BorderLayout.CENTER);
        tarjeta.add(nombreArchivo, BorderLayout.SOUTH);
        return tarjeta;
    }

    /**
     * Crea el icono de la miniatura de ancho x alto. Windows suele ampliar todo (ej. escala
     * 125%): una imagen de 180 px se dibujaria estirada en 225 px reales y se veria borrosa.
     * Por eso tambien se prepara una version al tamaño real en pantalla, y Swing usa esa.
     */
    private Icon crearIcono(BufferedImage original, int ancho, int alto) {
        BufferedImage normal = escalar(original, ancho, alto);
        GraphicsConfiguration pantalla = getGraphicsConfiguration();
        double escalaPantalla = (pantalla == null) ? 1.0 : pantalla.getDefaultTransform().getScaleX();
        if (escalaPantalla <= 1.0) {
            return new ImageIcon(normal);
        }
        BufferedImage nitida = escalar(original, (int) Math.round(ancho * escalaPantalla),
                (int) Math.round(alto * escalaPantalla));
        return new ImageIcon(new BaseMultiResolutionImage(normal, nitida));
    }

    /** Cambia el tamaño de la imagen reduciendola en pasos de a la mitad, asi no pierde detalle. */
    private static BufferedImage escalar(BufferedImage original, int ancho, int alto) {
        BufferedImage actual = original;
        int w = original.getWidth();
        int h = original.getHeight();
        do {
            w = Math.max(ancho, w / 2);
            h = Math.max(alto, h / 2);
            BufferedImage paso = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = paso.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(actual, 0, 0, w, h, null);
            g.dispose();
            actual = paso;
        } while (w != ancho || h != alto);
        return actual;
    }

    /**
     * Deja elegir un archivo de la carpeta "imagenes" del proyecto, lo valida con
     * ReglasProducto (las mismas reglas del menu de consola) y lo agrega al producto.
     */
    private void agregarImagen() {
        JFileChooser selector = new JFileChooser(ReglasProducto.carpetaImagenes());
        selector.setDialogTitle("Seleccionar imagen de la carpeta «" + ReglasProducto.CARPETA_IMAGENES + "»");
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes (jpg, png)", ReglasProducto.EXTENSIONES_IMAGEN));
        selector.setAcceptAllFileFilterUsed(false);
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // el usuario cancelo
        }
        try {
            String ruta = ReglasProducto.convertirRutaImagen(selector.getSelectedFile(), producto);
            lista.agregarImagenAProducto(producto.getNombre(), ruta);
            mostrarMiniaturas();
            mostrarMensaje("Imagen agregada correctamente.", true);
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), false);
        }
    }

    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaMensaje.setText(texto);
        etiquetaMensaje.setForeground(exito ? Estilo.EXITO : Estilo.PELIGRO);
    }
}
