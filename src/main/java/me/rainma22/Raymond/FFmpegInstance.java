package me.rainma22.Raymond;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FFmpegInstance extends s16beProviderInstance {
    private static final List<String> OUTPARAM_FOR_FFMPEG =
            List.of("-f", "s16be",
                    "-codec:a", "pcm_s16be",
                    "-ac", "2",
                    "-ar", "48000",
                    "pipe:1");
    static String ffmpegPath = "ffmpeg";
    public FFmpegInstance(String inPath) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(ffmpegPath);
        cmds.addAll(List.of("-i",  inPath));
        cmds.addAll(OUTPARAM_FOR_FFMPEG);

        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        System.out.println(processBuilder.command());
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            Process process = processBuilder.start();
            inputStream = process.getInputStream();
        } catch (IOException exception){
            System.err.println("Encountered an Exception!");
            exception.printStackTrace(System.err);
            System.err.println("Exiting...");
            System.exit(-1);
        }
    }

    public static void setFfmpegPath(String path) {
        ffmpegPath = path;
    }
}
