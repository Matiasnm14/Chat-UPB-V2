package edu.upb.chatupb_v2.model.entities.enums;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public enum Sound {

    BUZZ("/sounds/nudge.wav"),
    NEW_MESSAGE("/sounds/msg.wav"); // Puedes agregar más a futuro

    private final String path;

    Sound(String path) {
        this.path = path;
    }

    public void play() {
        new Thread(() -> {
            try {
                URL url = getClass().getResource(path);
                if (url != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                } else {
                    System.out.println("No se encontró el archivo de sonido: " + path);
                }
            } catch (Exception e) {
                System.out.println("Error al reproducir el sonido: " + e.getMessage());
            }
        }).start();
    }
}
