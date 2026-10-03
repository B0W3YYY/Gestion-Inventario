package com.cenfotec.inventario;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

/**
 * Colores, fuentes, formatos y componentes comunes de la interfaz grafica.
 * Tenerlos en un solo lugar mantiene el mismo aspecto en todas las ventanas.
 */
public class Estilo {

    // Paleta: fondo gris claro, paneles blancos, azul para acciones,
    // verde para exito, rojo para errores/eliminar y gris para texto secundario.
    public static final Color FONDO = new Color(0xF3F4F6);
    public static final Color PANEL = Color.WHITE;
    public static final Color BORDE = new Color(0xE5E7EB);
    public static final Color BORDE_CAMPO = new Color(0xD1D5DB);
    public static final Color TEXTO = new Color(0x111827);
    public static final Color TEXTO_SECUNDARIO = new Color(0x6B7280);
    public static final Color TEXTO_AYUDA = new Color(0x9CA3AF);
    public static final Color PRIMARIO = new Color(0x2563EB);
    public static final Color SELECCION = new Color(0xDBEAFE);
    public static final Color EXITO = new Color(0x15803D);
    public static final Color EXITO_FONDO = new Color(0xDCFCE7);
    public static final Color PELIGRO = new Color(0xDC2626);
    public static final Color PELIGRO_FONDO = new Color(0xFEE2E2);
    public static final Color ADVERTENCIA = new Color(0xB45309);

    /** Dias de anticipacion para avisar que un producto esta por vencer. */
    public static final int DIAS_AVISO_VENCIMIENTO = 30;

    // Segoe UI es la fuente de Windows; en otros sistemas se usa la fuente sans-serif por defecto.
    private static final String FAMILIA = Arrays.asList(GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains("Segoe UI")
            ? "Segoe UI" : Font.SANS_SERIF;

    /** Usa el aspecto del sistema operativo y una misma fuente en todos los componentes. */
    public static void aplicarTema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, Swing usa su aspecto por defecto y la aplicacion funciona igual.
        }
        FontUIResource fuenteBase = new FontUIResource(fuente(Font.PLAIN, 14));
        for (Object clave : UIManager.getLookAndFeelDefaults().keySet().toArray()) {
            if (UIManager.get(clave) instanceof FontUIResource) {
                UIManager.put(clave, fuenteBase);
            }
        }
    }

    public static Font fuente(int estilo, int tamanio) {
        return new Font(FAMILIA, estilo, tamanio);
    }

    /** Da formato de colones con 2 decimales, por ejemplo: ₡1,500.50 */
    public static String moneda(double valor) {
        return String.format(Locale.US, "₡%,.2f", valor);
    }

    public static JLabel crearEtiqueta(String texto, int estilo, int tamanio, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(estilo, tamanio));
        etiqueta.setForeground(color);
        return etiqueta;
    }

    /** Boton plano con esquinas redondeadas. Si el fondo es blanco se le dibuja un borde gris. */
    public static JButton crearBoton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Color color = fondo;
                if (!isEnabled()) {
                    color = FONDO;
                } else if (getModel().isRollover()) {
                    color = fondo.equals(PANEL) ? FONDO : fondo.darker();
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (fondo.equals(PANEL)) {
                    g2.setColor(BORDE_CAMPO);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g); // dibuja el texto encima del fondo
            }
        };
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setRolloverEnabled(true);
        boton.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setForeground(colorTexto);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    /** Campo de texto con borde suave y un texto de ayuda gris que se ve mientras esta vacio. */
    public static JTextField crearCampo(String textoAyuda) {
        JTextField campo = new JTextField(22) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(TEXTO_AYUDA);
                    FontMetrics medidas = g2.getFontMetrics();
                    int y = (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent();
                    g2.drawString(textoAyuda, getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        marcarCampo(campo, false);
        return campo;
    }

    /** Pinta el borde del campo en rojo si tiene un error, o en gris si esta bien. */
    public static void marcarCampo(JTextField campo, boolean conError) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(conError ? PELIGRO : BORDE_CAMPO),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    /** Tarjeta blanca de indicador: titulo pequeno, valor grande y una nota explicativa. */
    public static JPanel crearTarjeta(String titulo, JLabel valor, String nota) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(PANEL);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        valor.setFont(fuente(Font.BOLD, 24));
        valor.setForeground(TEXTO);
        tarjeta.add(crearEtiqueta(titulo, Font.BOLD, 13, TEXTO_SECUNDARIO));
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(valor);
        tarjeta.add(Box.createVerticalStrut(2));
        tarjeta.add(crearEtiqueta(nota, Font.PLAIN, 12, TEXTO_SECUNDARIO));
        return tarjeta;
    }

    /** Da a la tabla el aspecto comun: filas altas, solo lineas horizontales y encabezado sobrio. */
    public static void configurarTabla(JTable tabla) {
        tabla.setRowHeight(36);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1)); // sin huecos verticales entre celdas
        tabla.setGridColor(BORDE);
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(TEXTO);
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(PANEL);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderizadorCelda());
        tabla.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                           boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(t, valor, false, false, fila, columna);
                setFont(fuente(Font.BOLD, 13));
                setForeground(TEXTO_SECUNDARIO);
                setBackground(new Color(0xF9FAFB));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                        BorderFactory.createEmptyBorder(9, 10, 9, 10)));
                // El titulo se alinea igual que los datos de su columna.
                boolean esNumero = t.getRowCount() > 0 && t.getValueAt(0, columna) instanceof Number;
                setHorizontalAlignment(esNumero ? RIGHT : LEFT);
                return this;
            }
        });
    }

    /**
     * Dibuja cada celda segun el tipo de dato: textos a la izquierda, numeros
     * y dinero a la derecha, y fechas en rojo (vencido) o naranja (por vencer).
     */
    private static class RenderizadorCelda extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, false, fila, columna);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setForeground(TEXTO);
            setHorizontalAlignment(valor instanceof Number ? RIGHT : LEFT);

            if (valor instanceof Double && tabla.getColumnName(columna).startsWith("%")) {
                setText(String.format(Locale.US, "%.1f %%", (Double) valor));
            } else if (valor instanceof Double) {
                setText(moneda((Double) valor));
            } else if (valor instanceof Integer) {
                setText(String.format(Locale.US, "%,d", (Integer) valor));
            } else if (valor instanceof LocalDate) {
                LocalDate fecha = (LocalDate) valor;
                LocalDate hoy = LocalDate.now();
                setText(fecha.format(ReglasProducto.FORMATO_FECHA));
                if (fecha.isBefore(hoy)) {
                    setText(getText() + " · Vencido");
                    setForeground(PELIGRO);
                } else if (!fecha.isAfter(hoy.plusDays(DIAS_AVISO_VENCIMIENTO))) {
                    setText(getText() + " · Por vencer");
                    setForeground(ADVERTENCIA);
                }
            } else if (valor == null) {
                setText("—"); // el producto no tiene fecha de vencimiento
                setForeground(TEXTO_AYUDA);
            }
            // Si el texto no cabe en la columna, se puede leer completo al pasar el mouse.
            int anchoDisponible = tabla.getColumnModel().getColumn(columna).getWidth() - 20;
            setToolTipText(getFontMetrics(getFont()).stringWidth(getText()) > anchoDisponible ? getText() : null);
            return this;
        }
    }
}
