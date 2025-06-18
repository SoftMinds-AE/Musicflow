package controlador;

import modelo.Cancion;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;

public class BibliotecaController {

    private List<Cancion> biblioteca;
    private static final String[] FORMATOS_SOPORTADOS = {
        ".mp3", ".wav", ".flac", ".ogg", ".aac", ".m4a"
    };

    public BibliotecaController() {
        biblioteca = new ArrayList<>();
    }

    public void cargarCancionesDesdeRuta(String rutaArchivo) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.out.println("Ruta inválida: " + rutaArchivo);
            return;
        }

        if (archivo.isFile()) {
            if (esArchivoDeAudio(archivo)) {
                agregarCancionConMetadatos(archivo);
            }
        } else if (archivo.isDirectory()) {
            escanearDirectorio(archivo);
        }
    }

    private void escanearDirectorio(File directorio) {
        File[] archivos = directorio.listFiles();
        if (archivos == null) return;

        for (File archivo : archivos) {
            if (archivo.isDirectory()) {
                escanearDirectorio(archivo);
            } else if (esArchivoDeAudio(archivo)) {
                agregarCancionConMetadatos(archivo);
            }
        }
    }

    private boolean esArchivoDeAudio(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        for (String extension : FORMATOS_SOPORTADOS) {
            if (nombre.endsWith(extension)) return true;
        }
        return false;
    }

    private void agregarCancionConMetadatos(File archivo) {
        String nombre = archivo.getName();
        String path = archivo.getAbsolutePath();
        String extension = nombre.toLowerCase();

        String artista = null;
        String album = null;
        String genero = null;
        String titulo = null;
        BufferedImage portada = null;

        if (extension.endsWith(".mp3")) {
            // Extraer metadatos con mp3agic
            try {
                Mp3File mp3file = new Mp3File(path);
                if (mp3file.hasId3v2Tag()) {
                    ID3v2 tag = mp3file.getId3v2Tag();
                    titulo = tag.getTitle();
                    artista = tag.getArtist();
                    album = tag.getAlbum();
                    genero = tag.getGenreDescription();

                    byte[] imageData = tag.getAlbumImage();
                    if (imageData != null) {
                        portada = ImageIO.read(new ByteArrayInputStream(imageData));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error extrayendo metadatos con mp3agic: " + e.getMessage());
            }

        } else {
            // Usar jaudiotagger para el resto de formatos
            try {
                AudioFile audioFile = AudioFileIO.read(archivo);
                Tag tag = audioFile.getTag();
                if (tag != null) {
                    artista = tag.getFirst(FieldKey.ARTIST);
                    album = tag.getFirst(FieldKey.ALBUM);
                    genero = tag.getFirst(FieldKey.GENRE);
                    titulo = tag.getFirst(FieldKey.TITLE);

                    Artwork artwork = tag.getFirstArtwork();
                    if (artwork != null) {
                        byte[] imageData = artwork.getBinaryData();
                        portada = ImageIO.read(new ByteArrayInputStream(imageData));
                    }
                }
            } catch (Exception e) {
                System.out.println("Error leyendo metadatos con jaudiotagger: " + e.getMessage());
            }
        }

        Cancion nueva = new Cancion(nombre, artista, album, path);
        nueva.setGenero(genero);
        nueva.setTitulo(titulo);
        nueva.setPortada(portada);

        biblioteca.add(nueva);

        System.out.println("Añadida: " + (titulo != null ? titulo : nombre) +
                " | Artista: " + (artista != null ? artista : "Desconocido") +
                " | Álbum: " + (album != null ? album : "Desconocido") +
                " | Género: " + (genero != null ? genero : "Desconocido") +
                " | Portada: " + (portada != null ? "Sí" : "No"));
    }

    public List<Cancion> getBiblioteca() {
        return biblioteca;
    }
}
