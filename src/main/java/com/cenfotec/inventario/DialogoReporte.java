package com.cenfotec.inventario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

/**
 * Reporte de costos: el costo total de cada producto (precio x cantidad) y el
 * costo total acumulado de la lista. Los datos salen de recorrer la lista enlazada.
 */
public class DialogoReporte extends JDialog {

    public DialogoReporte(JFrame padre, ListaProductos lista) {
        super(padre, "Reporte de costos", true);
        double total = lista.calcularCostoTotal();

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);
        JLabel titulo = Estilo.crearEtiqueta("Reporte de costos", Font.BOLD, 20, Estilo.TEXTO);
        JLabel subtitulo = Estilo.crearEtiqueta("Costo total de cada producto (precio × cantidad), en el orden de la lista.",
                Font.PLAIN, 13, Estilo.TEXTO_SECUNDARIO);
        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 16, 0));
        tarjetas.setOpaque(false);
        JLabel valorTotal = new JLabel(Estilo.moneda(total));
        valorTotal.setToolTipText(valorTotal.getText()); // por si un valor muy grande no cabe
        tarjetas.add(Estilo.crearTarjeta("Valor total del inventario", valorTotal, "Costo total acumulado"));
        tarjetas.add(Estilo.crearTarjeta("Productos", new JLabel(String.valueOf(lista.getTamanio())), "Nodos en la lista"));
        tarjetas.add(Estilo.crearTarjeta("Unidades", new JLabel(String.format(Locale.US, "%,d", lista.contarUnidades())),
                "Suma de las cantidades"));
        valorTotal.setForeground(Estilo.PRIMARIO);
        tarjetas.setAlignmentX(Component.LEFT_ALIGNMENT); // igual que los titulos
        titulos.add(titulo);
        titulos.add(subtitulo);
        titulos.add(Box.createVerticalStrut(16));
        titulos.add(tarjetas);

        JButton botonCerrar = Estilo.crearBoton("Cerrar", Estilo.PANEL, Estilo.TEXTO);
        botonCerrar.addActionListener(e -> dispose());
        JLabel etiquetaTotal = Estilo.crearEtiqueta("Costo total acumulado de la lista: " + Estilo.moneda(total),
                Font.BOLD, 15, Estilo.TEXTO);
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setOpaque(false);
        inferior.add(etiquetaTotal, BorderLayout.WEST);
        inferior.add(botonCerrar, BorderLayout.EAST);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBackground(Estilo.FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        contenido.add(titulos, BorderLayout.NORTH);
        contenido.add(lista.estaVacia() ? crearMensajeVacio() : crearTabla(lista, total), BorderLayout.CENTER);
        contenido.add(inferior, BorderLayout.SOUTH);
        setContentPane(contenido);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(860, 580);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(padre);
    }

    /** Tabla con una fila por producto, llenada recorriendo la lista desde la cabeza. */
    private JScrollPane crearTabla(ListaProductos lista, double total) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"#", "Producto", "Cantidad", "Precio unitario", "Costo total", "% del total"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        int posicion = 1;
        Nodo actual = lista.getCabeza();
        while (actual != null) {
            Producto p = actual.getProducto();
            double costo = p.calcularCostoTotal();
            double porcentaje = total > 0 ? costo * 100 / total : 0;
            modelo.addRow(new Object[]{posicion, p.getNombre(), p.getCantidad(), p.getPrecio(), costo, porcentaje});
            actual = actual.getSiguiente();
            posicion++;
        }

        JTable tabla = new JTable(modelo);
        Estilo.configurarTabla(tabla);
        int[] anchos = {50, 220, 100, 150, 190, 110};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        scroll.getViewport().setBackground(Estilo.PANEL);
        return scroll;
    }

    private JLabel crearMensajeVacio() {
        JLabel mensaje = Estilo.crearEtiqueta("No hay productos registrados. Agregue productos para ver el reporte.",
                Font.PLAIN, 14, Estilo.TEXTO_SECUNDARIO);
        mensaje.setHorizontalAlignment(SwingConstants.CENTER);
        mensaje.setOpaque(true);
        mensaje.setBackground(Estilo.PANEL);
        mensaje.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
        return mensaje;
    }
}
