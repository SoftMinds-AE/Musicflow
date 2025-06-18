package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

import controlador.BibliotecaController;
import modelo.Cancion;

public class PanelBiblioteca extends JPanel {
    private static final long serialVersionUID = 1L;

    private BibliotecaController bibliotecaController;
    private BiConsumer<Cancion, List<Cancion>> onCancionSeleccionada;

    private JTabbedPane tabbedPane;
    private Map<String, JList<Cancion>> listasPorCategoria;
    private Map<String, DefaultListModel<Cancion>> modelosPorCategoria;

    public PanelBiblioteca(BibliotecaController controller, BiConsumer<Cancion, List<Cancion>> onCancionSeleccionada) {
        this.bibliotecaController = controller;
        this.onCancionSeleccionada = onCancionSeleccionada;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        listasPorCategoria = new HashMap<>();
        modelosPorCategoria = new HashMap<>();

        crearTab("Todas");
        crearTabConGrupos("Artista");
        crearTabConGrupos("Álbum");
        crearTabConGrupos("Género");

        add(tabbedPane, BorderLayout.CENTER);

        JButton btnAgregar = new JButton("Añadir canciones o carpetas");
        btnAgregar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] seleccionados = fileChooser.getSelectedFiles();
                for (File archivo : seleccionados) {
                    bibliotecaController.cargarCancionesDesdeRuta(archivo.getAbsolutePath());
                }
                actualizarTodasLasListas();
            }
        });

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBoton.add(btnAgregar);
        add(panelBoton, BorderLayout.NORTH);

        actualizarTodasLasListas();
    }

    private void crearTab(String nombre) {
        DefaultListModel<Cancion> modelo = new DefaultListModel<>();
        JList<Cancion> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new CancionCellRenderer());

        lista.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lista.getSelectedIndex() != -1) {
                    Cancion seleccionada = lista.getSelectedValue();
                    
                    if (onCancionSeleccionada != null) {
                        onCancionSeleccionada.accept(seleccionada, Collections.list(modelo.elements()));
                    }
                }
            }
        });

        lista.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && lista.getSelectedIndex() != -1) {
                    Cancion seleccionada = lista.getSelectedValue();
                    if (onCancionSeleccionada != null) {
                        onCancionSeleccionada.accept(seleccionada, Collections.list(modelo.elements()));
                    }
                }
            }
        });

        listasPorCategoria.put(nombre, lista);
        modelosPorCategoria.put(nombre, modelo);
        tabbedPane.addTab(nombre, new JScrollPane(lista));
    }

    private void crearTabConGrupos(String categoria) {
        DefaultListModel<String> modelo = new DefaultListModel<>();
        JList<String> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new GrupoCellRenderer(categoria, new HashMap<>()));

        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scroll = new JScrollPane(lista);
        panel.add(scroll, BorderLayout.CENTER);

        listasPorCategoria.put(categoria, null);
        modelosPorCategoria.put(categoria, null);

        lista.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lista.getSelectedIndex() != -1) {
                    mostrarCancionesPorCategoria(categoria, lista.getSelectedValue(), panel);
                }
            }
        });

        lista.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && lista.getSelectedIndex() != -1) {
                    mostrarCancionesPorCategoria(categoria, lista.getSelectedValue(), panel);
                }
            }
        });

        tabbedPane.addTab(categoria, panel);
    }

    private void mostrarCancionesPorCategoria(String categoria, String valor, JPanel panel) {
        panel.removeAll();

        JButton btnVolver = new JButton("← Volver");
        btnVolver.addActionListener(e -> actualizarTodasLasListas());
        panel.add(btnVolver, BorderLayout.NORTH);

        DefaultListModel<Cancion> modeloCanciones = new DefaultListModel<>();
        List<Cancion> listaCancionesFiltradas = new ArrayList<>();
        JList<Cancion> listaCanciones = new JList<>(modeloCanciones);
        listaCanciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaCanciones.setCellRenderer(new CancionCellRenderer());

        for (Cancion c : bibliotecaController.getBiblioteca()) {
            boolean coincide = switch (categoria) {
                case "Artista" -> valor.equals(c.getArtista());
                case "Álbum"   -> valor.equals(c.getAlbum());
                case "Género"  -> valor.equals(c.getGenero());
                default -> false;
            };
            if (coincide) {
                modeloCanciones.addElement(c);
                listaCancionesFiltradas.add(c);
            }
        }

        listaCanciones.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && listaCanciones.getSelectedIndex() != -1) {
                    if (onCancionSeleccionada != null) {
                        onCancionSeleccionada.accept(listaCanciones.getSelectedValue(), listaCancionesFiltradas);
                    }
                }
            }
        });

        listaCanciones.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && listaCanciones.getSelectedIndex() != -1) {
                    if (onCancionSeleccionada != null) {
                        onCancionSeleccionada.accept(listaCanciones.getSelectedValue(), listaCancionesFiltradas);
                    }
                }
            }
        });

        panel.add(new JScrollPane(listaCanciones), BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    public void actualizarTodasLasListas() {
        DefaultListModel<Cancion> modeloTodas = modelosPorCategoria.get("Todas");
        if (modeloTodas != null) modeloTodas.clear();

        Map<String, Set<String>> categorias = new HashMap<>();
        Map<String, ImageIcon> portadasPorAlbum = new HashMap<>();

        categorias.put("Artista", new TreeSet<>());
        categorias.put("Álbum", new TreeSet<>());
        categorias.put("Género", new TreeSet<>());

        for (Cancion c : bibliotecaController.getBiblioteca()) {
            if (modeloTodas != null) modeloTodas.addElement(c);

            if (c.getArtista() != null && !c.getArtista().isEmpty()) categorias.get("Artista").add(c.getArtista());
            if (c.getAlbum() != null && !c.getAlbum().isEmpty()) {
                categorias.get("Álbum").add(c.getAlbum());
                if (c.getPortada() != null && !portadasPorAlbum.containsKey(c.getAlbum())) {
                    Image scaled = c.getPortada().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                    portadasPorAlbum.put(c.getAlbum(), new ImageIcon(scaled));
                }
            }
            if (c.getGenero() != null && !c.getGenero().isEmpty()) categorias.get("Género").add(c.getGenero());
        }

        for (String categoria : Arrays.asList("Artista", "Álbum", "Género")) {
            Component tab = tabbedPane.getComponentAt(tabbedPane.indexOfTab(categoria));
            if (tab instanceof JPanel panel) {
                panel.removeAll();
                DefaultListModel<String> modelo = new DefaultListModel<>();
                for (String valor : categorias.get(categoria)) {
                    modelo.addElement(valor);
                }

                JList<String> lista = new JList<>(modelo);
                lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                lista.setCellRenderer(new GrupoCellRenderer(categoria, portadasPorAlbum));

                lista.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && lista.getSelectedIndex() != -1) {
                            mostrarCancionesPorCategoria(categoria, lista.getSelectedValue(), panel);
                        }
                    }
                });

                lista.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER && lista.getSelectedIndex() != -1) {
                            mostrarCancionesPorCategoria(categoria, lista.getSelectedValue(), panel);
                        }
                    }
                });

                panel.add(new JScrollPane(lista), BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }
        }
    }

    // ==== RENDERERS INTERNOS ====

    private static class CancionCellRenderer extends JPanel implements ListCellRenderer<Cancion> {
        private JLabel labelIcono;
        private JLabel labelTitulo;
        private JLabel labelSubtitulo;

        public CancionCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            labelIcono = new JLabel();
            labelTitulo = new JLabel();
            labelSubtitulo = new JLabel();

            labelTitulo.setFont(labelTitulo.getFont().deriveFont(Font.BOLD, 14f));
            labelSubtitulo.setFont(labelSubtitulo.getFont().deriveFont(Font.PLAIN, 11f));

            labelTitulo.setOpaque(false);
            labelSubtitulo.setOpaque(false);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(labelTitulo);
            textPanel.add(labelSubtitulo);

            add(labelIcono, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);

            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Cancion> list, Cancion value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            // Obtener colores del tema actual
            Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();
            Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color subtitleColor = UIManager.getColor("Label.disabledForeground");
            if (subtitleColor == null || isSelected) {
                subtitleColor = fg.darker(); // fallback
            }

            setBackground(bg);
            labelTitulo.setForeground(fg);
            labelSubtitulo.setForeground(subtitleColor);

            labelTitulo.setText(value.getTitulo());
            labelSubtitulo.setText(value.getArtista() + " – " + value.getAlbum());

            // Escalar portada
            if (value.getPortada() != null) {
                Image scaled = value.getPortada().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                labelIcono.setIcon(new ImageIcon(scaled));
            } else {
                labelIcono.setIcon(null);
            }

            return this;
        }
    }

    private static class GrupoCellRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel labelTexto;
        private JLabel labelIcono;
        private String categoria;
        private Map<String, ImageIcon> portadas;

        public GrupoCellRenderer(String categoria, Map<String, ImageIcon> portadas) {
            this.categoria = categoria;
            this.portadas = portadas;
            setLayout(new BorderLayout());
            labelTexto = new JLabel();
            labelIcono = new JLabel();
            labelTexto.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            add(labelIcono, BorderLayout.WEST);
            add(labelTexto, BorderLayout.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            labelTexto.setText(value);
            labelTexto.setFont(labelTexto.getFont().deriveFont(Font.PLAIN, 14f));
            labelIcono.setIcon(null);

            if ("Álbum".equals(categoria)) {
                ImageIcon portada = portadas.get(value);
                if (portada != null) {
                    labelIcono.setIcon(portada);
                    labelIcono.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                }
            }

            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            return this;
        }
    }
}
