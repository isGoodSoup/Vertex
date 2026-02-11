package org.chess.service;

public class TimerService {
    private boolean isActive;
    private int frames;
    private int seconds;
    private int minutes;

    public TimerService() {
        reset();
    }

    public void start() {
        isActive = true;
    }

    public void stop() {
        isActive = false;
    }

    public void reset() {
        isActive = false;
        frames = 0;
        seconds = 0;
        minutes = 0;
    }

    public void update() {
        if(!isActive) { return; }
        frames++;
        if(frames >= 60) {
            frames = 0;
            seconds++;
            if(seconds >= 60) {
                seconds = 0;
                minutes++;
            }
        }
    }

    public String getTimeString() {
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isActive() {
        return isActive;
    }

    public int getSeconds() {
        return seconds + minutes * 60;
    }

    public int getMinutes() {
        return minutes;
    }
}

