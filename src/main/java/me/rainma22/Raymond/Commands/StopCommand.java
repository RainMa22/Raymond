package me.rainma22.Raymond.Commands;

import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class StopCommand implements iCommand{
    private final Map<Guild, QueuedMusicHandler> handlerMap;

    public StopCommand(Map<Guild,QueuedMusicHandler> handlerMap){
        this.handlerMap = handlerMap;
    }

    @Override
    public void accept(MessageReceivedEvent event, String[] commands) {
        QueuedMusicHandler handler = handlerMap.getOrDefault(event.getGuild(),null);
        MessageChannel channel = event.getChannel();
        if (handler == null){
            channel.sendMessage("No Music is Playing!").queue();
            return;
        }
        handler.stop();
        handlerMap.remove(event.getGuild());
    }
}
