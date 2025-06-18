package vista;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import controlador.BibliotecaController;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private PanelReproductor panelReproductor;
    private JPanel panelPrincipal;

    public MainWindow() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Musicflow");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        panelPrincipal = new JPanel(new BorderLayout());

        JPanel barraSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnTemas = new JButton("🎨 Cambiar Tema");
        btnTemas.addActionListener(e -> mostrarMenuTemas(btnTemas));
        barraSuperior.add(btnTemas);
        panelPrincipal.add(barraSuperior, BorderLayout.NORTH);

        BibliotecaController bibliotecaController = new BibliotecaController();
        panelReproductor = new PanelReproductor(bibliotecaController);
        panelPrincipal.add(panelReproductor, BorderLayout.CENTER);

        setContentPane(panelPrincipal);
    }

    private void mostrarMenuTemas(Component invocador) {
        JPopupMenu menu = new JPopupMenu();

        // Temas básicos
        String[] temasBasicos = {"Flat Light", "Flat Dark", "IntelliJ", "Darcula"};
        for (String tema : temasBasicos) {
            JMenuItem item = new JMenuItem(tema);
            item.addActionListener(e -> cambiarTema(tema));
            menu.add(item);
        }

        menu.addSeparator();

        // Temas IntelliJ Platform (excluyendo Material)
        Object[][] temasIntelliJ = {
            {"Arc", FlatArcIJTheme.class},
            {"Arc Orange", FlatArcOrangeIJTheme.class},
            {"Arc Dark", FlatArcDarkIJTheme.class},
            {"Carbon", FlatCarbonIJTheme.class},
            {"Cobalt 2", FlatCobalt2IJTheme.class},
            {"Dracula", FlatDraculaIJTheme.class},
            {"Gray", FlatGrayIJTheme.class},
            {"Gruvbox Dark Hard", FlatGruvboxDarkHardIJTheme.class},
            {"Hiberbee Dark", FlatHiberbeeDarkIJTheme.class},
            {"High contrast", FlatHighContrastIJTheme.class},
            {"Monocai", FlatMonocaiIJTheme.class},
            {"Nord", FlatNordIJTheme.class},
            {"One Dark", FlatOneDarkIJTheme.class},
            {"Solarized Dark", FlatSolarizedDarkIJTheme.class},
            {"Solarized Light", FlatSolarizedLightIJTheme.class},
            {"Spacegray", FlatSpacegrayIJTheme.class},
        };

        JMenu submenuIntelliJ = new JMenu("Temas IntelliJ");
        for (Object[] tema : temasIntelliJ) {
            String nombre = (String) tema[0];
            Class<?> clase = (Class<?>) tema[1];

            JMenuItem item = new JMenuItem(nombre);
            item.addActionListener(e -> cambiarTemaClase(clase));
            submenuIntelliJ.add(item);
        }

        menu.add(submenuIntelliJ);
        menu.show(invocador, 0, invocador.getHeight());
    }

    private void cambiarTema(String nombreTema) {
        try {
            switch (nombreTema) {
                case "Flat Light" -> UIManager.setLookAndFeel(new FlatLightLaf());
                case "Flat Dark" -> UIManager.setLookAndFeel(new FlatDarkLaf());
                case "IntelliJ" -> UIManager.setLookAndFeel(new FlatIntelliJLaf());
                case "Darcula" -> UIManager.setLookAndFeel(new FlatDarculaLaf());
            }

            SwingUtilities.updateComponentTreeUI(this);
            this.pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cambiarTemaClase(Class<?> themeClass) {
        try {
            LookAndFeel laf = (LookAndFeel) themeClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);
            SwingUtilities.updateComponentTreeUI(this);
            this.pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
