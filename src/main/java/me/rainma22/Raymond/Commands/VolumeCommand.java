package me.rainma22.Raymond.Commands;

import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class VolumeCommand implements iCommand{
    private final Map<Guild, QueuedMusicHandler> handlerMap;
    private final String USAGE_MSG = "Usage: !volume [0-200]";
    private final String VOLUME_SET_MSG = "Volume Set to %.2f%%";
    private final String BAD_COMMAND_ERR = "Bad Volume Command!";
    private final String VOLUME_OUT_OF_RANGE_ERR = "Desired Volume not in valid range!";
    private final String NO_MUSIC_PLAYING_ERR = "No Music is Playing!";

    public VolumeCommand(Map<Guild,QueuedMusicHandler> handlerMap){
        this.handlerMap = handlerMap;
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
        if (volume < 0 || volume > 200) {
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
        msgChannel.sendMessage(String.format(VOLUME_SET_MSG, volume)).queue();
    }

}
