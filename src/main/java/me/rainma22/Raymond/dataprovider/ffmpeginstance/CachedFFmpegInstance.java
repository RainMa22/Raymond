package me.rainma22.Raymond.dataprovider.ffmpeginstance;

import me.rainma22.Raymond.GlobalOptions;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedFFmpegInstance extends FFmpegInstance {
    private final int frameSize;
    private final BlockingQueue<byte[]> cacheQueue;
    private final LoaderThread loader = new LoaderThread();

    public CachedFFmpegInstance(String inPath, int frameSize) {
        super(inPath);
        this.frameSize = frameSize;
        int cacheSize = GlobalOptions.getGlobalOptions().getCacheSize();
        if (cacheSize <= 0) cacheQueue = new LinkedBlockingQueue<>();
        else cacheQueue = new ArrayBlockingQueue<>(cacheSize);
        loader.start();
    }

    @Override
    public byte[] provide(int length) throws IOException {
        if (frameSize != length) throw new IOException(String.format("%d != %d !!!", frameSize, length));
        byte[] out = cacheQueue.poll();
        while (!loader.isDone() && out == null) {
            out = cacheQueue.poll();
        }

        return out;
    }

    private class LoaderThread extends Thread {
        final int WAIT_TIME_NS = (int) (1e9 / 24000);
        public AtomicBoolean isDone = new AtomicBoolean(false);

        public boolean isDone() {
            return isDone.get();
        }

        @Override
        public void run() {
            super.run();
            byte[] out;
            try {
                while ((out = CachedFFmpegInstance.super.provide(frameSize)).length != 0) {
                    while (!cacheQueue.offer(out)) {
                        try {
                            synchronized (this) {
                                this.wait(0L, WAIT_TIME_NS);
                            }
                        } catch (InterruptedException e) {
                            //ignored
                        }
                    }
                }
            } catch (IOException e) {
                //ignored
            }
            isDone.set(true);
        }
    }
}
