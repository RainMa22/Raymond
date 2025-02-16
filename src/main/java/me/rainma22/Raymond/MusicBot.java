package me.rainma22.Raymond;

import me.rainma22.Raymond.commands.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;


public class MusicBot extends ListenerAdapter {
    private Map<String, iCommand> supportedCommands = new HashMap<>();
    private Map<Guild, QueuedMusicHandler> handlerMap = new HashMap<>();

    public MusicBot() {
        super();
        supportedCommands.put("!play", new PlayCommand(handlerMap));
        supportedCommands.put("!skip", new SkipCommand(handlerMap));
        supportedCommands.put("!stop", new StopCommand(handlerMap));
        supportedCommands.put("!volume", new VolumeCommand(handlerMap));
        supportedCommands.put("!seek", new SeekCommand(handlerMap));
        supportedCommands.put("default", new iCommand() {});
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Make sure we only respond to events that occur in a guild
        if (!event.isFromGuild()) return;
        // This makes sure we only execute our code when someone sends a message with "!!play"
        // Now we want to exclude messages from bots since we want to avoid command loops in chat!
        // this will include own messages as well for bot accounts
        // if this is not a bot make sure to check if this message is sent by yourself!
        if (event.getAuthor().isBot()) return;
        String userContent = event.getMessage().getContentRaw();
        if (!userContent.startsWith("!")) return;
        String[] cmds = userContent.split(" ");


        supportedCommands.getOrDefault(cmds[0].toLowerCase(),
                supportedCommands.get("default")).accept(event, cmds);
    }
}