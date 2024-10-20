package me.rainma22;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.util.Scanner;

public class Main {
    private static String BOT_TOKEN;
    public static void main(String[] arguments) throws Exception
    {
        BOT_TOKEN = new Scanner(new File("secret")).nextLine();
        //load settings
        SimpleFFmpegInstance.setFfmpegPath("G:\\ffmpeg\\bin\\ffmpeg.exe");

        JDA api = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new MusicBot())
                .build();



    }}