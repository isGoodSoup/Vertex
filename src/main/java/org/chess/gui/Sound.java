package org.chess.gui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;

public class Sound {
    private final transient Clip[] clips = new Clip[30];
    private final transient URL[] soundURL = new URL[30];
    private final transient FloatControl[] controls = new FloatControl[30];
    private int volumeScale = 3;
    private float volume;

    public Sound() {
        setSound(0, "piece-fx");
        setSound(1, "menu");
        setSound(2, "menu-select_1");
        setSound(3, "menu-select_2");
        setSound(4, "pages");
        setSound(5, "reveal");
        setSound(6, "checkmate");
        preload();
    }

    public Clip[] getClips() {
        return clips;
    }

    public URL[] getSoundURL() {
        return soundURL;
    }

    public FloatControl[] getControls() {
        return controls;
    }

    public int getVolumeScale() {
        return volumeScale;
    }

    public float getVolume() {
        return volume;
    }

    private void setSound(int i, String name) {
        String path = "/fx/";
        soundURL[i] = getClass().getResource(path + name + ".wav");
    }

    private void preload() {
        for (int i = 0; i < soundURL.length; i++) {
            try {
                if (soundURL[i] == null) continue;
                AudioInputStream ais = AudioSystem.
                        getAudioInputStream(soundURL[i]);
                Clip c = AudioSystem.getClip();
                c.open(ais);
                clips[i] = c;
                controls[i] = (FloatControl)c
                        .getControl(FloatControl.Type.MASTER_GAIN);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void play(int i) {
        Clip c = clips[i];
        if (c == null) return;
        if (c.isRunning()) c.stop();
        c.setFramePosition(0);
        c.start();
    }

    public void loop(int i) {
        Clip c = clips[i];
        if (c == null) return;
        c.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(int i) {
        Clip c = clips[i];
        if (c != null && c.isRunning()) c.stop();
    }

    public void checkVolume() {
        switch(volumeScale) {
            case 0 -> volume = -80f;
            case 1 -> volume = -20f;
            case 2 -> volume = -12f;
            case 3 -> volume = -5f;
            case 4 -> volume = 1f;
            case 5 -> volume = 6f;
        }

        for (FloatControl c : controls) {
            if (c != null) c.setValue(volume);
        }
    }

    public void setVolumeScale(int volumeScale) {
        if (volumeScale < 0) volumeScale = 0;
        if (volumeScale > 5) volumeScale = 5;
        this.volumeScale = volumeScale;
        checkVolume();
    }

    public void playFX(int i) {
        play(i);
        stop(i);
    }
}
