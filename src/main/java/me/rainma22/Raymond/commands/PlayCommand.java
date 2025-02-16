package me.rainma22.Raymond.commands;

import me.rainma22.Raymond.GuildOptions;
import me.rainma22.Raymond.QueuedMusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class PlayCommand implements iCommand {
    private final Map<Guild, QueuedMusicHandler> handlerMap;
    private final GuildOptions guildOptions;

    public PlayCommand(Map<Guild, QueuedMusicHandler> handlerMap) {
        this.handlerMap = handlerMap;
        guildOptions = GuildOptions.getGuildOptions();
    }

    @Override
    public void accept(MessageReceivedEvent event, String[] cmds) {
        MessageChannelUnion msgChannel = event.getChannel();
        if (cmds.length < 2) {
            msgChannel.sendMessage("No URL found!\n" +
                    "Usage: !play [Youtube URL]").queue();
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
                handler = new QueuedMusicHandler(manager, vc, msgChannel.asTextChannel());
                handler.setVolume(guildOptions.getVolumeOf(guild));
            }
            handlerMap.put(guild, handler);
            int position = handler.queueURL(url);
            if (position == 0) {
                msgChannel.sendMessage("Song Loaded: Playing Now!").queue();
            } else {
                msgChannel.sendMessage("Song Loaded: Added to Queue, position #" +
                        position).queue();
            }
        } catch (ExtractionException e) {
            e.printStackTrace();
            msgChannel.sendMessage("ERROR! Extraction failed\n").queue();
//            Arrays.stream(e.getStackTrace()).sequential().forEach((elem) -> {
//                msgChannel.sendMessage(elem.toString()).queue();
//            });
            handlerMap.get(guild).loadNextSong();
            return;

        } catch (IOException e) {
            e.printStackTrace();
            msgChannel.sendMessage("ERROR! Unknown Error\n").queue();
//            Arrays.stream(e.getStackTrace()).sequential().forEach((elem) -> {
//                msgChannel.sendMessage(elem.toString()).queue();
//            });
            return;
        }

    }
}
