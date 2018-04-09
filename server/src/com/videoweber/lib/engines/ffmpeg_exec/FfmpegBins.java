package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.common.ResourceManager;
import java.io.File;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public final class FfmpegBins {

    static {
        if (!ffmpegBin().exists()) {
            throw new RuntimeException("Ffmpeg bin file doesn't exist.");
        }
        if (!ffmpegBin().canExecute()) {
            throw new RuntimeException("Ffmpeg bin file is not executable.");
        }
        if (!ffprobeBin().exists()) {
            throw new RuntimeException("Ffmpeg bin file doesn't exist.");
        }
        if (!ffprobeBin().canExecute()) {
            throw new RuntimeException("Ffmpeg bin file is not executable.");
        }
        if (!ffplayBin().exists()) {
            throw new RuntimeException("Ffmpeg bin file doesn't exist.");
        }
        if (!ffplayBin().canExecute()) {
            throw new RuntimeException("Ffmpeg bin file is not executable.");
        }
    }


    public static File ffmpegBin() {
        return ResourceManager.getResourceFile("com/videoweber/lib/engines/ffmpeg_exec/ffmpeg.exe");
    }
    public static File ffprobeBin() {
        return ResourceManager.getResourceFile("com/videoweber/lib/engines/ffmpeg_exec/ffprobe.exe");

    }
    public static File ffplayBin() {
        return ResourceManager.getResourceFile("com/videoweber/lib/engines/ffmpeg_exec/ffplay.exe");

    }
}
