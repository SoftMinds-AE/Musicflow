package vista;

import controlador.AudioController;
import modelo.Cancion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

public class PanelMetadatos extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AudioController audioController;

    private JLabel lblPortada;
    private JPanel panelDatos;
    private JSplitPane splitPane;

    private BufferedImage imagenOriginal;

    public PanelMetadatos(AudioController controller) {
        this.audioController = controller;

        setLayout(new BorderLayout());

        lblPortada = new JLabel("", SwingConstants.CENTER);
        lblPortada.setVerticalAlignment(SwingConstants.CENTER);
        lblPortada.setHorizontalAlignment(SwingConstants.CENTER);
        lblPortada.setBackground(Color.BLACK);
        lblPortada.setOpaque(true);

        panelDatos = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));
        panelDatos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        splitPane = new JSplitPane();
        splitPane.setDividerSize(5);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                actualizarLayout();
                escalarYActualizarPortada();
            }
        });
    }

    public void mostrarMetadatos(Cancion cancion) {
        if (cancion == null) return;

        imagenOriginal = cancion.getPortada();

        // Redimensionar imagen cuando se muestre
        escalarYActualizarPortada();

        // Limpiar y actualizar metadatos
        panelDatos.removeAll();
        panelDatos.add(crearEtiqueta("🎵 Título: " + cancion.getTitulo()));
        panelDatos.add(crearEtiqueta("👤 Artista: " + cancion.getArtista()));
        panelDatos.add(crearEtiqueta("💿 Álbum: " + cancion.getAlbum()));
        panelDatos.add(crearEtiqueta("⏱ Duración: " + formatearTiempo(audioController.getDuracionSegundos())));
        panelDatos.add(crearEtiqueta("🎼 Género: " + cancion.getGenero()));
        panelDatos.add(Box.createVerticalGlue());

        actualizarLayout();
        revalidate();
        repaint();
    }

    private void escalarYActualizarPortada() {
        if (imagenOriginal == null) return;

        int contenedorW = lblPortada.getWidth();
        int contenedorH = lblPortada.getHeight();
        if (contenedorW == 0 || contenedorH == 0) return;

        double imgW = imagenOriginal.getWidth();
        double imgH = imagenOriginal.getHeight();

        double escala = Math.min(contenedorW / imgW, contenedorH / imgH);
        int nuevoAncho = (int) (imgW * escala);
        int nuevoAlto = (int) (imgH * escala);

        Image redimensionada = imagenOriginal.getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);
        lblPortada.setIcon(new ImageIcon(redimensionada));
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }

    private void actualizarLayout() {
        remove(splitPane);

        int width = getWidth();
        int height = getHeight();

        if (width > height) {
            // Horizontal
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(lblPortada), new JScrollPane(panelDatos));
        } else {
            // Vertical
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(lblPortada), new JScrollPane(panelDatos));
        }

        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);
        revalidate();
    }

    private String formatearTiempo(int segundos) {
        int mins = segundos / 60;
        int segs = segundos % 60;
        return String.format("%02d:%02d", mins, segs);
    }
}
