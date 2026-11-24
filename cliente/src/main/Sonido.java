package main;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

public final class Sonido {
    private static volatile int volumenMusica = 50; // 0 a 100
    private static volatile boolean musicaActiva;
    private static volatile SourceDataLine lineaMusica;
    private static Thread hiloMusica;

    private Sonido() {}

    public static int getVolumenMusica() { return volumenMusica; }

    public static void setVolumenMusica(int porcentaje) {
        volumenMusica = Math.max(0, Math.min(100, porcentaje));
        SourceDataLine linea = lineaMusica;
        if (linea != null) aplicarPorcentaje(linea, volumenMusica);
    }

    public static synchronized void iniciarMusica(String... rutas) {
        detenerMusica();
        if (rutas == null || rutas.length == 0) return;
        musicaActiva = true;
        hiloMusica = new Thread(() -> {
            int indice = 0;
            byte[] buffer = new byte[4096];
            while (musicaActiva) {
                String ruta = rutas[indice % rutas.length];
                indice++;
                try {
                    InputStream raw = Sonido.class.getResourceAsStream(ruta);
                    if (raw == null) break;
                    try (AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                        AudioFormat formato = audio.getFormat();
                        SourceDataLine linea = AudioSystem.getSourceDataLine(formato);
                        linea.open(formato);
                        aplicarPorcentaje(linea, volumenMusica);
                        lineaMusica = linea;
                        linea.start();
                        int leidos;
                        while (musicaActiva && (leidos = audio.read(buffer, 0, buffer.length)) != -1) {
                            linea.write(buffer, 0, leidos);
                        }
                        if (musicaActiva) linea.drain();
                        linea.stop();
                        linea.close();
                        lineaMusica = null;
                    }
                } catch (Exception ignored) {
                    // Si no hay dispositivo de audio o el formato no es compatible, el juego sigue igual.
                    break;
                }
            }
        }, "musica-fondo");
        hiloMusica.setDaemon(true);
        hiloMusica.start();
    }

    public static synchronized void detenerMusica() {
        musicaActiva = false;
        SourceDataLine linea = lineaMusica;
        if (linea != null) {
            try {
                linea.stop();
                linea.close();
            } catch (Exception ignored) {}
            lineaMusica = null;
        }
        if (hiloMusica != null) {
            hiloMusica.interrupt();
            hiloMusica = null;
        }
    }

    private static void aplicarPorcentaje(Line linea, int porcentaje) {
        try {
            if (linea.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl control = (FloatControl) linea.getControl(FloatControl.Type.MASTER_GAIN);
                float db = porcentaje <= 0
                        ? control.getMinimum()
                        : (float) (20.0 * Math.log10(porcentaje / 100.0));
                float clamped = Math.max(control.getMinimum(), Math.min(control.getMaximum(), db));
                control.setValue(clamped);
            }
        } catch (Exception ignored) {}
    }
}
