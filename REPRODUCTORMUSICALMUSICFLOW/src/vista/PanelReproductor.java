package vista;

import controlador.AudioController;
import controlador.BibliotecaController;
import modelo.Cancion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class PanelReproductor extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AudioController audioController;
    private final BibliotecaController miBibliotecaController;

    private JLabel lblEstado, lblTiempo;
    private JSlider sliderProgreso;
    private Timer timerProgreso;
    private JButton btnPlayPause;
    private boolean enReproduccion = false;
    private Cancion cancionActual;
    private List<Cancion> listaActual;

    private PanelMetadatos panelMetadatos;
    private JButton btnVolumen;
    private JPopupMenu popupVolumen;
    private JSlider sliderVolumen;

    public PanelReproductor(BibliotecaController bibliotecaController) {
        this.miBibliotecaController = bibliotecaController;
        this.audioController = new AudioController();

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // Biblioteca
        JPanel panelBiblioteca = new JPanel(new BorderLayout());
        PanelBiblioteca listaBiblioteca = new PanelBiblioteca(miBibliotecaController, (cancion, lista) -> {
            cancionActual = cancion;
            listaActual = lista;
            cargarCancion(cancion.getRutaArchivo());
        });
        panelBiblioteca.add(listaBiblioteca, BorderLayout.CENTER);
        tabbedPane.addTab("💽 Biblioteca", panelBiblioteca);

        // Playlists
        PanelPlaylists panelPlaylists = new PanelPlaylists(miBibliotecaController, (cancion, lista) -> {
            cancionActual = cancion;
            listaActual = lista;
            cargarCancion(cancion.getRutaArchivo());
        });
        tabbedPane.addTab("🗃️ Playlists", panelPlaylists);

        // Metadatos
        panelMetadatos = new PanelMetadatos(audioController);
        tabbedPane.addTab("🎧 Canción", panelMetadatos);
        add(tabbedPane, BorderLayout.CENTER);

        // Panel de controles
        JPanel panelControles = new JPanel();

        JButton btnAnterior = new JButton("⏮");
        btnAnterior.addActionListener(e -> reproducirAnterior());

        btnPlayPause = new JButton("▶");
        btnPlayPause.addActionListener(e -> togglePlayPause());

        JButton btnSiguiente = new JButton("⏭");
        btnSiguiente.addActionListener(e -> reproducirSiguiente());

        btnVolumen = new JButton("🔊");
        btnVolumen.addActionListener(e -> {
            popupVolumen.pack(); // Calcula correctamente el tamaño
            int popupHeight = popupVolumen.getPreferredSize().height;
            popupVolumen.show(btnVolumen, 0, -popupHeight); // Mostrar encima del botón
        });

        // Slider vertical dentro del popup
        sliderVolumen = new JSlider(JSlider.VERTICAL, 0, 100, 50);
        sliderVolumen.addChangeListener(e -> {
            int vol = sliderVolumen.getValue();
            audioController.setVolumen(vol);
            btnVolumen.setText(vol == 0 ? "🔇" : "🔊");
        });

        popupVolumen = new JPopupMenu();
        popupVolumen.add(sliderVolumen);


        panelControles.add(btnAnterior);
        panelControles.add(btnPlayPause);
        panelControles.add(btnSiguiente);
        panelControles.add(btnVolumen);

        // Progreso
        sliderProgreso = new JSlider();
        sliderProgreso.setValue(0);
        sliderProgreso.setEnabled(false);
        lblTiempo = new JLabel("   00:00 / 00:00");
        JPanel panelProgreso = new JPanel(new BorderLayout());
        panelProgreso.add(lblTiempo, BorderLayout.WEST);
        panelProgreso.add(sliderProgreso, BorderLayout.CENTER);

        // Estado
        lblEstado = new JLabel("Estado: Listo", SwingConstants.CENTER);

        sliderProgreso.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (sliderProgreso.isEnabled()) {
                    audioController.setPosicion(sliderProgreso.getValue());
                }
            }
        });

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelProgreso, BorderLayout.NORTH);
        panelInferior.add(panelControles, BorderLayout.CENTER);
        panelInferior.add(lblEstado, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void togglePlayPause() {
        if (enReproduccion) {
            audioController.pausar();
            btnPlayPause.setText("▶");
            lblEstado.setText("Estado: Pausado");
        } else {
            audioController.reproducir();
            btnPlayPause.setText("⏸");
            lblEstado.setText("Estado: Reproduciendo");
        }
        enReproduccion = !enReproduccion;
    }

    private void cargarCancion(String ruta) {
        try {
            audioController.cargarCancion(ruta);
            audioController.setVolumen(50); // Volumen inicial a la mitad
            sliderVolumen.setValue(50);
            btnVolumen.setText("🔊");

            int duracion = audioController.getDuracionSegundos();
            sliderProgreso.setMaximum(duracion);
            sliderProgreso.setValue(0);
            sliderProgreso.setEnabled(true);
            lblTiempo.setText("00:00 / " + formatoTiempo(duracion));
            lblEstado.setText("Estado: Cargada - " + new File(ruta).getName());
            enReproduccion = false;
            btnPlayPause.setText("▶");

            if (cancionActual != null) {
                panelMetadatos.mostrarMetadatos(cancionActual);
            }

            iniciarTimerProgreso();
        } catch (Exception e) {
            lblEstado.setText("Estado: Error al cargar canción");
            e.printStackTrace();
        }
    }

    private void reproducirAnterior() {
        if (cancionActual == null || listaActual == null) return;
        int index = listaActual.indexOf(cancionActual);
        if (index > 0) {
            cancionActual = listaActual.get(index - 1);
            cargarCancion(cancionActual.getRutaArchivo());
        }
    }

    private void reproducirSiguiente() {
        if (cancionActual == null || listaActual == null) return;
        int index = listaActual.indexOf(cancionActual);
        if (index < listaActual.size() - 1) {
            cancionActual = listaActual.get(index + 1);
            cargarCancion(cancionActual.getRutaArchivo());
        }
    }

    private void iniciarTimerProgreso() {
        if (timerProgreso != null && timerProgreso.isRunning()) {
            timerProgreso.stop();
        }
        timerProgreso = new Timer(1000, e -> {
            int actual = audioController.getPosicionActualSegundos();
            int total = audioController.getDuracionSegundos();
            sliderProgreso.setValue(actual);
            lblTiempo.setText("" + formatoTiempo(actual) + " / " + formatoTiempo(total));
        });
        timerProgreso.start();
    }

    private String formatoTiempo(int segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }
}
