package me.rainma22.Raymond;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


public class MusicBot extends ListenerAdapter {
    private static final BiConsumer<MessageReceivedEvent, String[]> NO_SUCH_COMMAND = (messageReceivedEvent, commands) ->
            messageReceivedEvent.getChannel().sendMessage("No Such Command: " + commands[0]).queue();
    private Map<String, BiConsumer<MessageReceivedEvent, String[]>> supportedCommands = new HashMap<>();
    private Map<Guild, QueuedMusicHandler> handlerMap = new HashMap<>();

    public MusicBot() {
        super();
        supportedCommands.put("!play", (event, cmds) -> {
            MessageChannelUnion msgChannel = event.getChannel();
            if (cmds.length < 2) {
                msgChannel.sendMessage("No URL found!\n" +
                        "Usage: !!play [Youtube URL]").queue();
                return;
            }
            URL url;
            try {
                url = new URL(cmds[1]);
            } catch (MalformedURLException e) {
                msgChannel.asTextChannel().sendMessage("bad Url: " +
                        cmds[1]).queue();
                return;
            }

            Guild guild = event.getGuild();
            try {
                QueuedMusicHandler handler = handlerMap.getOrDefault(guild, null);
                if (handler == null) {
                    AudioManager manager = guild.getAudioManager();
                    VoiceChannel vc = null;
                    for (Channel channel : guild.getChannels()) {
                        if (!(channel instanceof AudioChannel)) continue;
                        VoiceChannel channel1 = (VoiceChannel) channel;
                        if (channel1.getMembers().contains(event.getMember())) {
                            vc = channel1;
                            break;
                        }
                    }
                    if (vc == null) {
                        msgChannel.sendMessage(event.getAuthor().getAsMention() + ": Please be in a VC to do this!")
                                .queue();
                        return;
                    }
                    handler = new QueuedMusicHandler(manager, vc);
                }
                handlerMap.put(guild, handler);
                int position = handler.queueURL(url);
                if (position == 0){
                    msgChannel.sendMessage("Song Loaded: Playing Now!").queue();
                } else {
                    msgChannel.sendMessage("Song Loaded: Added to Queue, position #"+
                            position).queue();
                }
            } catch (ExtractionException e) {
                e.printStackTrace();
                msgChannel.sendMessage("ERROR! Extraction failed").queue();
                return;

            } catch (IOException e) {
                e.printStackTrace();
                msgChannel.sendMessage("ERROR! Unknown Error\n" + StringUtils.join(e.getStackTrace(), "\n"))
                        .queue();
                return;
            }
        });
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
        String[] cmds = userContent.split(" ");

        supportedCommands.getOrDefault(cmds[0], NO_SUCH_COMMAND).accept(event, cmds);
    }
}