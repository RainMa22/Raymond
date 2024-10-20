package me.rainma22.Raymond;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
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
//    private static Map<String, List<String>> downloadHeader;
//
//    static {
//        downloadHeader = Map.of("Range", List.of("bytes=0-"));
//    }

    private final Downloader downloader;
    private s16beProviderInstance providerInstance;
    private byte[] nextData;
    private LinkedBlockingQueue<URL> songQueue;
    private AudioManager manager;
    private VoiceChannel voiceChannel;

    public QueuedMusicHandler(AudioManager manager, VoiceChannel voiceChannel) {
        super();
        downloader = new DownloaderImpl();
        songQueue = new LinkedBlockingQueue<>();

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

    public URL loadNextSong() {
        songQueue.poll();//remove playing url from queue
        URL out = songQueue.peek();
        if (out == null) {
            providerInstance = DUMMIE_PROVIDER;
            return null;
        }
        try {
            loadURL(out);
        } catch (Exception e) {
        }
        return out;
    }

    private void loadURL(URL url) throws ExtractionException, IOException {
        if (url == null) return;
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


    @Override
    public boolean canProvide() {
        try {
            nextData = providerInstance.provide(SAMPLE_SIZE);
        } catch (Exception e) {
            nextData = null;
        }
        boolean canProvide = nextData != null && nextData.length != 0;
        if (!canProvide) {
            try {
                loadNextSong();
                return !songQueue.isEmpty() && canProvide(); //refresh to see if song can be provided
            } catch (Exception e) {
                e.printStackTrace();
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
        return ByteBuffer.wrap(nextData);
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
