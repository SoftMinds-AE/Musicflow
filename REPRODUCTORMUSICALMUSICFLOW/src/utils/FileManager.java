package utils;

import modelo.Cancion;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    public static void guardarBiblioteca(List<Cancion> biblioteca, String rutaArchivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            for (Cancion cancion : biblioteca) {
                writer.println(cancion.getNombre() + "," + cancion.getArtista() + "," + cancion.getGenero() + "," + cancion.getRutaArchivo());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Cancion> cargarBiblioteca(String rutaArchivo) {
        List<Cancion> biblioteca = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 4) {
                    biblioteca.add(new Cancion(datos[0], datos[1], datos[2], datos[3]));
                }
            }
        } catch (IOException e) {
            System.out.println("No se encontró archivo de biblioteca. Se creará uno nuevo.");
        }
        return biblioteca;
    }
}
