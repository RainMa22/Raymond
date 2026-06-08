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
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import me.rainma22.Raymond.Debug.Debugger;
import me.rainma22.Raymond.Utils.RetryUtils;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.VideoStream;

public class QueuedMusicHandler implements AudioSendHandler {

    private static final int SAMPLE_SIZE = 3840;
    private static final String NEXT_SONG_MSG = "Song Done, loading next song: %s";
    private static final String QUEUE_CLEARED_MSG = "Queue has been cleared.";
    private static final String SONG_ERR = "Encountered an Exception while loading song: %s";
    private static final String NEXT_SONG_ERR = "Encountered an Exception while loading next song: %s";
    private static final String NO_STREAM_FOUND_ERR = "Unable to find audio stream for: %s";
    private static final String STOPPED_MSG = "Bot Stopped, Bye-bye!";
    private String preferredEncoding;
    private final LinkedBlockingQueue<URL> songQueue;
    private StreamingService service = null;
    private final AudioManager manager;
    private final VoiceChannel voiceChannel;
    private final TextChannel originChannel;
    private Debugger debug;
    private s16beProviderInstance providerInstance;
    private GlobalOptions globals;
    private float volume = 1f;
    private byte[] nextData;

    public QueuedMusicHandler(AudioManager manager, VoiceChannel voiceChannel, TextChannel originChannel) {
        super();
        songQueue = new LinkedBlockingQueue<>();
        this.originChannel = originChannel;
        this.manager = manager;
        globals = GlobalOptions.getGlobalOptions();
        preferredEncoding = globals.getPreferredEncodingIn();
        manager.setSendingHandler(this);
        debug = Debugger.getInstance();
        this.voiceChannel = voiceChannel;
    }

    private void clearQueue() {
        songQueue.clear();
        originChannel.sendMessage(QUEUE_CLEARED_MSG).queue();
    }

    private void clearProvider() {
        if (providerInstance != null) {
            providerInstance.cleanup();
        }
        providerInstance = null;
    }

    private void loadURL(URL url) throws ExtractionException, IOException {
        clearProvider();
        if (url == null) {
            return;
        }
        manager.openAudioConnection(voiceChannel);

        Double preferredBitrate = globals.getPreferredInBitrate_kbs();
        Function<AudioStream, Double> valueof = (as) -> Math.abs(as.getAverageBitrate() - preferredBitrate);

        RetryUtils.RetryingTask loadAudio = () -> {
            try {
                if (service == null) {
                    service = NewPipe.getService("YouTube");
                }
                YoutubeStreamExtractor extractor = (YoutubeStreamExtractor) service.getStreamExtractor(url.toString());
                extractor.fetchPage();
                var astreams = extractor.getAudioStreams();
                var vstreams = extractor.getVideoStreams();
                if (astreams.isEmpty()) {
                    debug.log("Audio stream is empty for some reason");
                    if (vstreams.isEmpty()) {
                        debug.log("Video stream is empty too for some reason");
                    }
                }

                Stream stream = astreams.stream()
                        .filter((as) -> as.getCodec().equals(preferredEncoding))
                        .min((as1, as2) -> valueof.apply(as1) < valueof.apply(as2) ? -1 : 1)
                        //fallback on other encoding
                        .or(() -> {
                            Debugger.getInstance().log("Unable to find" + preferredEncoding + "stream, trying to fall back");
                            return astreams.stream().min((as1, as2) -> valueof.apply(as1) < valueof.apply(as2) ? -1 : 1);
                        }).orElse(null);
                if (stream == null) {
                    Debugger.getInstance().log("Unable to find" + preferredEncoding + " Audio stream, trying to fall back to video stream");
                    stream = vstreams.getFirst();
                    if (stream != null) {
                        debug.log("Found a video stream:");
                        debug.LogStreamInfo(vstreams.getFirst());
                    }
                }

                String contentURL = stream.getContent();
                String query = url.getQuery();
                int startTime = 0;
                for (String param : query.split("&")) {
                    if (param.startsWith("t=")) {
                        try {
                            startTime = Integer.parseInt(param.substring(2));
                        } catch (NumberFormatException formatException) {
                            Debugger.getInstance().log("bad time format: " + param);
                        }
                    }
                }
                providerInstance = new CachedFFmpegInstance(contentURL, SAMPLE_SIZE, startTime);
                providerInstance.setVolume(volume);
                //FFMpeg convert to stereo, 48k sample rate, 16bit Big endian PCM audio and pipe back?
                return true;
            } catch (IOException | ExtractionException e) {
                return false;
            }
        };
        if (!loadAudio.isSuccess()) {
            throw new IOException(String.format(NO_STREAM_FOUND_ERR, url.toString()));
        }
    }

    public void seekCurrentMusic(float second) {
        if (providerInstance != null) {
            providerInstance.seek(second);
        }
    }

    /**
     * Queues the URL and returns its position on queue note: returns 0 if song
     * would be played immediately
     *
     */
    public int queueURL(URL url) throws ExtractionException, IOException {
        int position = songQueue.size();
        songQueue.add(url);
        if (position == 0) {
            try {
                loadURL(url);
            } catch (ExtractionException | IOException e) {
                songQueue.poll();
                throw e;
            }
        }
        return position;
    }

    /**
     * removes the current song from queue and start playing the next song
     *
     * @return the current URL playing if the URL is valid, else return null
     */
    @Nullable
    public URL loadNextSong() {
        songQueue.poll(); //remove playing url from queue
        URL nextUrl = songQueue.peek();
        try {
            loadURL(nextUrl);
            originChannel.sendMessage(String.format(NEXT_SONG_MSG, songQueue.peek())).queue();
        } catch (Exception e) {
            originChannel.sendMessage(
                    String.format(NEXT_SONG_ERR, e.getMessage())).queue();
            Debugger.getInstance().log(StringUtils.join(e.getStackTrace()));
        }
        return nextUrl;
    }

    public void stop() {
        clearQueue();
        clearProvider();
        manager.closeAudioConnection();
        originChannel.sendMessage(STOPPED_MSG).queue();
    }

    @Override
    public boolean canProvide() {
        if (providerInstance == null) {
            return false;
        }
        try {
            nextData = providerInstance.provide(SAMPLE_SIZE);
            if (nextData.length != 0) {
                return true;
            }
            loadNextSong();
            return canProvide(); //refresh to see if song can be provided
        } catch (Exception e) {
            originChannel.sendMessage(
                    String.format(SONG_ERR, StringUtils.join(e.getStackTrace(), "\n"))).queue();
            return false;
        }
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        //0.02 second
        //48000 samples/channel/seconds * 0.02 seconds/20 ms = 960 samples/20ms
        // * 2 bytes/sample * 2 channel = 3840 bytes/2 channel/20ms
        return ByteBuffer.wrap(nextData);
    }

    @Override
    public boolean isOpus() {
        return providerInstance != null && providerInstance.isOpus;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (providerInstance == null) {
            return;
        }
        providerInstance.setVolume(volume);
    }
}
