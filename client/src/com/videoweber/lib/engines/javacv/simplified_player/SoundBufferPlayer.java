package com.videoweber.lib.engines.javacv.simplified_player;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SoundBufferPlayer {

    private SourceDataLine soundLine = null;
    private ExecutorService executor = null;
    private boolean started = false;

    public void start() throws LineUnavailableException {
        stop();
        AudioFormat audioFormat = new AudioFormat(8000, 16, 1, true, true);
        //AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        soundLine = (SourceDataLine) AudioSystem.getLine(info);
        soundLine.open(audioFormat);
        soundLine.start();
        executor = Executors.newSingleThreadExecutor((target) -> {
            Thread thread = new Thread(target);
            thread.setName(thread.getName() + " / SoundBufferPlayer");
            return thread;
        });
        started = true;
    }

    public void play(byte[] buffer) throws LineUnavailableException, ExecutionException, InterruptedException {
        if (!started) {
            start();
        }
        /**
         * soundLine.write ignores interruptions during writing.
         */
        executor.submit(() -> {
            soundLine.write(buffer, 0, buffer.length);
        }).get();
    }

    public void stop() {
        started = false;
        if (Objects.nonNull(soundLine)) {
            soundLine.stop();
            soundLine = null;
        }
        if (Objects.nonNull(executor)) {
            executor.shutdown();
            executor = null;
        }
    }

    /**
     * @param level Number between 0 and 1 (loudest).
     * @throws javax.sound.sampled.LineUnavailableException
     */
    public void setVolume(float level) throws LineUnavailableException {
        if (!started) {
            start();
        }
        if (level < 0 || level > 1) {
            throw new IllegalArgumentException();
        }
        float dB = (float) (Math.log(level) / Math.log(10.0) * 20.0);
        FloatControl control = (FloatControl) soundLine.getControl(FloatControl.Type.MASTER_GAIN);
        control.setValue(dB);
        BooleanControl muteControl = (BooleanControl) soundLine.getControl(BooleanControl.Type.MUTE);
        muteControl.setValue(true);
        muteControl.setValue(false);
    }

    /**
     * @return Number between 0 and 1 (loudest).
     * @throws javax.sound.sampled.LineUnavailableException
     */
    public float getVolume() throws LineUnavailableException {
        if (!started) {
            start();
        }
        FloatControl control = (FloatControl) soundLine.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = control.getValue();
        float level = (float) Math.pow(Math.E, (dB * Math.log(10)) / 20);
        return level;
    }

    public boolean isStarted() {
        return started;
    }

}
