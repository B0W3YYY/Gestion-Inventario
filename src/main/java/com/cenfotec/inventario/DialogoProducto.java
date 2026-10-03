package com.cenfotec.inventario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Formulario para agregar o editar un producto. La categoria y el tipo se eligen
 * del catalogo de ReglasProducto; el resto se valida con esas mismas reglas (las
 * del menu de consola) y el error se muestra debajo del campo.
 * Este formulario no cambia la lista: la VentanaPrincipal lee los datos ya
 * validados y llama a ListaProductos.
 */
public class DialogoProducto extends JDialog {

    private static final String AYUDA_DETALLE = "Opcional. Va después del tipo, ej. Leche + «entera 1 L».";
    private static final String AYUDA_PRECIO = "En colones. Use punto para los decimales.";
    private static final String AYUDA_FECHA = "Opcional. Déjela vacía si el producto no vence.";

    private final ListaProductos lista;
    private final Producto productoEditado; // null cuando se agrega un producto nuevo

    private final JComboBox<String> comboCategoria = new JComboBox<>(ReglasProducto.CATEGORIAS);
    private final JComboBox<String> comboTipo = new JComboBox<>(ReglasProducto.TIPOS[0]);
    private final JTextField campoDetalle = Estilo.crearCampo("Ej. entera 1 L");
    private final JTextField campoPrecio = Estilo.crearCampo("Ej. 1500.50");
    private final JTextField campoCantidad = Estilo.crearCampo("Ej. 10");
    private final JTextField campoFecha = Estilo.crearCampo("dd/mm/aaaa");
    private final JLabel errorDetalle = new JLabel();
    private final JLabel errorPrecio = new JLabel();
    private final JLabel errorCantidad = new JLabel();
    private final JLabel errorFecha = new JLabel();
    private final JRadioButton opcionFinal = new JRadioButton("Al final de la lista", true);
    private final JRadioButton opcionInicio = new JRadioButton("Al inicio de la lista");

    // Datos ya validados; la ventana principal los lee despues de cerrar el formulario.
    private boolean guardado = false;
    private String nombre;
    private String categoria;
    private double precio;
    private int cantidad;
    private LocalDate fecha;

    /** Si productoEditado es null el formulario sirve para agregar; si no, para editarlo. */
    public DialogoProducto(JFrame padre, ListaProductos lista, Producto productoEditado) {
        super(padre, productoEditado == null ? "Agregar producto" : "Editar producto", true);
        this.lista = lista;
        this.productoEditado = productoEditado;
        boolean agregando = productoEditado == null;

        // Al cambiar la categoria, el segundo menu muestra solo los tipos de esa categoria.
        comboCategoria.addActionListener(e ->
                comboTipo.setModel(new DefaultComboBoxModel<>(ReglasProducto.TIPOS[comboCategoria.getSelectedIndex()])));

        JPanel formulario = new JPanel();
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        formulario.setBackground(Estilo.PANEL);
        formulario.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));
        formulario.add(Estilo.crearEtiqueta(agregando ? "Nuevo producto" : "Editar producto",
                Font.BOLD, 20, Estilo.TEXTO));
        formulario.add(Estilo.crearEtiqueta(agregando ? "Información del producto"
                : "Cambie solo los datos que necesite.", Font.PLAIN, 13, Estilo.TEXTO_SECUNDARIO));
        formulario.add(Box.createVerticalStrut(18));
        formulario.add(crearFila(crearGrupo("Categoría", comboCategoria, null),
                crearGrupo("Tipo de producto", comboTipo, null)));
        formulario.add(crearGrupo("Detalle del nombre", campoDetalle, errorDetalle));
        formulario.add(crearFila(crearGrupo("Precio unitario (₡)", campoPrecio, errorPrecio),
                crearGrupo("Cantidad", campoCantidad, errorCantidad)));
        formulario.add(crearGrupo("Fecha de vencimiento", campoFecha, errorFecha));
        if (agregando) {
            formulario.add(crearOpcionesPosicion());
        } else {
            llenarCampos(productoEditado);
        }

        JButton botonCancelar = Estilo.crearBoton("Cancelar", Estilo.PANEL, Estilo.TEXTO);
        JButton botonGuardar = Estilo.crearBoton(agregando ? "Agregar producto" : "Guardar cambios",
                Estilo.PRIMARIO, Color.WHITE);
        botonCancelar.addActionListener(e -> dispose());
        botonGuardar.addActionListener(e -> guardar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBackground(Estilo.PANEL);
        botones.setBorder(BorderFactory.createEmptyBorder(8, 20, 20, 20));
        botones.add(botonCancelar);
        botones.add(botonGuardar);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(Estilo.PANEL);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);

        getRootPane().setDefaultButton(botonGuardar); // Enter = guardar
        getRootPane().registerKeyboardAction(e -> dispose(), // Esc = cancelar
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        restablecerErrores();
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    /** Etiqueta, el campo (o menu) y debajo el texto de ayuda/error si lo tiene. */
    private JPanel crearGrupo(String titulo, JComponent campo, JLabel ayuda) {
        // Todos los campos y menus con la misma altura que los campos de texto.
        int alto = campoPrecio.getPreferredSize().height;
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, alto));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel grupo = new JPanel();
        grupo.setLayout(new BoxLayout(grupo, BoxLayout.Y_AXIS));
        grupo.setOpaque(false);
        grupo.setAlignmentX(Component.LEFT_ALIGNMENT);
        grupo.add(Estilo.crearEtiqueta(titulo, Font.BOLD, 13, Estilo.TEXTO));
        grupo.add(Box.createVerticalStrut(6));
        grupo.add(campo);
        if (ayuda != null) {
            ayuda.setFont(Estilo.fuente(Font.PLAIN, 12));
            grupo.add(Box.createVerticalStrut(4));
            grupo.add(ayuda);
        }
        grupo.add(Box.createVerticalStrut(ayuda != null ? 6 : 14));
        return grupo;
    }

    /** Dos grupos lado a lado con el mismo ancho. */
    private JPanel crearFila(JPanel izquierda, JPanel derecha) {
        JPanel fila = new JPanel(new GridLayout(1, 2, 16, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.add(izquierda);
        fila.add(derecha);
        return fila;
    }

    /** Opciones para elegir si el producto se inserta al inicio o al final de la lista. */
    private JPanel crearOpcionesPosicion() {
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(opcionFinal);
        grupo.add(opcionInicio);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Estilo.crearEtiqueta("Posición en la lista", Font.BOLD, 13, Estilo.TEXTO));
        panel.add(Box.createVerticalStrut(4));
        agregarOpcion(panel, opcionFinal, "Se agrega después del último producto.");
        agregarOpcion(panel, opcionInicio, "Queda como el primer producto (nueva cabeza de la lista).");
        return panel;
    }

    private void agregarOpcion(JPanel panel, JRadioButton opcion, String explicacion) {
        opcion.setOpaque(false);
        opcion.setFocusPainted(false);
        opcion.setForeground(Estilo.TEXTO);
        JLabel detalle = Estilo.crearEtiqueta(explicacion, Font.PLAIN, 12, Estilo.TEXTO_SECUNDARIO);
        detalle.setBorder(BorderFactory.createEmptyBorder(0, 26, 6, 0)); // alineado con el texto de la opcion
        panel.add(opcion);
        panel.add(detalle);
    }

    /** Al editar, el formulario empieza con los datos actuales del producto. */
    private void llenarCampos(Producto producto) {
        // El nombre empieza con el tipo: "Leche entera 1 L" = tipo "Leche" + detalle "entera 1 L".
        String[] partes = producto.getNombre().split(" ", 2);
        comboCategoria.setSelectedItem(producto.getCategoria()); // esto carga los tipos de la categoria
        comboTipo.setSelectedItem(partes[0]);
        campoDetalle.setText(partes.length > 1 ? partes[1] : "");
        campoPrecio.setText(String.format(Locale.US, "%.2f", producto.getPrecio()));
        campoCantidad.setText(String.valueOf(producto.getCantidad()));
        LocalDate vencimiento = producto.getFechaVencimiento();
        campoFecha.setText(vencimiento == null ? "" : vencimiento.format(ReglasProducto.FORMATO_FECHA));
    }

    /** Valida todos los campos; si estan bien cierra el formulario, si no muestra los errores. */
    private void guardar() {
        restablecerErrores();
        categoria = (String) comboCategoria.getSelectedItem(); // sale del catalogo, siempre es valida
        JTextField[] campos = {campoDetalle, campoPrecio, campoCantidad, campoFecha};
        JLabel[] etiquetas = {errorDetalle, errorPrecio, errorCantidad, errorFecha};
        String[] errores = {validarNombre(), validarPrecio(), validarCantidad(), validarFecha()};

        boolean valido = true;
        for (int i = 0; i < campos.length; i++) {
            if (errores[i] != null) {
                if (valido) {
                    campos[i].requestFocusInWindow(); // el cursor va al primer campo con error
                }
                valido = false;
                Estilo.marcarCampo(campos[i], true);
                etiquetas[i].setText(errores[i]);
                etiquetas[i].setForeground(Estilo.PELIGRO);
            }
        }
        if (valido) {
            guardado = true;
            dispose();
        }
    }

    /** Quita las marcas de error y deja los textos de ayuda. */
    private void restablecerErrores() {
        for (JTextField campo : new JTextField[]{campoDetalle, campoPrecio, campoCantidad, campoFecha}) {
            Estilo.marcarCampo(campo, false);
        }
        for (JLabel etiqueta : new JLabel[]{errorDetalle, errorPrecio, errorCantidad, errorFecha}) {
            etiqueta.setText(" "); // un espacio conserva la altura y el formulario no "salta"
            etiqueta.setForeground(Estilo.TEXTO_SECUNDARIO);
        }
        errorDetalle.setText(AYUDA_DETALLE);
        errorPrecio.setText(AYUDA_PRECIO);
        errorFecha.setText(AYUDA_FECHA);
    }

    // Cada validacion usa la regla de ReglasProducto (la misma del menu de consola):
    // si el dato es correcto lo guarda y retorna null; si no, retorna el mensaje de error.

    /** El nombre se arma con el tipo elegido y el detalle escrito (ej. "Leche" + "entera 1 L"). */
    private String validarNombre() {
        try {
            nombre = ReglasProducto.armarNombre((String) comboTipo.getSelectedItem(), campoDetalle.getText(),
                    lista, productoEditado);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String validarPrecio() {
        try {
            precio = ReglasProducto.convertirPrecio(campoPrecio.getText());
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String validarCantidad() {
        try {
            cantidad = ReglasProducto.convertirCantidad(campoCantidad.getText());
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String validarFecha() {
        try {
            fecha = ReglasProducto.convertirFecha(campoFecha.getText());
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public boolean isGuardado() {
        return guardado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    /** Fecha de vencimiento, o null si el producto no vence. */
    public LocalDate getFecha() {
        return fecha;
    }

    /** true si el usuario eligio insertar al inicio de la lista. */
    public boolean isAlInicio() {
        return opcionInicio.isSelected();
    }
}
