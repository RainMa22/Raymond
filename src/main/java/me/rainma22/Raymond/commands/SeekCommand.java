package me.rainma22.Raymond.commands;

import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;

public class SeekCommand implements iCommand {
    private Map<Guild, QueuedMusicHandler> handlerMap;

    public SeekCommand(Map<Guild, QueuedMusicHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void accept(MessageReceivedEvent event, String[] cmds) {
        if (cmds.length < 2) {
            event.getMessage().reply("Please Enter a second to seek to!\n" +
                    "Usage: !seek [second]").queue();
        }
        QueuedMusicHandler handler = handlerMap.get(event.getGuild());
        if (handler == null){
            event.getMessage().reply("No Music is Playing!").queue();
            return;
        }
        float seekTo;
        try {
            seekTo = Float.parseFloat(cmds[1]);
            if(seekTo < 0) throw new NumberFormatException();
        } catch (NumberFormatException formatException) {
            event.getMessage().reply("Invalid Second: " + cmds[1] + "!").queue();
            return;
        }
        event.getMessage().reply(String.format("Seeking to %.02f Seconds.", seekTo)).queue();
        handlerMap.get(event.getGuild()).seekCurrentMusic(seekTo);
    }
}
