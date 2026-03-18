package edu.upb.chatupb_v2.model.audio;

import edu.upb.chatupb_v2.model.audio.enums.AudioName;
import edu.upb.chatupb_v2.model.audio.enums.AudioState;
import edu.upb.chatupb_v2.model.audio.enums.DurationType;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.Getter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioManager {
    @Getter
    private List<Audio> audios;
    private static final ExecutorService soundPool = Executors.newCachedThreadPool();


    public AudioManager() {
        audios = new ArrayList<>();
//        audios.add(new Audio("04. Grasswalk.mp3", DurationType.ETERNAL));
        startThread();
    }

    public void addAudio(AudioName audioName) {
        System.out.println(audioName);
        switch (audioName) {
            case BUZZ -> audios.add(new Audio("sounds/nudge.mp3", DurationType.UNIQUE));
            default -> System.out.println("audio aun no implementado");
        }
    }

    private void startThread() {
        new Thread(() -> {
            while (true) {
                if (!audios.isEmpty())
                    for (int i = 0; i < audios.size(); i++) {
                        if (audios.get(i).getState() == AudioState.NEW) {
                            playSound(audios.get(i));
                        } else if (audios.get(i).getState() == AudioState.FINISHED) {
                            if (audios.get(i).getDurationType() == DurationType.ETERNAL) {
                                playSound(audios.get(i));
                            } else { // una sola reproduccion
                                audios.remove(i);
                                i--;
                            }
                        }
                    }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void playSound(Audio audio) {
        //System.out.println("playSound inicio.");
        soundPool.execute(() -> { // funciona de forma asincrona
            Player player = null;
            try {
                audio.setState(AudioState.IN_PROGRESS);
                InputStream isSound = this.getClass().getClassLoader().getResourceAsStream(audio.getName());
                //System.out.println("inputStream: " + isSound);


                if (isSound != null) player = new Player(isSound); // no reproduce ogg

                if (player != null) player.play();


            } catch (JavaLayerException e) {
                e.printStackTrace();
            } finally {
                if (player != null)
                    player.close();
                audio.setState(AudioState.FINISHED);
            }
        });
        //System.out.println("playSound fin.");
    }
}
