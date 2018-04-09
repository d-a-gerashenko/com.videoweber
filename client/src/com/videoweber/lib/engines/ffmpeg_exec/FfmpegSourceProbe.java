package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.channel.Source;
import com.videoweber.lib.channel.SourceProbe;
import com.videoweber.lib.channel.sources.Rtsp;
import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.cli.ContinuousRuntimeExec;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegSourceProbe extends SourceProbe {

    public FfmpegSourceProbe(Source source) {
        super(source);
    }

    @Override
    protected void initSourceInfo() {
        if (!(getSource() instanceof Rtsp)) {
            throw new RuntimeException(
                    String.format(
                            "Unsupproted class \"%s\".",
                            getSource().getClass().getName()
                    )
            );
        }
        Rtsp rtspSource = (Rtsp)getSource();
        ContinuousRuntimeExec continuousRuntimeExec = new ContinuousRuntimeExec(
                "\"" + FfmpegBins.ffprobeBin().getAbsolutePath() + "\""
                + " -v quiet -print_format json -show_format -show_streams "
                + " -i \"" + rtspSource.getUri() + "\""
        );

        try {
            continuousRuntimeExec.exec();
        } catch (IOException e) {
            throw new RuntimeException("ffprobe can't get info about \"" + rtspSource.getUri() + "\"", e);
        }

//        continuousRuntimeExec.waitForInfinitely();
        int timeoutSeconds = 10;
        try {
            Thread.sleep(timeoutSeconds * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (continuousRuntimeExec.isExecuting()) {
            continuousRuntimeExec.stopAndWaitForInfinitely();
            throw new RuntimeException(
                    String.format("Can't get source info for a long time (%s seconds).", timeoutSeconds)
            );
        }

        if (!continuousRuntimeExec.getErrorReader().getLines().isEmpty()) {
            throw new RuntimeException("Erros during ffprobe, last 20 lines of error: " + System.lineSeparator() + continuousRuntimeExec.getErrorReader().getMessage(20));
        }

        JSONObject resultJSONObject;

        try {
            resultJSONObject = new JSONObject(continuousRuntimeExec.getOutputReader().getMessage());
        } catch (RuntimeException e) {
            throw new RuntimeException("Incorrect ffprobe result, last 20 lines: " + continuousRuntimeExec.getOutputReader().getMessage(20));
        }

        initMediaType(resultJSONObject);
    }

    private void initMediaType(JSONObject fileInfoJSONObject) {
        JSONArray streams = fileInfoJSONObject.getJSONArray("streams");

        loop:
        for (int i = 0; i < streams.length(); i++) {
            JSONObject stream = streams.getJSONObject(i);
            switch (stream.getString("codec_type")) {
                case "video":
                    if (this.mediaType == MediaType.AUDIO) {
                        this.mediaType = MediaType.VIDEO_AND_AUDIO;
                        break loop;
                    }
                    this.mediaType = MediaType.VIDEO;
                    break;
                case "audio":
                    if (this.mediaType == MediaType.VIDEO) {
                        this.mediaType = MediaType.VIDEO_AND_AUDIO;
                        break loop;
                    }
                    this.mediaType = MediaType.AUDIO;
                    break;
            }
        }
    }

}
