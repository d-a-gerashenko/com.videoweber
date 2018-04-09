package com.videoweber.lib.track.player;

import com.videoweber.lib.common.RandomStringGenerator;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class SignedBufferedImage extends BufferedImage {

    private String typeSign;
    private String dataSign;

    public SignedBufferedImage(int width, int height, int imageType) {
        super(width, height, imageType);
        initSigns();
    }

    public SignedBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
        super(width, height, imageType, cm);
        initSigns();
    }

    public SignedBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        super(cm, raster, isRasterPremultiplied, properties);
        initSigns();
    }

    private void initSigns() {
        typeSign = RandomStringGenerator.generate();
        updateDataSign();
    }

    private void updateDataSign() {
        dataSign = RandomStringGenerator.generate();
    }

    public String getTypeSign() {
        return typeSign;
    }

    public String getDataSign() {
        return dataSign;
    }

    /**
     * Tries to copy in buffer if it possible. Otherwise returns new buffer.
     *
     * @param source
     * @param buffer
     * @return
     */
    public static SignedBufferedImage copy(BufferedImage source, SignedBufferedImage buffer) {
        if (source == null) {
            return null;
        }
        SignedBufferedImage copy;
        if (buffer == null
                || (source instanceof SignedBufferedImage) && !buffer.getTypeSign().equals(((SignedBufferedImage) source).getTypeSign())) {
            int type = source.getType();
            if (type == BufferedImage.TYPE_CUSTOM) {
                copy = new SignedBufferedImage(
                        source.getColorModel(),
                        source.copyData(null),
                        source.isAlphaPremultiplied(),
                        null
                );
            } else {
                copy = new SignedBufferedImage(source.getWidth(), source.getHeight(), source.getType());
                Graphics g = copy.getGraphics();
                g.drawImage(source, 0, 0, null);
                g.dispose();
            }
        } else {
            copy = buffer;
        }
        if (!(source instanceof SignedBufferedImage) || !copy.getDataSign().equals(((SignedBufferedImage) source).getTypeSign())) {
            Graphics g = copy.getGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
            if (source instanceof SignedBufferedImage) {
                copy.typeSign = ((SignedBufferedImage) source).getTypeSign();
                copy.dataSign = ((SignedBufferedImage) source).getDataSign();
            } else {
                copy.updateDataSign();
            }
        }
        return copy;
    }
}
