package me.rainma22.Raymond;

import me.rainma22.Raymond.dataprovider.ffmpeginstance.CachedFFmpegInstance;
import me.rainma22.Raymond.dataprovider.s16beProviderInstance;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class QueuedMusicHandler implements AudioSendHandler {
    //    static final String BASE_URL = "https://www.youtube.com/watch?v=";
    private static final int SAMPLE_SIZE = 3840;
    private static final s16beProviderInstance DUMMIE_PROVIDER = new s16beProviderInstance() {
        @Override
        public byte[] provide(int length) throws IOException {
            return new byte[0];
        }
    };
    private static final String NEXT_SONG_MSG = "Song Done, loading next song: %s";
    private static final String QUEUE_CLEARED_MSG = "Queue has been cleared.";
    private static final String NEXT_SONG_ERR = "Encountered an Exception while loading next song: %s";
    private static final String STOPPED_MSG = "Bot Stopped, Bye-bye!";



    private final Downloader downloader;
    private s16beProviderInstance providerInstance;
    private byte[] nextData;
    private final LinkedBlockingQueue<URL> songQueue;
    private final AudioManager manager;
    private final VoiceChannel voiceChannel;
    private final TextChannel originChannel;
    private float volume = 1.0f;

    private void clearQueue(){
        songQueue.clear();
        originChannel.sendMessage(QUEUE_CLEARED_MSG).queue();
    }
    private void clearProvider(){
        if(providerInstance != null){
            providerInstance.cleanup();
        }
        providerInstance = null;
    }

    private void loadURL(URL url) throws ExtractionException, IOException {
        clearProvider();
        if (url == null){
            return;
        };
        NewPipe.init(downloader, new Localization("CA", "en"));
        YoutubeStreamExtractor extractor = (YoutubeStreamExtractor) NewPipe.getService("YouTube")
                .getStreamExtractor(url.toString());
        extractor.fetchPage();
        AudioStream audioStream = extractor.getAudioStreams().stream().reduce((accumulator, stream) -> {
            if (accumulator.getBitrate() < stream.getBitrate()) return stream;
            else return accumulator;
        }).get();
        String contentURL = audioStream.getContent();

        providerInstance = new CachedFFmpegInstance(contentURL, SAMPLE_SIZE);
        //FFMpeg convert to stereo, 48k sample rate, 16bit Big endian PCM audio and pipe back?
        manager.openAudioConnection(voiceChannel);
    }

    public QueuedMusicHandler(AudioManager manager, VoiceChannel voiceChannel, TextChannel originChannel) {
        super();
        downloader = new DownloaderImpl();
        songQueue = new LinkedBlockingQueue<>();
        this.originChannel = originChannel;
        this.manager = manager;
        manager.setSendingHandler(this);

        this.voiceChannel = voiceChannel;
    }

    /**
     * Queues the url and returns its position on queue
     * note: returns 0 if song would be played immediately
     **/
    public int queueURL(URL url) throws ExtractionException, IOException {
        int position = songQueue.size();
        songQueue.add(url);
        if (position == 0) loadURL(url);
        return position;
    }

    /**
     * removes the current song from queue and start playing the next song
     * @return the current URL playing if the URL is valid, else return null
     */
    @Nullable
    public URL loadNextSong() {
        songQueue.poll(); //remove playing url from queue
        URL out = songQueue.peek();
        try {
            loadURL(out);
        } catch (Exception ignored) {
        }
        return out;
    }

    public void stop(){
        clearQueue();
        loadNextSong();
        manager.closeAudioConnection();
        originChannel.sendMessage(STOPPED_MSG).queue();
    }




    @Override
    public boolean canProvide() {
        if (providerInstance == null) return false;

        try {
            nextData = providerInstance.provide(SAMPLE_SIZE);
        } catch (Exception e) {
            nextData = null;
        }
        boolean canProvide = nextData != null;

        if (!canProvide) {
            try {
                loadNextSong();
                boolean result = !songQueue.isEmpty() && canProvide();
                originChannel.sendMessage(String.format(NEXT_SONG_MSG, songQueue.peek())).queue();
                return result; //refresh to see if song can be provided
            } catch (Exception e) {
                originChannel.sendMessage(
                        String.format(NEXT_SONG_ERR, StringUtils.join(e.getStackTrace(), "\n"))).queue();
                clearQueue();
                return false;
            }
        }
        return true;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        //0.02 second
        //48000 samples/channel/seconds * 0.02 seconds/20 ms = 960 samples/20ms
        // * 2 bytes/sample * 2 channel = 3840 bytes/2 channel/20ms
        for (int i = 0; i < nextData.length; i++) {
            nextData[i] = (byte) (nextData[i] * volume);
        }
        return ByteBuffer.wrap(nextData);
    }

    @Override
    public boolean isOpus() {
        return false;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
