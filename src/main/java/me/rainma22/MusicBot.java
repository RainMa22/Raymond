package me.rainma22;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MusicBot extends ListenerAdapter
{
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        // Make sure we only respond to events that occur in a guild
        if (!event.isFromGuild()) return;
        // This makes sure we only execute our code when someone sends a message with "!!play"
        // Now we want to exclude messages from bots since we want to avoid command loops in chat!
        // this will include own messages as well for bot accounts
        // if this is not a bot make sure to check if this message is sent by yourself!
        if (event.getAuthor().isBot()) return;
        String userContent = event.getMessage().getContentRaw();
        String[] cmds = userContent.split(" ");
        if (!cmds[0].equalsIgnoreCase("!play")) return;
        if (cmds.length < 2) {
            event.getChannel().sendMessage("No URL found!\n" +
                    "Usage: !!play [Youtube URL]").queue();
            return;
        }
        URL url;
        try {
            url = new URL(cmds[1]);
        } catch (MalformedURLException e) {
            event.getChannel().asTextChannel().sendMessage("bad Url: " +
                    cmds[1]).queue();
            return;
        }

        Guild guild = event.getGuild();
        // This will get the first voice channel with the name "music"
        // matching by voiceChannel.getName().equalsIgnoreCase("music")
//        VoiceChannel channel = guild.getVoiceChannelsByName("music", true).get(0);
        VoiceChannel vc = null;
        for (Channel channel : guild.getChannels()){
            if (!(channel instanceof AudioChannel)) continue;
            VoiceChannel channel1 = (VoiceChannel) channel;
            if (channel1.getMembers().contains(event.getMember())){
                vc = channel1;
                break;
            };
        }
        if (vc == null){
            event.getChannel().sendMessage(event.getAuthor().getAsMention() +": Please be in a VC to do this!")
                    .queue();
            return;
        }
        AudioManager manager = guild.getAudioManager();

        // MySendHandler should be your AudioSendHandler implementation
        try {
            manager.setSendingHandler(new MySendHandler(url));
        } catch (ExtractionException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("ERROR! Extraction failed").queue();
            return;

        } catch (IOException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("ERROR! Unknown Error\n" + StringUtils.join(e.getStackTrace(), "\n"))
                    .queue();
            return;
        }
        // Here we finally connect to the target voice channel
        // and it will automatically start pulling the audio from the MySendHandler instance
        manager.openAudioConnection(vc);
    }
}