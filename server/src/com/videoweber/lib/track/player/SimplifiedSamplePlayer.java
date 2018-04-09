package com.videoweber.lib.track.player;

import com.videoweber.lib.engines.javacv.simplified_player.SimplifiedPlayer;
import com.videoweber.lib.engines.javacv.simplified_player.SimplifiedPlayerException;
import com.videoweber.lib.engines.javacv.simplified_player.SoundBufferPlayer;
import com.videoweber.lib.sampler.Sample;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SimplifiedSamplePlayer extends SimplifiedPlayer {

    private final Sample sample;

    public SimplifiedSamplePlayer(Sample sample, SoundBufferPlayer soundBufferPlayer) throws SimplifiedPlayerException {
        super(sample.getFile().getAbsolutePath(), soundBufferPlayer);
        this.sample = sample;
    }

    public Sample getSample() {
        return sample;
    }

}
