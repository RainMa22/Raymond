package me.rainma22.Raymond.Commands;

import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class VolumeCommand implements iCommand{
    private final Map<Guild, QueuedMusicHandler> handlerMap;

    public VolumeCommand(Map<Guild,QueuedMusicHandler> handlerMap){
        this.handlerMap = handlerMap;
    }

    private void printUsage(TextChannel channel){
        channel.sendMessage( "Usage: !volume [0-200]").queue();
    }

    @Override
    public void accept(MessageReceivedEvent event, String[] cmds) {
        MessageChannelUnion msgChannel = event.getChannel();
        if (cmds.length != 2) {
            msgChannel.sendMessage("Please Specify a Volume!").queue();
            printUsage(msgChannel.asTextChannel());
            return;
        }
        float volume = Float.parseFloat(cmds[1]);
        if (volume < 0 || volume > 200) {
            msgChannel.sendMessage("desired Volume not in valid range!");
            printUsage(msgChannel.asTextChannel());
            return;
        }
        QueuedMusicHandler handler = handlerMap.getOrDefault(event.getGuild(),null);
        if (handler == null){
            msgChannel.sendMessage("No Music is Playing!").queue();
            return;
        }
        handler.setVolume(volume/100);
        msgChannel.sendMessage(String.format("Volume Set to %f", volume)).queue();
    }

}
