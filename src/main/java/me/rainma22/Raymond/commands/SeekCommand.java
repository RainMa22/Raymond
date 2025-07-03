package me.rainma22.Raymond.commands;

import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

public class SeekCommand implements iCommand {
    private Map<Guild, QueuedMusicHandler> handlerMap;
    private static final String USAGE_STRING = "Usage:`!seek [[hour]H][[minute]M][[second][S]]`";
    private static final Set<String> VALID_SUFFIXES = Set.of("H","M","S");
    public SeekCommand(Map<Guild, QueuedMusicHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void accept(MessageReceivedEvent event, String[] cmds) {
        if (cmds.length < 2) {
            event.getMessage().reply(String.join("\n",
                    "Please Enter a timestamp to seek to!",
                    USAGE_STRING)).queue();
        }
        QueuedMusicHandler handler = handlerMap.get(event.getGuild());
        if (handler == null){
            event.getMessage().reply("No Music is Playing!").queue();
            return;
        }
        float seekTo;
        String timestampString = cmds[1].toUpperCase();
        try {
            if(!VALID_SUFFIXES.contains(timestampString.substring(timestampString.length()-1)))
                timestampString += "S";
            seekTo = (float) Duration.parse("PT" + timestampString).getSeconds();
            if(seekTo < 0) throw new NumberFormatException();
        } catch (NumberFormatException formatException) {
            event.getMessage().reply("Invalid Timestamp: " + cmds[1] + "!").queue();
            return;
        } catch (DateTimeParseException dtpe){
            event.getMessage().reply("Unable to parse duration String: " + timestampString).queue();
            return;
        }
        event.getMessage().reply(String.format("Seeking to %s(%.02f Seconds).", cmds[1],seekTo)).queue();
        handlerMap.get(event.getGuild()).seekCurrentMusic(seekTo);
    }

    @Override
    public String getDescription(String separator) {
        return String.join(separator, "Seek to a certain second count.",
                USAGE_STRING);
    }
}
