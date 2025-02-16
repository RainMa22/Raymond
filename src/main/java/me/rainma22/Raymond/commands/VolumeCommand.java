package me.rainma22.Raymond.commands;

import me.rainma22.Raymond.GuildOptions;
import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.Map;

public class VolumeCommand implements iCommand{
    private final Map<Guild, QueuedMusicHandler> handlerMap;
    private static final int VOLUME_MAX = 100;
    private static final String USAGE_MSG = String.format("Usage: !volume [0-%d]", VOLUME_MAX);
    private static final String VOLUME_SET_MSG = "Volume Set to %.2f%%";
    private static final String BAD_COMMAND_ERR = "Bad Volume Command!";
    private static final String VOLUME_OUT_OF_RANGE_ERR = "Desired Volume not in valid range!";
    private static final String NO_MUSIC_PLAYING_ERR = "No Music is Playing!";
    private GuildOptions guildOptions;

    public VolumeCommand(Map<Guild,QueuedMusicHandler> handlerMap){
        this.handlerMap = handlerMap;
        guildOptions = GuildOptions.getGuildOptions();
    }

    private void printUsage(TextChannel channel){
        channel.sendMessage(USAGE_MSG).queue();
    }

    @Override
    public void accept(MessageReceivedEvent event, String[] cmds) {
        MessageChannelUnion msgChannel = event.getChannel();
        if (cmds.length != 2) {
            msgChannel.sendMessage(BAD_COMMAND_ERR).queue();
            printUsage(msgChannel.asTextChannel());
            return;
        }
        float volume = Float.parseFloat(cmds[1]);
        if (volume < 0 || volume > VOLUME_MAX) {
            msgChannel.sendMessage(VOLUME_OUT_OF_RANGE_ERR).queue();
            printUsage(msgChannel.asTextChannel());
            return;
        }
        QueuedMusicHandler handler = handlerMap.getOrDefault(event.getGuild(),null);
        if (handler == null){
            msgChannel.sendMessage(NO_MUSIC_PLAYING_ERR).queue();
            return;
        }
        handler.setVolume(volume/100);
        guildOptions.setVolumeOf(event.getGuild(), volume/100);
        try {
            guildOptions.save();
        } catch (IOException ignored) {
            //...
        }

        msgChannel.sendMessage(String.format(VOLUME_SET_MSG, volume)).queue();
    }

}
