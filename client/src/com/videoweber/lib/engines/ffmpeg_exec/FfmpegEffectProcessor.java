package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.cli.ContinuousRuntimeExec;
import com.videoweber.lib.sampler.Effect;
import com.videoweber.lib.sampler.EffectProcessor;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.sampler.effects.Rotate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegEffectProcessor extends EffectProcessor {

    private final File tempDir;

    public FfmpegEffectProcessor(File tempDir) {
        if (tempDir == null) {
            throw new IllegalArgumentException();
        }
        if (!tempDir.exists()) {
            throw new RuntimeException("Temp dit is not exists.");
        }
        this.tempDir = tempDir;
    }

    @Override
    public Sample applyEffect(Sample sample, Effect effect) {
        ContinuousRuntimeExec continuousRuntimeExec;
        if (!effect.getMediaType().isCompatible(sample.getMediaType())) {
            throw new RuntimeException(
                    String.format("Sample media type is not campatible with effect media type %s.", effect.getClass())
            );
        }
        if (effect instanceof Rotate) {
            File tempFile = new File(
                    tempDir.getAbsolutePath()
                    + File.separator
                    + sample.getFile().getName()
            );
            String command = "\"" + FfmpegBins.ffmpegBin().getAbsolutePath() + "\""
                    + " -y "
                    + " -i \"" + sample.getFile() + "\" "
                    + " -preset ultrafast "
                    + " -vf \"";

            switch (((Rotate) effect).getAngel()) {
                case 90:
                    command += "transpose=1";
                    break;
                case 180:
                    command += "transpose=1,transpose=1";
                    break;
                case 270:
                    command += "transpose=2";
                    break;
            }

            command += "\" "
                    + "\"" + tempFile.getAbsolutePath() + "\"";
            continuousRuntimeExec = new ContinuousRuntimeExec(command);
            try {
                continuousRuntimeExec.exec();
            } catch (IOException ex) {
                throw new RuntimeException("Error on rotate sample.");
            }
            continuousRuntimeExec.waitForInfinitely();
            Logger.getLogger(this.getClass().getName()).log(Level.FINER, "Replase sample file with rotated file.");
            try {
                Files.move(tempFile.toPath(), sample.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Cant't replase sample file with rotated.", e);
            }
        } else {
            throw new RuntimeException("Unsupported effect: " + effect.getClass().getName());
        }
        return new Sample(
                sample.getSamplerInfo(),
                sample.getBegin(),
                sample.getExtension(),
                sample.getFile(),
                sample.getDudation(),
                (int)sample.getFile().length(),
                sample.getMediaType()
        );
    }

    @Override
    public boolean isSupportedEffect(Effect effect) {
        return effect instanceof Rotate;
    }

}
