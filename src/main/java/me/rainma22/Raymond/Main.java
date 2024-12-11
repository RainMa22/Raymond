package me.rainma22.Raymond;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    private static String BOT_TOKEN;
    public static void main(String[] arguments) throws Exception
    {
        File secret = new File("secret");
        BOT_TOKEN = new String(new FileInputStream(secret).readAllBytes(), StandardCharsets.UTF_8);
        //load settings
//        FFmpegInstance.setFfmpegPath("G:\\ffmpeg\\bin\\ffmpeg.exe");

        JDA api = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new MusicBot())
                .build();
    }}