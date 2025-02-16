package me.rainma22.Raymond;

import me.rainma22.Raymond.dataprovider.ffmpeginstance.FFmpegInstance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    private static String BOT_TOKEN;

    public static void main(String[] arguments) throws Exception {
        GlobalOptions globalOptions = GlobalOptions.getGlobalOptions();
//        File secret = new File("secret");
//        BOT_TOKEN = new String(new FileInputStream(secret).readAllBytes(), StandardCharsets.UTF_8);
        BOT_TOKEN = globalOptions.getAPIKey();
        if (BOT_TOKEN.isEmpty()){
            System.err.printf("\nAPI KEY not set! Please specify it in: %s!\n",
                    GlobalOptions.OPTION_PATH.toAbsolutePath());
            System.exit(-1);
        }
        //load settings
//        FFmpegInstance.setFfmpegPath("G:\\ffmpeg\\bin\\ffmpeg.exe");
        FFmpegInstance.setFfmpegPath(
                globalOptions.getFFmpegPath());

        globalOptions.save();

        JDA api = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new MusicBot())
                .build();
    }
}