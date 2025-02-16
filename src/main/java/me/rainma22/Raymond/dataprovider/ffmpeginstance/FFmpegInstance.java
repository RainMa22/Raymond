package me.rainma22.Raymond.dataprovider.ffmpeginstance;

import me.rainma22.Raymond.dataprovider.s16beProviderInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FFmpegInstance extends s16beProviderInstance {
    private Process ffmpegProcess = null;
    private static final List<String> OUTPARAM_FOR_FFMPEG =
            List.of("-f", "s16be",
                    "-codec:a", "pcm_s16be",
                    "-ac", "2",
                    "-ar", "48000",
                    "-xerror",
                    "pipe:1");
    private static String SKIP_SEC = "-ss";
    private String inPath;
    protected static String ffmpegPath = "ffmpeg";
    public FFmpegInstance(String inPath) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(ffmpegPath);
        this.inPath = inPath;
        cmds.addAll(List.of("-i",  inPath));
        cmds.addAll(OUTPARAM_FOR_FFMPEG);

        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
//        System.out.println(processBuilder.command());
//        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            ffmpegProcess = processBuilder.start();
            inputStream = ffmpegProcess.getInputStream();
        } catch (IOException exception){
            System.err.println("Encountered an Exception!");
            exception.printStackTrace(System.err);
            System.err.println("Exiting...");
            System.exit(-1);
        }
    }

    public void seek(float second){
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(ffmpegPath);
        cmds.addAll(List.of("-i",  inPath));
        cmds.addAll(List.of(SKIP_SEC, Float.toString(second)));
        cmds.addAll(OUTPARAM_FOR_FFMPEG);
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
//        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        try {
            ffmpegProcess.destroy();
            ffmpegProcess = processBuilder.start();
            inputStream = ffmpegProcess.getInputStream();
        } catch (IOException exception){
            System.err.println("Encountered an Exception!");
            exception.printStackTrace(System.err);
            System.err.println("Exiting...");
            System.exit(-1);
        }
    }

    public boolean isInterrupted(){
        try{
            return !ffmpegProcess.isAlive() && ffmpegProcess.exitValue() != 0;
        } catch (IllegalThreadStateException exception){
            return false;
        }
    }



    @Override
    public void cleanup(){
        ffmpegProcess.destroy();
    }

    public static void setFfmpegPath(String path) {
        ffmpegPath = path;
    }
}
