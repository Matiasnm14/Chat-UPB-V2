package edu.upb.chatupb_v2.model.audio;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.InputStream;

public class AudioMain {
    public AudioMain() {

    }

    public void playMainSound() {
        Player player = null;
        try {
            // cuando no logre leer de resources
            // 1. revisar que el nombre este bien escrito (nombre y extension)
            // 2. revisar que se encuentre en target/classes, sino se encuentra ejecutar en maven: clean compile
            // 3. escribirlo manualmente. a veces se copia algun caracter raro

            //InputStream isMainAudio = new FileInputStream("D:\\Throw.mp3"); // funciona
            InputStream isMainAudio = this.getClass().getClassLoader().getResourceAsStream("sounds/Voicy_pvz_theme.mp3");
            System.out.println("inputStream: " + isMainAudio);

            player = new Player(isMainAudio); // no reproduce ogg
            player.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        } finally {
            if (player != null)
                player.close();
        }
    }
}
