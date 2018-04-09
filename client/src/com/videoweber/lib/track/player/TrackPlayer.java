package com.videoweber.lib.track.player;

import com.videoweber.lib.track.player.model.TrackPlayerModel;
import com.videoweber.lib.common.Executor;
import com.videoweber.lib.engines.javacv.simplified_player.SimplifiedPlayerException;
import com.videoweber.lib.engines.javacv.simplified_player.SoundBufferPlayer;
import com.videoweber.lib.sampler.Sample;
import com.videoweber.lib.track.Track;
import com.videoweber.lib.track.player.model.TrackPlayerModelUpdate;
import com.videoweber.lib.track.player.model.UnmodifiableTrackPlayerModel;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class TrackPlayer {

    private static final Logger LOG = Logger.getLogger(TrackPlayer.class.getName());

    private final Object modelSyncLock = new Object();
    private final TrackPlayerModel model = new TrackPlayerModel();
    private final TrackPlayerModelUpdate modelUpdate = new TrackPlayerModelUpdate();
    private final Executor executor;
    private SignedBufferedImage image = null;
    private final Object imageSyncLock = new Object();
    private final SoundBufferPlayer soundBufferPlayer = new SoundBufferPlayer();
    private SimplifiedSamplePlayer simplifiedSamplePlayer = null;

    public TrackPlayer(Track track) {
        Objects.requireNonNull(track);
        executor = new Executor() {
            @Override
            public void run() throws Exception {
                try {
                    // Reduces synchronized blocks number. Not any change of model requires synchronization.
                    TrackPlayerModel tmpModel = new TrackPlayerModel();
                    // Provides work with immutable update object.
                    TrackPlayerModelUpdate tmpModelUpdate = new TrackPlayerModelUpdate();
                    while (!isStoping()) {
                        synchronized (modelSyncLock) {
                            model.updateWith(tmpModel);
                            tmpModelUpdate.updateWith(modelUpdate);
                            modelUpdate.reset();
                            if (!tmpModelUpdate.isChanged() && tmpModel.getState() != TrackPlayerState.PLAYING && !isStoping()) {
                                modelSyncLock.wait();
                            }
                        }

                        // Processing outer updates.
                        if (tmpModelUpdate.isChanged()) {
                            tmpModel.updateWith(tmpModelUpdate);

                            if (tmpModelUpdate.isPositionChanged() || tmpModelUpdate.isStateChanged()) {
                                // Stopping the player.
                                tmpModel.setUnreachedState(null);
                                setImage(null);
                                releaseSimplifiedPlayer();
                                soundBufferPlayer.stop();

                                if (tmpModel.getState() != TrackPlayerState.STOPPED) {
                                    // At least we need to get one image.
                                    Sample sample = track.getSample(new Date(Math.round(tmpModel.getPosition() / 1000.)));

                                    if (sample == null) {
                                        // No samples. Can't change the state.
                                        tmpModel.setUnreachedState(tmpModel.getState());
                                        tmpModel.setState(TrackPlayerState.STOPPED);
                                    } else {
                                        // Seeking for required place.
                                        simplifiedSamplePlayer = new SimplifiedSamplePlayer(sample, soundBufferPlayer);
                                        long sampleBeginInMicroseconds = sample.getBegin().getTime() * 1000;
                                        if (sampleBeginInMicroseconds < tmpModel.getPosition()) {
                                            simplifiedSamplePlayer.seek(tmpModel.getPosition() - sampleBeginInMicroseconds);
                                            tmpModel.setPosition(sampleBeginInMicroseconds + simplifiedSamplePlayer.getTimestamp());
                                        } else {
                                            tmpModel.setPosition(sampleBeginInMicroseconds);
                                        }

                                        setImage(simplifiedSamplePlayer.getCurrentImage());

                                        if (tmpModel.getState() != TrackPlayerState.PLAYING) {
                                            // We should to release sample player if we are not going to play.
                                            releaseSimplifiedPlayer();
                                        } else {
                                            // For playing we need sound channel.
                                            soundBufferPlayer.start();
                                            soundBufferPlayer.setVolume(tmpModel.getVolume());
                                        }
                                    }
                                }
                            } else {
                                if (tmpModelUpdate.isVolumeChanged() && soundBufferPlayer.isStarted()) {
                                    soundBufferPlayer.setVolume(tmpModel.getVolume());
                                }
                            }

                            tmpModelUpdate.reset();

                            synchronized (modelSyncLock) {
                                model.updateWith(tmpModel);
                            }
                        }

                        // Playing.
                        if (tmpModel.getState() == TrackPlayerState.PLAYING) {
                            if (simplifiedSamplePlayer.next()) {
                                tmpModel.setPosition(simplifiedSamplePlayer.getSample().getBegin().getTime() * 1000 + simplifiedSamplePlayer.getTimestamp());
                                setImage(simplifiedSamplePlayer.getCurrentImage());
                            } else {
                                // End of sample.

                                Sample lastSample = simplifiedSamplePlayer.getSample();

                                // Stop sample playing but sound channel isn't stopped yet.
                                releaseSimplifiedPlayer();

                                Sample sample = track.getSample(new Date(lastSample.getEnd().getTime() + 1));
                                if (sample == null) {
                                    tmpModel.setUnreachedState(tmpModel.getState());
                                    tmpModel.setState(TrackPlayerState.STOPPED);

                                    // No more samples. Stopping the sound channel.
                                    soundBufferPlayer.stop();
                                } else {
                                    // Starting new player if we have one more sample.
                                    simplifiedSamplePlayer = new SimplifiedSamplePlayer(sample, soundBufferPlayer);
                                    tmpModel.setPosition(sample.getBegin().getTime() * 1000);
                                    setImage(simplifiedSamplePlayer.getCurrentImage());
                                }
                            }
                        }
                    }
                } finally {
                    setImage(null);
                    releaseSimplifiedPlayer();
                    soundBufferPlayer.stop();
                    synchronized (modelSyncLock) {
                        model.setState(TrackPlayerState.STOPPED);
                        model.setUnreachedState(null);
                    }
                }
            }

            @Override
            public String getName() {
                return "track player";
            }

        };
        executor.getListeners().add(new Executor.Listener() {
            @Override
            public void onStoping(Executor executor) {
                synchronized (modelSyncLock) {
                    modelSyncLock.notify();
                }
            }

        });
    }

    public Executor getExecutor() {
        return executor;
    }

    public UnmodifiableTrackPlayerModel getModelCopy() {
        synchronized (modelSyncLock) {
            TrackPlayerModel modelCopy = new TrackPlayerModel();
            modelCopy.updateWith(model);
            return new UnmodifiableTrackPlayerModel(modelCopy);
        }
    }

    public void addModelUpdate(TrackPlayerModelUpdate modelUpdate) {
        synchronized (modelSyncLock) {
            this.modelUpdate.updateWith(modelUpdate);
            modelSyncLock.notify();
        }
    }

    public SignedBufferedImage getImageCopy(SignedBufferedImage buffer) {
        synchronized (imageSyncLock) {
            return SignedBufferedImage.copy(image, buffer);
        }
    }

    private void setImage(SignedBufferedImage image) {
        synchronized (imageSyncLock) {
            this.image = SignedBufferedImage.copy(image, this.image);
        }
    }

    private void releaseSimplifiedPlayer() {
        if (simplifiedSamplePlayer != null) {
            try {
                simplifiedSamplePlayer.stop();
            } catch (SimplifiedPlayerException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                simplifiedSamplePlayer = null;
            }
        }
    }

}
