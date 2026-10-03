package com.cenfotec.inventario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Ventana principal de la aplicacion. Muestra los productos de la
 * ListaProductos en una tabla y tiene un boton por cada operacion.
 * Cada operacion llama al metodo correspondiente de la lista enlazada y
 * despues la tabla se vuelve a llenar recorriendo la lista desde la cabeza.
 */
public class VentanaPrincipal extends JFrame {

    private static final String[] COLUMNAS =
            {"#", "Nombre", "Categoría", "Precio", "Cantidad", "Vencimiento", "Costo total", "Imágenes"};
    private static final int COLUMNA_NOMBRE = 1;

    private final ListaProductos lista;

    private final DefaultTableModel modeloTabla;
    private final JTable tabla;
    private final PanelBusquedaGuiada panelBusqueda;
    private final JPanel panelCentral = new JPanel(new CardLayout()); // tabla o mensaje de lista vacia
    private final JTextField campoBuscar = Estilo.crearCampo("Buscar producto por nombre…");
    private final JButton botonEditar = Estilo.crearBoton("Editar", Estilo.PANEL, Estilo.TEXTO);
    private final JButton botonImagenes = Estilo.crearBoton("Imágenes", Estilo.PANEL, Estilo.TEXTO);
    private final JButton botonEliminar = Estilo.crearBoton("Eliminar", Estilo.PANEL, Estilo.PELIGRO);
    private final JLabel etiquetaMensaje = new JLabel();
    private final Timer temporizadorMensaje = new Timer(5000, e -> mostrarAyuda());

    // Indicadores de la parte superior
    private final JLabel valorProductos = new JLabel();
    private final JLabel valorUnidades = new JLabel();
    private final JLabel valorInventario = new JLabel();
    private final JLabel valorPorVencer = new JLabel();

    public VentanaPrincipal(ListaProductos lista) {
        super("Gestión de Inventarios");
        this.lista = lista;

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false; // los datos se cambian con el formulario, no escribiendo en la tabla
            }
        };
        tabla = new JTable(modeloTabla);
        Estilo.configurarTabla(tabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.rowAtPoint(e.getPoint()) != -1) {
                    editarProducto(); // doble clic en una fila = editar
                }
            }
        });
        int[] anchos = {44, 175, 115, 125, 85, 175, 160, 85};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        panelBusqueda = new PanelBusquedaGuiada(lista, this::filtroCambiado);

        campoBuscar.addActionListener(e -> buscarProducto()); // Enter = buscar
        botonEditar.addActionListener(e -> editarProducto());
        botonImagenes.addActionListener(e -> verImagenes());
        botonEliminar.addActionListener(e -> eliminarProducto());
        botonEditar.setToolTipText("Editar el producto seleccionado (también con doble clic)");
        botonImagenes.setToolTipText("Ver y agregar imágenes del producto seleccionado");
        botonEliminar.setToolTipText("Eliminar el producto seleccionado");
        temporizadorMensaje.setRepeats(false);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        cuerpo.add(crearIndicadores(), BorderLayout.NORTH);
        cuerpo.add(crearPanelProductos(), BorderLayout.CENTER);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(cuerpo, BorderLayout.CENTER);
        setContentPane(raiz);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // al cerrarla se vuelve al menu de consola
        setMinimumSize(new Dimension(1240, 640));
        setSize(1320, 780);
        setLocationRelativeTo(null);

        actualizarVista();
        mostrarAyuda();
    }

    // ------------------------------------------------------------------
    // Construccion de la pantalla
    // ------------------------------------------------------------------

    /** Barra superior: nombre de la aplicacion, descripcion y boton del reporte. */
    private JPanel crearEncabezado() {
        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);
        titulos.add(Estilo.crearEtiqueta("Gestión de Inventarios", Font.BOLD, 22, Estilo.TEXTO));
        titulos.add(Estilo.crearEtiqueta("Administración de productos con una lista enlazada simple",
                Font.PLAIN, 13, Estilo.TEXTO_SECUNDARIO));

        JButton botonReporte = Estilo.crearBoton("Reporte de costos", Estilo.PANEL, Estilo.TEXTO);
        botonReporte.addActionListener(e -> new DialogoReporte(this, lista).setVisible(true));
        JPanel derecha = new JPanel(new GridBagLayout()); // centra el boton verticalmente
        derecha.setOpaque(false);
        derecha.add(botonReporte);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.PANEL);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        encabezado.add(titulos, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    /** Fila de tarjetas con los indicadores del inventario. */
    private JPanel crearIndicadores() {
        JPanel indicadores = new JPanel(new GridLayout(1, 4, 16, 0));
        indicadores.setOpaque(false);
        indicadores.add(Estilo.crearTarjeta("Productos", valorProductos, "Nodos en la lista"));
        indicadores.add(Estilo.crearTarjeta("Unidades", valorUnidades, "Suma de las cantidades"));
        indicadores.add(Estilo.crearTarjeta("Valor del inventario", valorInventario, "Suma de precio × cantidad"));
        indicadores.add(Estilo.crearTarjeta("Por vencer", valorPorVencer,
                "Vencidos o vencen en " + Estilo.DIAS_AVISO_VENCIMIENTO + " días"));
        return indicadores;
    }

    /** Panel blanco con la barra de busqueda/acciones, el mensaje y la tabla. */
    private JPanel crearPanelProductos() {
        JButton botonBuscar = Estilo.crearBoton("Buscar", Estilo.PANEL, Estilo.TEXTO);
        botonBuscar.addActionListener(e -> buscarProducto());
        JButton botonAgregar = Estilo.crearBoton("+ Agregar producto", Estilo.PRIMARIO, Color.WHITE);
        botonAgregar.addActionListener(e -> agregarProducto());

        campoBuscar.setPreferredSize(new Dimension(280, botonBuscar.getPreferredSize().height));
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        busqueda.setOpaque(false);
        busqueda.add(campoBuscar);
        busqueda.add(botonBuscar);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(botonImagenes);
        acciones.add(botonEditar);
        acciones.add(botonEliminar);
        acciones.add(botonAgregar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(busqueda, BorderLayout.WEST);
        barra.add(acciones, BorderLayout.EAST);

        etiquetaMensaje.setOpaque(true);
        etiquetaMensaje.setFont(Estilo.fuente(Font.PLAIN, 13));
        etiquetaMensaje.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        JPanel zonaMensaje = new JPanel(new BorderLayout());
        zonaMensaje.setOpaque(false);
        zonaMensaje.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        zonaMensaje.add(etiquetaMensaje);

        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        superior.setBorder(BorderFactory.createEmptyBorder(16, 8, 0, 8));
        superior.add(barra, BorderLayout.NORTH);
        superior.add(zonaMensaje, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE));
        scroll.getViewport().setBackground(Estilo.PANEL);
        panelCentral.add(scroll, "tabla");
        panelCentral.add(crearEstadoVacio(), "vacio");

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Estilo.PANEL);
        panel.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        panel.add(superior, BorderLayout.NORTH);
        panel.add(panelBusqueda, BorderLayout.WEST); // menu de busqueda guiada a la izquierda de la tabla
        panel.add(panelCentral, BorderLayout.CENTER);
        return panel;
    }

    /** Lo que se muestra en lugar de la tabla cuando no hay productos. */
    private JPanel crearEstadoVacio() {
        JLabel titulo = Estilo.crearEtiqueta("No hay productos registrados", Font.BOLD, 18, Estilo.TEXTO);
        JLabel texto = Estilo.crearEtiqueta("Agregue el primer producto para empezar a llenar la lista enlazada.",
                Font.PLAIN, 14, Estilo.TEXTO_SECUNDARIO);
        JButton botonAgregar = Estilo.crearBoton("+ Agregar producto", Estilo.PRIMARIO, Color.WHITE);
        botonAgregar.addActionListener(e -> agregarProducto());

        JPanel caja = new JPanel();
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.setOpaque(false);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        texto.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonAgregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        caja.add(titulo);
        caja.add(Box.createVerticalStrut(6));
        caja.add(texto);
        caja.add(Box.createVerticalStrut(18));
        caja.add(botonAgregar);

        JPanel vacio = new JPanel(new GridBagLayout()); // centra la caja
        vacio.setBackground(Estilo.PANEL);
        vacio.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE));
        vacio.add(caja);
        return vacio;
    }

    // ------------------------------------------------------------------
    // Operaciones (una por cada opcion del antiguo menu de consola)
    // ------------------------------------------------------------------

    /** Abre el formulario y agrega el producto al inicio o al final de la lista. */
    private void agregarProducto() {
        DialogoProducto dialogo = new DialogoProducto(this, lista, null);
        dialogo.setVisible(true); // espera hasta que el formulario se cierre
        if (!dialogo.isGuardado()) {
            return; // el usuario cancelo
        }
        Producto nuevo = new Producto(dialogo.getNombre(), dialogo.getPrecio(), dialogo.getCategoria(),
                dialogo.getFecha(), dialogo.getCantidad());
        if (dialogo.isAlInicio()) {
            lista.insertarAlInicio(nuevo);
        } else {
            lista.insertarAlFinal(nuevo);
        }
        actualizarVista();
        seleccionarFila(nuevo.getNombre());
        mostrarMensaje("Producto agregado " + (dialogo.isAlInicio() ? "al inicio" : "al final")
                + " de la lista correctamente.", true);
    }

    /** Abre el formulario con los datos del producto seleccionado para cambiarlos. */
    private void editarProducto() {
        Producto producto = productoSeleccionado();
        String nombreOriginal = producto.getNombre();
        DialogoProducto dialogo = new DialogoProducto(this, lista, producto);
        dialogo.setVisible(true);
        if (!dialogo.isGuardado()) {
            return;
        }
        lista.modificarProducto(nombreOriginal, dialogo.getNombre(), dialogo.getPrecio(), dialogo.getCategoria(),
                dialogo.getFecha(), dialogo.getCantidad());
        actualizarVista();
        seleccionarFila(dialogo.getNombre());
        mostrarMensaje("Producto actualizado correctamente.", true);
    }

    /** Pide confirmacion y elimina el producto seleccionado de la lista. */
    private void eliminarProducto() {
        Producto producto = productoSeleccionado();
        String[] opciones = {"Eliminar", "Cancelar"};
        int respuesta = JOptionPane.showOptionDialog(this,
                "¿Está seguro de que desea eliminar «" + producto.getNombre() + "»?\nEsta acción no se puede deshacer.",
                "Eliminar producto", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, opciones, opciones[1]);
        if (respuesta != 0) {
            return; // eligio Cancelar o cerro la ventana
        }
        lista.eliminarProducto(producto.getNombre());
        actualizarVista();
        mostrarMensaje("Producto «" + producto.getNombre() + "» eliminado correctamente.", true);
    }

    /**
     * Busca el nombre escrito con buscarPorNombre de la lista y selecciona su fila.
     * No importan mayusculas, tildes ni espacios de mas ("cafe   molido" = "Café molido").
     */
    private void buscarProducto() {
        String nombre = campoBuscar.getText().trim();
        if (nombre.isEmpty()) {
            mostrarMensaje("Escriba el nombre del producto que desea buscar.", false);
            return;
        }
        Producto encontrado = lista.buscarPorNombre(nombre);
        if (encontrado == null) {
            tabla.clearSelection();
            mostrarMensaje("No se encontró ningún producto llamado «" + nombre
                    + "». Escriba el nombre completo o use la búsqueda guiada de la izquierda.", false);
            return;
        }
        seleccionarFila(encontrado.getNombre());
        mostrarMensaje("Producto encontrado: «" + encontrado.getNombre() + "», posición "
                + modeloTabla.getValueAt(tabla.getSelectedRow(), 0) + " de la lista.", true);
    }

    /** Se llama cuando el usuario marca o desmarca una opcion de la busqueda guiada. */
    private void filtroCambiado() {
        actualizarVista();
        // Si lo marcado lleva a un solo producto, se selecciona para poder editarlo o eliminarlo.
        if (panelBusqueda.estaActivo() && modeloTabla.getRowCount() == 1) {
            tabla.setRowSelectionInterval(0, 0);
            mostrarMensaje("Producto encontrado: «" + modeloTabla.getValueAt(0, COLUMNA_NOMBRE) + "».", true);
        }
    }

    /** Abre la ventana de imagenes del producto seleccionado. */
    private void verImagenes() {
        Producto producto = productoSeleccionado();
        new DialogoImagenes(this, lista, producto).setVisible(true);
        actualizarVista(); // puede haber cambiado la cantidad de imagenes
        seleccionarFila(producto.getNombre());
    }

    // ------------------------------------------------------------------
    // Actualizacion de la pantalla
    // ------------------------------------------------------------------

    /**
     * Vuelve a llenar la tabla y los indicadores recorriendo la lista enlazada.
     * Si hay opciones marcadas en la busqueda guiada, solo se muestran esos productos,
     * pero la columna # sigue indicando la posicion real del producto en la lista.
     */
    private void actualizarVista() {
        panelBusqueda.actualizar();
        modeloTabla.setRowCount(0);
        int posicion = 1;
        Nodo actual = lista.getCabeza();
        while (actual != null) {
            Producto p = actual.getProducto();
            if (panelBusqueda.coincide(p)) {
                modeloTabla.addRow(new Object[]{
                        posicion, p.getNombre(), p.getCategoria(), p.getPrecio(), p.getCantidad(),
                        p.getFechaVencimiento(), p.calcularCostoTotal(), p.getListaImagenes().size()});
            }
            actual = actual.getSiguiente();
            posicion++;
        }
        panelBusqueda.setVisible(!lista.estaVacia());

        int porVencer = lista.contarPorVencer(LocalDate.now().plusDays(Estilo.DIAS_AVISO_VENCIMIENTO));
        valorProductos.setText(String.valueOf(lista.getTamanio()));
        valorUnidades.setText(String.format(Locale.US, "%,d", lista.contarUnidades()));
        valorInventario.setText(Estilo.moneda(lista.calcularCostoTotal()));
        valorInventario.setToolTipText(valorInventario.getText()); // por si un valor muy grande no cabe
        valorPorVencer.setText(String.valueOf(porVencer));
        valorPorVencer.setForeground(porVencer > 0 ? Estilo.ADVERTENCIA : Estilo.TEXTO);

        ((CardLayout) panelCentral.getLayout()).show(panelCentral, lista.estaVacia() ? "vacio" : "tabla");
        actualizarBotones();
    }

    /** Editar, Imagenes y Eliminar solo se activan si hay una fila seleccionada. */
    private void actualizarBotones() {
        boolean haySeleccion = tabla.getSelectedRow() != -1;
        botonEditar.setEnabled(haySeleccion);
        botonImagenes.setEnabled(haySeleccion);
        botonEliminar.setEnabled(haySeleccion);
    }

    /**
     * Retorna el producto de la fila seleccionada, buscandolo por nombre en la lista enlazada.
     * Solo se llama con una fila seleccionada: sin seleccion los botones estan desactivados.
     */
    private Producto productoSeleccionado() {
        return lista.buscarPorNombre((String) modeloTabla.getValueAt(tabla.getSelectedRow(), COLUMNA_NOMBRE));
    }

    /** Selecciona la fila del producto con ese nombre y la hace visible. */
    private void seleccionarFila(String nombre) {
        Producto producto = lista.buscarPorNombre(nombre);
        if (!panelBusqueda.coincide(producto)) {
            // La busqueda guiada oculta ese producto: se quita el filtro para poder mostrarlo.
            panelBusqueda.limpiar();
            actualizarVista();
        }
        for (int fila = 0; fila < modeloTabla.getRowCount(); fila++) {
            if (producto.getNombre().equals(modeloTabla.getValueAt(fila, COLUMNA_NOMBRE))) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    /** Muestra un mensaje verde (exito) o rojo (error) durante unos segundos. */
    private void mostrarMensaje(String texto, boolean exito) {
        etiquetaMensaje.setText(texto);
        etiquetaMensaje.setForeground(exito ? Estilo.EXITO : Estilo.PELIGRO);
        etiquetaMensaje.setBackground(exito ? Estilo.EXITO_FONDO : Estilo.PELIGRO_FONDO);
        temporizadorMensaje.restart();
    }

    /** Mensaje gris de ayuda que se muestra cuando no hay otro mensaje. */
    private void mostrarAyuda() {
        etiquetaMensaje.setText(lista.estaVacia()
                ? "La lista está vacía. Use «+ Agregar producto» para crear el primero."
                : "Seleccione un producto para editarlo, ver sus imágenes o eliminarlo. Doble clic en una fila para editar.");
        etiquetaMensaje.setForeground(Estilo.TEXTO_SECUNDARIO);
        etiquetaMensaje.setBackground(Estilo.FONDO);
    }
}
