package com.videoweber.lib.track.player;

import com.videoweber.lib.common.ResourceManager;
import com.videoweber.lib.track.player.model.ReadableTrackPlayerModelInterface;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TrackPlayerImageTransformer {

    public static final BufferedImage IMAGE_FOR_STOPPED;
    public static final BufferedImage IMAGE_FOR_AUDIO;

    static {
        try {
            IMAGE_FOR_STOPPED = ImageIO.read(ResourceManager.getResourceFile("com/videoweber/lib/player/logo.png"));
            IMAGE_FOR_AUDIO = ImageIO.read(ResourceManager.getResourceFile("com/videoweber/lib/player/audio_only.jpg"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static BufferedImage transform(BufferedImage image, ReadableTrackPlayerModelInterface model) {
        Objects.requireNonNull(model);
        if (image == null) {
            if (model.getState() == TrackPlayerState.STOPPED) {
                return IMAGE_FOR_STOPPED;
            } else {
                return IMAGE_FOR_AUDIO;
            }
        }
        return image;
    }
}
