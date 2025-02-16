package me.rainma22.Raymond.dataprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class s16beProviderInstance {
    public static final int FRAMES_PER_SECOND = 50;
    protected InputStream inputStream = InputStream.nullInputStream();
    public byte[] provide(int length) throws IOException {
        byte[] out = inputStream.readNBytes(length);
        if(out.length == length || out.length == 0) return out;
        else return Arrays.copyOf(out, length);
    }

    public void cleanup(){}
}
