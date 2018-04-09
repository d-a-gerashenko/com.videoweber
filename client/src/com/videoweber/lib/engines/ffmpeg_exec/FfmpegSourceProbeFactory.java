package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.channel.Source;
import com.videoweber.lib.channel.SourceProbe;
import com.videoweber.lib.channel.SourceProbeFactory;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegSourceProbeFactory implements SourceProbeFactory {

    @Override
    public SourceProbe createProbe(Source source) {
        return new FfmpegSourceProbe(source);
    }

}
