package me.rainma22.Raymond.commands;

import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.net.URL;
import java.util.Map;

public class SkipCommand implements iCommand{
    private Map<Guild,QueuedMusicHandler> handlerMap;

    public SkipCommand(Map<Guild, QueuedMusicHandler> handlerMap){
        this.handlerMap = handlerMap;
    }
    @Override
    public void accept(MessageReceivedEvent event, String[] cmds) {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();

        QueuedMusicHandler handler = handlerMap.getOrDefault(guild,null);
        if (handler == null){
            channel.sendMessage("No Song Playing!").queue();
            return;
        }
        URL nextSong = handler.loadNextSong();
        if (nextSong == null){
            channel.sendMessage("No More Songs in Queue!").queue();
        }else {
            channel.sendMessage("Song Skipped! Now Playing: " + nextSong).queue();
        }
    }

    @Override
    public String getDescription(String separator) {
        return "Skips the current song.";
    }
}
