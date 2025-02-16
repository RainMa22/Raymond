package me.rainma22.Raymond.dataprovider.ffmpeginstance;

import me.rainma22.Raymond.GlobalOptions;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedFFmpegInstance extends FFmpegInstance {
    private final int frameSize;
    private final BlockingQueue<byte[]> cacheQueue;
    private final LoaderThread loader = new LoaderThread();

    public CachedFFmpegInstance(String inPath, int frameSize, int second) {
        super(inPath);
        this.frameSize = frameSize;
        int cacheSize = GlobalOptions.getGlobalOptions().getCacheSize();
        if (cacheSize <= 0) cacheQueue = new LinkedBlockingQueue<>();
        else cacheQueue = new LinkedBlockingQueue<>(cacheSize);
        seek(second);
        loader.start();
    }

    @Override
    public byte[] provide(int length) throws IOException {
        if (frameSize != length) throw new IOException(String.format("%d != %d !!!", frameSize, length));
        byte[] out = cacheQueue.poll();
        while (!loader.isDone() && out == null) {
            synchronized (loader) {
                loader.notify();
            }
            out = cacheQueue.poll();
        }

        return out;
    }

    @Override
    public void seek(float second) {
        synchronized (loader) {
            cacheQueue.clear();
            super.seek(second);
            loader.setCurrFrame((int) (second * FRAMES_PER_SECOND));
        }
    }

    private class LoaderThread extends Thread {
        private static final int NUM_RETRIES = 3;
        private static final int WAIT_TIME_MS = 20;
        public AtomicBoolean isDone = new AtomicBoolean(false);
        public AtomicInteger currFrame;
        private int retries;

        public LoaderThread() {
            retries = 0;
            currFrame = new AtomicInteger(0);
        }

        public void setCurrFrame(int currFrame) {
            this.currFrame.set(currFrame);
        }


        public boolean isDone() {
            return isDone.get();
        }

        @Override
        public void run() {
            super.run();
            byte[] out;
            while (true) {
                synchronized (this) {
                    try {
                        out = CachedFFmpegInstance.super.provide(frameSize);
                        if (out.length == 0) {
                            if (CachedFFmpegInstance.super.isInterrupted())
                                throw new IOException("process is interrupted");
                            else break;
                        }
                        while (!cacheQueue.offer(out)) {
                            try {
                                this.wait(WAIT_TIME_MS);
                            } catch (InterruptedException e) {
                                //ignored
                            }
                        }
                        currFrame.incrementAndGet();
                    } catch (IOException e) {
                        if (retries > NUM_RETRIES) {
                            break;
                        }
                        retries++;
                        CachedFFmpegInstance.super.seek((float) (currFrame.get()) / FRAMES_PER_SECOND);
                    }
                }
            }
            isDone.set(true);
        }
    }
}
