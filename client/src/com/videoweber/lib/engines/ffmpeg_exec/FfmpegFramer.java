package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.cli.ContinuousRuntimeExec;
import com.videoweber.lib.framer.Frame;
import com.videoweber.lib.framer.Framer;
import com.videoweber.lib.sampler.Sample;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegFramer extends Framer {

    public FfmpegFramer(File tempDir) {
        super(tempDir);
    }

    @Override
    protected Frame _cutFrame(Sample sample) {
        File frameImage = new File(
                getTempDir().getAbsolutePath()
                + File.separator
                + FileNameFunstions.randomName()
                + ".jpg"
        );

        String command
                = "\"" + FfmpegBins.ffmpegBin().getAbsolutePath() + "\""
                + " -i \"" + sample.getFile() + "\" "
                + " -preset ultrafast "
                + " -vframes 1 "
                /**
                 * For JPEG output use -q:v to control output quality. Full
                 * range is a linear scale of 1-31 where a lower value results
                 * in a higher quality. 2-5 is a good range to try.
                 */
                + " -q:v 2 "
                + "\"" + frameImage.getAbsolutePath() + "\"";

        ContinuousRuntimeExec continuousRuntimeExec = new ContinuousRuntimeExec(command);
        try {
            continuousRuntimeExec.exec();
        } catch (IOException ex) {
            throw new RuntimeException("Error on export jpg from sample.", ex);
        }
        continuousRuntimeExec.waitForInfinitely();

        if (!frameImage.exists()) {
            throw new RuntimeException("Image file not found.");
        }

        return new Frame(sample.getBegin(), frameImage);
    }

}
