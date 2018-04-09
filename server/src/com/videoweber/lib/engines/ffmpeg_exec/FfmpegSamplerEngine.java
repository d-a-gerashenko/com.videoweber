package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.common.FileNameFunstions;
import com.videoweber.lib.channel.Channel;
import com.videoweber.lib.channel.Source;
import com.videoweber.lib.channel.sources.Rtsp;
import com.videoweber.lib.cli.ContinuousRuntimeExec;
import com.videoweber.lib.sampler.SamplerEngine;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegSamplerEngine extends SamplerEngine {

    private static final Logger LOG = Logger.getLogger(FfmpegSamplerEngine.class.getName());

    public static enum Mode {
        MINIMAL_LATANCY, MINIMAL_CPU;
    }

    private final Mode mode;
    /**
     * Sample duration in milliseconds for ffmpeg and sleep in lopp.
     */
    private final int SAMPLE_SIZE;
    private final int SAMPLE_WAITING_TIMEOUT;

    private static final String FFMPEG_FILE_FORMAT = "%Y-%m-%d_%H-%M-%S.mp4";
    private static final ThreadLocal<SimpleDateFormat> DATE_IN_FILE_NAME_FORMAT_HOLDER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        }

    };

    private final Map<File, Date> fileDatesCache = new HashMap<>();
    private ContinuousRuntimeExec continuousRuntimeExec;

    public FfmpegSamplerEngine(Channel channel, File tempDir, Mode mode) {
        super(channel, tempDir);
        Objects.requireNonNull(mode);

        if (mode == Mode.MINIMAL_CPU) {
            SAMPLE_SIZE = 30000;
            SAMPLE_WAITING_TIMEOUT = 120000;
        } else {
            SAMPLE_SIZE = 1000;
            SAMPLE_WAITING_TIMEOUT = 30000;
        }
        this.mode = mode;
    }

    private Date fileToDate(File file) {
        if (fileDatesCache.containsKey(file)) {
            return fileDatesCache.get(file);
        }
        try {
            Date fileDate = DATE_IN_FILE_NAME_FORMAT_HOLDER.get().parse(FileNameFunstions.withoutExtension(file.getName()));
            fileDatesCache.put(file, fileDate);
            return fileDate;
        } catch (ParseException | RuntimeException e) {
            throw new RuntimeException(
                    String.format("Unexpected error during parsing date from file name \"%s\".", file.getAbsolutePath()),
                    e
            );
        }
    }

    @Override
    public void run() {
        try {
            LOG.log(Level.FINER, "Attempt to start sampler engine \"{0}\".", getInfo());

            continuousRuntimeExec = new ContinuousRuntimeExec(generateFfmpegCommand());
            try {
                continuousRuntimeExec.exec();
            } catch (IOException ex) {
                throw new RuntimeException("Sampler engine isn't started because of errors during bin execution.", ex);
            }

            long lastStepTime = 0;
            long timeWithoutSample = 0;
            while (!isStoping()) {
                // Catching not responding source.
                if (lastStepTime != 0) {
                    timeWithoutSample += System.currentTimeMillis() - lastStepTime;
                    if (timeWithoutSample > SAMPLE_WAITING_TIMEOUT) {
                        throw new RuntimeException(String.format("There are no samples more than %d seconds.", SAMPLE_WAITING_TIMEOUT / 1000));
                    }
                }
                lastStepTime = System.currentTimeMillis();

                if (!continuousRuntimeExec.getErrorReader().getLines().isEmpty()) {
                    throw new RuntimeException("Errors during bin execution, last 20 lines of error output: " + System.lineSeparator() + continuousRuntimeExec.getErrorReader().getMessage(20));
                }
                LOG.log(Level.FINEST, "Attempt to rename ready samples.");
                File[] listOfFiles = getTempDir().listFiles((File file) -> !file.getName().startsWith("ready_"));
                if (listOfFiles.length != 0) {
                    // Sort ASC by creation time.
                    Arrays.sort(listOfFiles, (File o1, File o2) -> fileToDate(o1).compareTo(fileToDate(o2)));

                    for (File file : listOfFiles) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        Date created;
                        created = fileToDate(file);

                        String extension = FileNameFunstions.extension(file.getName());

                        File fileToImport = new File(file.getParentFile().getAbsolutePath() + File.separator + "ready_" + created.getTime() + "." + extension);

                        try {
                            Files.move(file.toPath(), fileToImport.toPath());
                        } catch (FileSystemException e) {
                            LOG.log(Level.FINEST, "File \"{0}\" is busy.", file.getName());
                        }
                        if (fileToImport.exists()) {
                            LOG.log(Level.FINEST, "File \"{0}\" renamed to \"{1}\"", new String[]{file.getName(), fileToImport.getAbsolutePath()});
                            timeWithoutSample = 0;
                            fileDatesCache.remove(file);

                            getRawSampleHandler().accept(fileToImport, created);
                        }
                    }
                }

                try {
                    /**
                     * SamplerEngine is an Executor so we already have a Thread.
                     * It's redundant to use Scheduler with one more Thread
                     * here.
                     */
                    sleep(SAMPLE_SIZE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Engine is stopped on error.", e);
        } finally {
            if (continuousRuntimeExec != null) {
                continuousRuntimeExec.stop();
                continuousRuntimeExec.waitForInfinitely();
            }
            fileDatesCache.clear();
            LOG.log(Level.FINER, "Engine is stopped.");
        }

    }

    private String generateFfmpegCommand() {
        boolean sameSource = false;

        if (getChannel().getVideoSource() != null && getChannel().getAudioSource() != null
                && getFfmpegUri(getChannel().getAudioSource()).equals(getFfmpegUri(getChannel().getVideoSource()))) {
            sameSource = true;
        }

        StringBuilder ffmpegCommandSB = new StringBuilder();

        ffmpegCommandSB
                .append("\"").append(FfmpegBins.ffmpegBin().getAbsolutePath()).append("\"")
                .append(" -loglevel fatal ");
//                    + " -max_delay 5000 ";
//                    + " -timeout 5 ";
        if (sameSource) {
            ffmpegCommandSB
                    .append(" -i ").append(getFfmpegUri(getChannel().getAudioSource()));
        } else if (getChannel().getVideoSource() != null) {
            ffmpegCommandSB
                    .append(" -i ").append(getFfmpegUri(getChannel().getVideoSource()));
            if (getChannel().getAudioSource() != null) {
                ffmpegCommandSB
                        .append(" -i ").append(getFfmpegUri(getChannel().getAudioSource()))
                        .append(" -map 1:a ");
            }
            ffmpegCommandSB.append(" -map 0:v ");
        } else {
            ffmpegCommandSB
                    .append(" -i ").append(getFfmpegUri(getChannel().getAudioSource()))
                    .append(" -map 0:a ");
        }

        ffmpegCommandSB.append(" -r 5 ");
        if (mode == Mode.MINIMAL_CPU) {
            /**
             * Makes bad image quality without CPU time benefits. Also one of
             * the cams show the only one frame.
             */
//                ffmpegCommand
//                    += " -preset ultrafast ";
        } else {
            ffmpegCommandSB
                    .append(" -b_strategy 0 ")
                    .append(" -x264-params keyint=15:no-scenecut=1 ");
        }
        ffmpegCommandSB
                .append(" -vcodec libx264 -acodec aac ");
        if (getChannel().getVideoSource() != null) {
            ffmpegCommandSB
                    //  If yuvj420p there may be problems on max.
                    //  .append( -pix_fmt yuv420p ")
                    // Fit to 640x480.
                    .append(" -filter_complex \"scale=iw*min(1\\,min(640/iw\\,480/ih)):-1\" ");
        }
        ffmpegCommandSB
                // Part name contains created time.
                .append(" -strftime 1 ")
                // Stream to files.
                .append(" -f segment  -segment_time ").append(SAMPLE_SIZE / 1000).append(" -segment_format mp4 ")
                .append("\"").append(getTempDir().getAbsolutePath()).append(File.separator).append(FFMPEG_FILE_FORMAT).append("\"");

        return ffmpegCommandSB.toString();
    }

    private String getFfmpegUri(Source source) {
        if (source instanceof Rtsp) {
            return ((Rtsp) source).getUri();
        }
        throw new RuntimeException("Unsupported source device.");
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getName() {
        return "ffmpeg-bin-external";
    }

}
