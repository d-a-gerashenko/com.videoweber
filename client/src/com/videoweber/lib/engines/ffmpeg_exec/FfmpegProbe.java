package com.videoweber.lib.engines.ffmpeg_exec;

import com.videoweber.lib.common.MediaType;
import com.videoweber.lib.cli.ContinuousRuntimeExec;
import com.videoweber.lib.sampler.Probe;
import java.io.File;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class FfmpegProbe extends Probe {

    protected FfmpegProbe(File madiaFile) {
        super(madiaFile);
    }

    @Override
    protected void initFileInfo() {
        ContinuousRuntimeExec continuousRuntimeExec = new ContinuousRuntimeExec(
                "\"" + FfmpegBins.ffprobeBin().getAbsolutePath() + "\""
                + " -v quiet -print_format json -show_format -show_streams "
                + " -i \"" + getMediaFile().getAbsolutePath() + "\""
        );
        try {
            continuousRuntimeExec.exec();
        } catch (IOException e) {
            throw new RuntimeException("ffprobe can't get info about \"" + getMediaFile().getAbsolutePath() + "\"", e);
        }
        continuousRuntimeExec.waitForInfinitely();
        if (!continuousRuntimeExec.getErrorReader().getLines().isEmpty()) {
            throw new RuntimeException("Erros during ffprobe, last 20 lines of error: " + System.lineSeparator() + continuousRuntimeExec.getErrorReader().getMessage(20));
        }

        JSONObject resultJSONObject;

        try {
            resultJSONObject = new JSONObject(continuousRuntimeExec.getOutputReader().getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Incorrect ffprobe result, last 20 lines: " + continuousRuntimeExec.getOutputReader().getMessage(20));
        }

        initDuration(resultJSONObject);
        initMediaType(resultJSONObject);
    }

    private void initDuration(JSONObject fileInfoJSONObject) {
        JSONArray streams = fileInfoJSONObject.getJSONArray("streams");

        for (int i = 0; i < streams.length(); i++) {
            JSONObject stream = streams.getJSONObject(i);
            if (stream.getString("codec_type").equalsIgnoreCase("audio")) {
                duration = (long) (stream.getDouble("duration") * 1000);
                break;
            }
        }

        if (duration == null) {
            duration = (long) (fileInfoJSONObject.getJSONObject("format").getDouble("duration") * 1000);
        }
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
