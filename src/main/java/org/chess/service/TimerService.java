package org.chess.service;

import org.chess.enums.Time;

public class TimerService {
    private boolean isActive;
    private long lastTime;
    private long timeNanos;
    private static Time mode;
    private static final long TWO_MINUTES = 120L * 1_000_000_000L;

    public TimerService() {
        reset();
    }

    public static void setTime(Time time) {
        mode = time;
    }

    public void start() {
        if(!isActive) {
            isActive = true;
            lastTime = System.nanoTime();
        }
    }

    public void stop() {
        isActive = false;
    }

    public void reset() {
        isActive = false;

        if(mode == Time.TIMER) {
            timeNanos = TWO_MINUTES;
        } else {
            timeNanos = 0;
        }
    }

    public void update() {
        if(!isActive) { return; }

        long now = System.nanoTime();
        long delta = now - lastTime;
        lastTime = now;

        if(mode == Time.TIMER) {
            timeNanos -= delta;
            if(timeNanos < 0) {
                timeNanos = 0;
                isActive = false;
            }
        } else {
            timeNanos += delta;
        }
    }

    public String getTimeString() {
        long millis = (timeNanos / 1_000_000L) % 1000;
        long totalSeconds = timeNanos/1_000_000_000L;
        long seconds = totalSeconds % 60;
        long minutes = totalSeconds/60;
        return String.format("%02d:%02d:%03d", minutes, seconds, millis);
    }

    public boolean isActive() {
        return isActive;
    }
}