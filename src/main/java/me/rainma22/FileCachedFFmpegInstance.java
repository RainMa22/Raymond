package me.rainma22;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FileCachedFFmpegInstance extends s16beProviderInstance {


    private ProcessBuilder processBuilder;
    private Process process;

//    private FileInputStream inputStream;
    private static final List<String> OUTPARAM_FOR_FFMPEG =
            List.of("-f", "s16be",
                    "-codec:a" , "pcm_s16be",
                    "-ac", "2",
                    "-ar", "48000",
                    "-y",
                    "temp.raw");

    public FileCachedFFmpegInstance(String inPath) throws IOException {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add(FFmpegInstance.ffmpegPath);
        cmds.addAll(List.of("-i", '"'+inPath+'"'));
        cmds.addAll(OUTPARAM_FOR_FFMPEG);

        processBuilder = new ProcessBuilder(cmds);
        System.out.println(processBuilder.command());
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        process = processBuilder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        inputStream = new FileInputStream("temp.raw");
    }

    public byte[] provide(int length) throws IOException {
        return inputStream.readNBytes(length);
    }

}
