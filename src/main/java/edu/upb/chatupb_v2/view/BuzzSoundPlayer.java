package edu.upb.chatupb_v2.view;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;

public class BuzzSoundPlayer {
    private volatile boolean playing;

    public void play() {
        if (playing) {
            return;
        }
        playing = true;
        Thread soundThread = new Thread(() -> {
            try {
                playToneSequence(new int[]{880, 740, 880}, new int[]{120, 90, 140});
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
            } finally {
                playing = false;
            }
        }, "buzz-sound");
        soundThread.setDaemon(true);
        soundThread.start();
    }

    private void playToneSequence(int[] frequencies, int[] durationsMs) throws Exception {
        AudioFormat format = new AudioFormat(44100f, 8, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            for (int i = 0; i < frequencies.length && i < durationsMs.length; i++) {
                writeTone(line, frequencies[i], durationsMs[i], format.getSampleRate());
                writeSilence(line, 25, format.getSampleRate());
            }
            line.drain();
        }
    }

    private void writeTone(SourceDataLine line, int frequency, int durationMs, float sampleRate) {
        int sampleCount = Math.max(1, (int) (durationMs * sampleRate / 1000f));
        byte[] buffer = new byte[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            double angle = 2.0 * Math.PI * i * frequency / sampleRate;
            buffer[i] = (byte) (Math.sin(angle) * 96);
        }
        line.write(buffer, 0, buffer.length);
    }

    private void writeSilence(SourceDataLine line, int durationMs, float sampleRate) {
        int sampleCount = Math.max(1, (int) (durationMs * sampleRate / 1000f));
        byte[] buffer = new byte[sampleCount];
        line.write(buffer, 0, buffer.length);
    }
}
