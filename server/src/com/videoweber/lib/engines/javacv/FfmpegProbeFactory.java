package com.videoweber.lib.engines.javacv;

import com.videoweber.lib.sampler.Probe;
import com.videoweber.lib.sampler.ProbeFactory;
import java.io.File;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegProbeFactory implements ProbeFactory{

    @Override
    public Probe createProbe(File mediFile) {
        return new FfmpegProbe(mediFile);
    }
    
}
