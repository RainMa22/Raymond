package me.rainma22;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleFFmpegInstance extends s16beProviderInstance {
    private static final List<String> OUTPARAM_FOR_FFMPEG =
            List.of("-f", "s16be",
                    "-codec:a", "pcm_s16be",
                    "-ac", "2",
                    "-ar", "48000",
                    "pipe:1");
    static String ffmpegPath = "ffmpeg";
    public SimpleFFmpegInstance(String inPath) throws IOException {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(ffmpegPath);
        cmds.addAll(List.of("-i", '"' + inPath + '"'));
        cmds.addAll(OUTPARAM_FOR_FFMPEG);

        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();
        inputStream = process.getInputStream();
    }

    public static void setFfmpegPath(String path) {
        ffmpegPath = path;
    }
}
