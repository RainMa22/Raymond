package me.rainma22.Raymond.dataprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class s16beProviderInstance {
    protected InputStream inputStream = InputStream.nullInputStream();
    public byte[] provide(int length) throws IOException {
        byte[] out = inputStream.readNBytes(length);
        return out.length == 0 ? new byte[0]: Arrays.copyOf(out, length);
    }

    public void cleanup(){}
}
