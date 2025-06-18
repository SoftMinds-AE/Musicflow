package vista;

import controlador.BibliotecaController;
import controlador.PlaylistController;
import modelo.Cancion;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class PanelPlaylists extends JPanel {
    private static final long serialVersionUID = 1L;

    private final PlaylistController playlistController;
    private final BibliotecaController bibliotecaController;

    private final DefaultListModel<String> modeloPlaylists;
    private final JList<String> listaPlaylists;

    private final DefaultListModel<Cancion> modeloCanciones;
    private final JList<Cancion> listaCanciones;

    private BiConsumer<Cancion, List<Cancion>> onCancionDobleClick;

    public PanelPlaylists(BibliotecaController bibliotecaController, BiConsumer<Cancion, List<Cancion>> onCancionDobleClick) {
        this.bibliotecaController = bibliotecaController;
        this.playlistController = new PlaylistController();
        this.onCancionDobleClick = onCancionDobleClick;
        setLayout(new BorderLayout());

        modeloPlaylists = new DefaultListModel<>();
        listaPlaylists = new JList<>(modeloPlaylists);
        listaPlaylists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaPlaylists.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarCancionesDePlaylist(listaPlaylists.getSelectedValue());
            }
        });

        JButton btnNuevaPlaylist = new JButton("➕ Nueva Playlist");
        btnNuevaPlaylist.addActionListener(e -> crearNuevaPlaylist());

        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.add(new JScrollPane(listaPlaylists), BorderLayout.CENTER);
        panelIzquierdo.add(btnNuevaPlaylist, BorderLayout.SOUTH);

        modeloCanciones = new DefaultListModel<>();
        listaCanciones = new JList<>(modeloCanciones);
        listaCanciones.setCellRenderer(new BibliotecaRenderer());

        listaCanciones.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Cancion seleccionada = listaCanciones.getSelectedValue();
                    if (seleccionada != null && onCancionDobleClick != null) {
                        onCancionDobleClick.accept(seleccionada, playlistController.obtenerCancionesDePlaylist(listaPlaylists.getSelectedValue()));
                    }
                }
            }
        });

        JScrollPane scrollCanciones = new JScrollPane(listaCanciones);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        JButton btnAgregarCancion = new JButton("➕ Añadir Canción");
        btnAgregarCancion.addActionListener(e -> mostrarPopupAgregarCancion());
        panelDerecho.add(btnAgregarCancion, BorderLayout.NORTH);
        panelDerecho.add(scrollCanciones, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        actualizarListaPlaylists();
    }

    private void actualizarListaPlaylists() {
        modeloPlaylists.clear();
        Set<String> nombres = playlistController.obtenerNombresPlaylists();
        for (String nombre : nombres) {
            modeloPlaylists.addElement(nombre);
        }
    }

    private void cargarCancionesDePlaylist(String nombrePlaylist) {
        modeloCanciones.clear();
        if (nombrePlaylist == null) return;

        List<Cancion> canciones = playlistController.obtenerCancionesDePlaylist(nombrePlaylist);
        for (Cancion c : canciones) {
            modeloCanciones.addElement(c);
        }
    }

    private void crearNuevaPlaylist() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva playlist:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            playlistController.crearPlaylist(nombre.trim());
            actualizarListaPlaylists();
        }
    }

    private void mostrarPopupAgregarCancion() {
        if (listaPlaylists.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione primero una playlist.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nombrePlaylist = listaPlaylists.getSelectedValue();

        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Agregar Canción", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setSize(500, 400);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());

        JTextField campoBusqueda = new JTextField();
        JPanel panelResultados = new JPanel();
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        JScrollPane scrollResultados = new JScrollPane(panelResultados);

        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { buscar(); }
            public void removeUpdate(DocumentEvent e) { buscar(); }
            public void changedUpdate(DocumentEvent e) { buscar(); }

            private void buscar() {
                String texto = campoBusqueda.getText().trim().toLowerCase();
                panelResultados.removeAll();

                List<Cancion> todas = bibliotecaController.getBiblioteca();
                for (Cancion c : todas) {
                    if (c.getTitulo().toLowerCase().contains(texto) ||
                        c.getArtista().toLowerCase().contains(texto) ||
                        c.getAlbum().toLowerCase().contains(texto)) {

                        JPanel panelCancion = PanelPlaylists.this.crearPanelCancion(c, nombrePlaylist);
                        panelResultados.add(panelCancion);
                    }
                }
                panelResultados.revalidate();
                panelResultados.repaint();
            }
        });

        dialogo.add(campoBusqueda, BorderLayout.NORTH);
        dialogo.add(scrollResultados, BorderLayout.CENTER);

        // Inicializamos con búsqueda vacía
        campoBusqueda.setText("");

        dialogo.setVisible(true);
    }
    
    private JPanel crearPanelCancion(Cancion cancion, String nombrePlaylist) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        JLabel labelImagen = new JLabel(new ImageIcon(cancion.getPortada().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        JLabel labelTexto = new JLabel("<html><b>" + cancion.getTitulo() + "</b><br>" + cancion.getArtista() + "</html>");
        JButton botonAgregar = new JButton("+");

        botonAgregar.addActionListener(e -> {
            System.out.println("➕ Agregando canción: " + cancion.getTitulo() + " a playlist: " + nombrePlaylist);
            playlistController.agregarCancionAPlaylist(nombrePlaylist, cancion);
            cargarCancionesDePlaylist(nombrePlaylist);
        });

        panel.add(labelImagen, BorderLayout.WEST);
        panel.add(labelTexto, BorderLayout.CENTER);
        panel.add(botonAgregar, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return panel;
    }



    private static class BibliotecaRenderer extends JPanel implements ListCellRenderer<Cancion> {
        private final JLabel labelTitulo = new JLabel();
        private final JLabel labelArtista = new JLabel();
        private final JLabel labelImagen = new JLabel();

        public BibliotecaRenderer() {
            setLayout(new BorderLayout(10, 0));
            JPanel panelTexto = new JPanel(new GridLayout(2, 1));
            panelTexto.add(labelTitulo);
            panelTexto.add(labelArtista);
            add(labelImagen, BorderLayout.WEST);
            add(panelTexto, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Cancion> list, Cancion value, int index, boolean isSelected, boolean cellHasFocus) {
            labelTitulo.setText(value.getTitulo());
            labelArtista.setText(value.getArtista());
            labelImagen.setIcon(new ImageIcon(value.getPortada().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            return this;
        }
    }
}
