package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.cli.ContinuousRuntimeExec;
import com.videoweber.lib.recorder.triggers.SoundDetector;
import com.videoweber.lib.sampler.Sample;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegSoundDetector extends SoundDetector {
    private final File tempDir;

    public FfmpegSoundDetector(long durationBefore, long durationAfter, int threshold, File tempDir) {
        super(durationBefore, durationAfter, threshold);
        if (tempDir == null) {
            throw new IllegalArgumentException();
        }
        if (!tempDir.exists()) {
            throw new RuntimeException("Temp dit is not exists.");
        }
        this.tempDir = tempDir;
    }

    @Override
    protected boolean _check(Sample sample) {
        File tempFile = new File(
                tempDir.getAbsolutePath()
                + File.separator
                + FileNameFunstions.randomName()
                + ".wav"
        );

        String command
                = "\"" + FfmpegBins.ffmpegBin().getAbsolutePath() + "\""
                + " -i \"" + sample.getFile() + "\" "
                + " -preset ultrafast "
                + "\"" + tempFile.getAbsolutePath() + "\"";

        ContinuousRuntimeExec continuousRuntimeExec = new ContinuousRuntimeExec(command);
        try {
            continuousRuntimeExec.exec();
        } catch (IOException ex) {
            throw new RuntimeException("Error on export wav from sample.", ex);
        }
        continuousRuntimeExec.waitForInfinitely();

        boolean hasSound;

        if (!tempFile.exists()) {
            throw new RuntimeException(
                    String.format(
                            "Cant't export wav file for sample file \"%s\".",
                            sample.getFile().getAbsolutePath()
                    )
            );
        }

        try {
            hasSound = hasSound(tempFile);
        } catch (Exception e) {
            throw e;
        } finally {
            Logger.getLogger(this.getClass().getName()).log(Level.FINER, "Remove temp wav file.");
            try {
                Files.delete(tempFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Cant't remove temp wav file.", e);
            }
        }
        
        return hasSound;
    }

    private boolean hasSound(File file) {
        double[][] decibels = readAudioDecibels(file);
        for (double[] channelDecibels : decibels) {
            for (double decibel : channelDecibels) {
                if (decibel > getThreshold()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double[][] readAudioDecibels(File audioFile) {
        // Open audio file.
        AudioInputStream audioInputStream;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(
                            new FileInputStream(
                                    audioFile
                            )
                    )
            );
        } catch (UnsupportedAudioFileException ex) {
            throw new RuntimeException(
                    String.format("Unsupported audio file \"%s\".", audioFile.getAbsolutePath()),
                    ex
            );
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Can't read data from file \"%s\".", audioFile.getAbsolutePath()),
                    ex
            );
        }

        // Read audio bytes.
        AudioFormat format = audioInputStream.getFormat();

        long sizeInBytes = audioInputStream.getFrameLength() * format.getFrameSize();

        if (sizeInBytes < Integer.MIN_VALUE || sizeInBytes > Integer.MAX_VALUE) {
            throw new RuntimeException("Audio file is too big.");
        }

        byte[] audioBytes = new byte[(int) sizeInBytes];
        try {
            audioInputStream.read(audioBytes);
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Can't read data from file \"%s\".", audioFile.getAbsolutePath()),
                    ex
            );
        }

        try {
            audioInputStream.close();
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Can't close audio stream for file \"%s\".", audioFile.getAbsolutePath()),
                    ex
            );
        }

        // Decode samples.
        int[][] samples;
        switch (format.getSampleSizeInBits()) {
            case 16: {
                int sampleLengthInBytes = 2;
                int channelLengthInSamples = audioBytes.length / sampleLengthInBytes / format.getChannels();
                samples = new int[format.getChannels()][channelLengthInSamples];
                for (int channelNum = 0; channelNum < format.getChannels(); channelNum++) {
                    if (format.isBigEndian()) {
                        for (int sampleNum = 0; sampleNum < channelLengthInSamples; sampleNum++) {
                            int sampleStart = (sampleNum * format.getChannels() + channelNum) * sampleLengthInBytes;
                            /* First byte is MSB (high order) */
                            int MSB = (int) audioBytes[sampleStart];
                            /* Second byte is LSB (low order) */
                            int LSB = (int) audioBytes[sampleStart + 1];
                            samples[channelNum][sampleNum] = MSB << 8 | (255 & LSB);
                        }
                    } else {
                        for (int sampleNum = 0; sampleNum < channelLengthInSamples; sampleNum++) {
                            int sampleStart = (sampleNum * format.getChannels() + channelNum) * sampleLengthInBytes;
                            /* First byte is LSB (low order) */
                            int LSB = (int) audioBytes[sampleStart];
                            /* Second byte is MSB (high order) */
                            int MSB = (int) audioBytes[sampleStart + 1];
                            samples[channelNum][sampleNum] = MSB << 8 | (255 & LSB);
                        }
                    }
                }
                break;
            }
            case 8: {
                int sampleLengthInBytes = 1;
                int channelLengthInSamples = audioBytes.length / sampleLengthInBytes / format.getChannels();
                samples = new int[format.getChannels()][channelLengthInSamples];
                for (int channelNum = 0; channelNum < format.getChannels(); channelNum++) {
                    if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                        for (int sampleNum = 0; sampleNum < channelLengthInSamples; sampleNum++) {
                            int sampleStart = (sampleNum * format.getChannels() + channelNum) * sampleLengthInBytes;
                            samples[channelNum][sampleNum] = audioBytes[sampleStart];
                        }
                    } else {
                        for (int sampleNum = 0; sampleNum < channelLengthInSamples; sampleNum++) {
                            int sampleStart = (sampleNum * format.getChannels() + channelNum) * sampleLengthInBytes;
                            samples[channelNum][sampleNum] = audioBytes[sampleStart] - 128;
                        }
                    }
                }
                break;
            }
            default:
                throw new RuntimeException("Unsupported bit rate value of sample.");
        }

        double[][] decibels = new double[samples.length][samples[0].length];
        double peak = Math.pow(256, format.getSampleSizeInBits() / 8) / 2;
        for (int channelNum = 0; channelNum < samples.length; channelNum++) {
            for (int sampleNum = 0; sampleNum < samples[channelNum].length; sampleNum++) {
                if (samples[channelNum][sampleNum] == 0) {
                    decibels[channelNum][sampleNum] = Double.NEGATIVE_INFINITY;
                } else {
                    decibels[channelNum][sampleNum] = 20 * Math.log10(Math.abs(samples[channelNum][sampleNum]) / peak);
                }

//                // Decibels for chart.
//                int sign = (int) Math.signum(samples[channelNum][sampleNum]);
//                double maxValue = Math.abs(20 * Math.log10(1 / peak)) + 1;
//                if (decibels[channelNum][sampleNum] == Double.NEGATIVE_INFINITY) {
//                    decibels[channelNum][sampleNum] = 0;
//                } else {
//                    decibels[channelNum][sampleNum] = sign * (maxValue - Math.abs(decibels[channelNum][sampleNum]));
//                }
            }
        }
        return decibels;
    }

}
