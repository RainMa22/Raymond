package me.rainma22;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

public class MySendHandler implements AudioSendHandler {
    static final String BASE_URL = "https://www.youtube.com/watch?v=";
    private static final int SAMPLE_SIZE = 3840;
//    private static Map<String, List<String>> downloadHeader;
//
//    static {
//        downloadHeader = Map.of("Range", List.of("bytes=0-"));
//    }

    private final Downloader downloader;
    private s16beProviderInstance ffmpegInstance;
    private byte[] nextData;


    public MySendHandler(String uid) throws ExtractionException, IOException {
        this(new URL(BASE_URL + uid));
    }

    public MySendHandler(URL url) throws ExtractionException, IOException {
        super();
        downloader = new DownloaderImpl();
        NewPipe.init(downloader, new Localization("CA", "en"));
        YoutubeStreamExtractor extractor = (YoutubeStreamExtractor) NewPipe.getService("YouTube")
                .getStreamExtractor(url.toString());
        extractor.fetchPage();
        AudioStream audioStream = extractor.getAudioStreams().stream().reduce((accumulator, stream) -> {
            if (accumulator == null) return stream;
            else {
                if (accumulator.getBitrate() < stream.getBitrate()) return stream;
                else return accumulator;
            }
        }).get();
        String contentURL = audioStream.getContent();

        String format = null;
        try {
            format = audioStream.getFormat().mimeType.split("/")[1];
        } catch (Exception e) {
            //do nothing
        }
        System.out.println(format);
//        byte[] fileData = downloader.get(contentURL, downloadHeader).responseMessage().getBytes(StandardCharsets.US_ASCII);
        ffmpegInstance = new FFmpegInstance(contentURL);
        //FFMpeg convert to stereo, 48k sample rate, 16bit Big endian PCM audio and pipe back?

    }

    @Override
    public boolean canProvide() {
        try {
            nextData = ffmpegInstance.provide(SAMPLE_SIZE);
        } catch (IOException e) {
            return false;
        }
        return nextData.length != 0;
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
