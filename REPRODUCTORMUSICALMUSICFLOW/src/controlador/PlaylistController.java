package controlador;

import modelo.Cancion;
import java.util.*;

public class PlaylistController {
    private final Map<String, List<Cancion>> playlists;

    public PlaylistController() {
        playlists = new HashMap<>();
        playlists.put("Cola", new ArrayList<>()); // Playlist predeterminada
        System.out.println("Inicializando PlaylistController con playlist predeterminada: Cola");
    }

    public void crearPlaylist(String nombre) {
        playlists.putIfAbsent(nombre, new ArrayList<>());
        System.out.println("Playlist creada: " + nombre);
    }

    public void agregarCancionAPlaylist(String nombrePlaylist, Cancion cancion) {
        playlists.putIfAbsent(nombrePlaylist, new ArrayList<>());
        List<Cancion> canciones = playlists.get(nombrePlaylist);
        if (!canciones.contains(cancion)) {
            canciones.add(cancion);
            System.out.println("Canción agregada a '" + nombrePlaylist + "': " + cancion.getTitulo());
        } else {
            System.out.println("La canción ya existe en la playlist '" + nombrePlaylist + "': " + cancion.getTitulo());
        }
        System.out.println("Total canciones en '" + nombrePlaylist + "': " + canciones.size());
    }

    public List<Cancion> obtenerCancionesDePlaylist(String nombrePlaylist) {
        List<Cancion> canciones = playlists.getOrDefault(nombrePlaylist, new ArrayList<>());
        System.out.println("Obteniendo canciones de '" + nombrePlaylist + "': " + canciones.size() + " canciones");
        return new ArrayList<>(canciones);
    }

    public void reemplazarPlaylist(String nombrePlaylist, List<Cancion> nuevasCanciones) {
        playlists.put(nombrePlaylist, new ArrayList<>(nuevasCanciones));
        System.out.println("Reemplazada playlist '" + nombrePlaylist + "' con " + nuevasCanciones.size() + " canciones.");
    }

    public List<Cancion> obtenerSublistaDesdeCancion(Cancion cancion, List<Cancion> listaOriginal) {
        int index = listaOriginal.indexOf(cancion);
        if (index >= 0) {
            return listaOriginal.subList(index, listaOriginal.size());
        }
        return new ArrayList<>();
    }

    public Set<String> obtenerNombresPlaylists() {
        return playlists.keySet();
    }
}
