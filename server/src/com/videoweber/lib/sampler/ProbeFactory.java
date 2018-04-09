package com.videoweber.lib.sampler;

import java.io.File;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public interface ProbeFactory {
    public Probe createProbe(File mediFile);
}
