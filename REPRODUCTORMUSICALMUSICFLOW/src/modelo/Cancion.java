package modelo;

import java.io.Serializable;
import java.awt.image.BufferedImage;

public class Cancion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String artista;
    private String genero;
    private String album;
    private String rutaArchivo;
    private String titulo;
    private transient BufferedImage portada; // transient para evitar problemas al serializar

    public Cancion(String nombre, String artista, String album, String rutaArchivo) {
        this.nombre = nombre;
        this.artista = artista;
        this.album = album;
        this.rutaArchivo = rutaArchivo;
    }

    // Getters y Setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public BufferedImage getPortada() {
        return portada;
    }

    public void setPortada(BufferedImage portada) {
        this.portada = portada;
    }

    @Override
    public String toString() {
        return (titulo != null ? titulo : nombre) + " - " + (artista != null ? artista : "Desconocido");
    }
}
