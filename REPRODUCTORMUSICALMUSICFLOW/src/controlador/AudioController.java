package controlador;

import javax.sound.sampled.*;
import java.io.*;

public class AudioController {
    private Clip clip;
    private FloatControl volumenControl;
    private boolean pausado = false;
    private int posicionPausada = 0;

    private File wavTemporal;

    public void cargarCancion(String rutaArchivo) throws Exception {
        // Cierra clip anterior
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
        }
        if (wavTemporal != null && wavTemporal.exists()) {
            wavTemporal.delete();
        }

        // Convertir a WAV usando FFmpeg
        wavTemporal = File.createTempFile("temp_audio", ".wav");
        wavTemporal.deleteOnExit();

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y", "-i", rutaArchivo,
                "-ac", "2", "-ar", "44100", // Estéreo, 44.1kHz
                wavTemporal.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process proceso = pb.start();

        // Leer salida de consola de ffmpeg
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                System.out.println("[ffmpeg] " + linea);
            }
        }

        int codigoSalida = proceso.waitFor();
        if (codigoSalida != 0) {
            throw new IOException("FFmpeg falló al convertir el archivo.");
        }

        // Abrir el WAV convertido
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavTemporal);
        clip = AudioSystem.getClip();
        clip.open(audioStream);

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            volumenControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        }

        pausado = false;
        posicionPausada = 0;
    }

    public void reproducir() {
        if (clip != null) {
            if (pausado) {
                clip.setFramePosition(posicionPausada);
                pausado = false;
            }
            clip.start();
        }
    }

    public void pausar() {
        if (clip != null && clip.isRunning()) {
            posicionPausada = clip.getFramePosition();
            clip.stop();
            pausado = true;
        }
    }

    public void setVolumen(float porcentaje) {
        if (volumenControl != null) {
            if (porcentaje == 0) {
                volumenControl.setValue(volumenControl.getMinimum());
            } else {
                float dB = (float) (Math.log(porcentaje / 100.0) * 20.0);
                volumenControl.setValue(dB);
            }
        }
    }

    public int getDuracionSegundos() {
        if (clip != null) {
            return (int) (clip.getMicrosecondLength() / 1_000_000);
        }
        return 0;
    }

    public int getPosicionActualSegundos() {
        if (clip != null) {
            return (int) (clip.getMicrosecondPosition() / 1_000_000);
        }
        return 0;
    }

    public void setPosicion(int segundos) {
        if (clip != null) {
            clip.setMicrosecondPosition(segundos * 1_000_000L);
        }
    }
}

