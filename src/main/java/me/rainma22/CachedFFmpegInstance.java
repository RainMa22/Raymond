package me.rainma22;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class CachedFFmpegInstance extends FFmpegInstance {
    boolean inited = false;
    private int frameSize;
    private LinkedBlockingQueue<byte[]> blockingQueue;

    public CachedFFmpegInstance(String inPath, int frameSize) throws IOException {
        super(inPath);
        this.frameSize = frameSize;
        blockingQueue = new LinkedBlockingQueue<>();
        new loaderThread().start();
    }

    @Override
    public byte[] provide(int length) throws IOException {
        if (frameSize != length) throw new IOException(String.format("%d != %d !!!", frameSize, length));
        byte[] out = blockingQueue.poll();
        if (!inited) {
            while (out == null){
                out = blockingQueue.poll();
            };
            inited = true;
        }

        return out;
    }

    private class loaderThread extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] out;
            try {
                while ((out = CachedFFmpegInstance.super.provide(frameSize)).length != 0) {
                    blockingQueue.add(out);
                }
            }catch (IOException e){
                blockingQueue.add(new byte[0]);
            }
        }
    }
}
