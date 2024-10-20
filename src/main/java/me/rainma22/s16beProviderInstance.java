package me.rainma22;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class s16beProviderInstance {
    protected InputStream inputStream;
    public byte[] provide(int length) throws IOException {
        byte[] out = inputStream.readNBytes(length);
        return out.length == 0 ? out: Arrays.copyOf(out, length);
    }
}