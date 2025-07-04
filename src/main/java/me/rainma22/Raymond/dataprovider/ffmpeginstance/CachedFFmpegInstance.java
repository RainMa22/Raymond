package me.rainma22.Raymond.dataprovider.ffmpeginstance;

import me.rainma22.Raymond.GlobalOptions;
import me.rainma22.Raymond.dataprovider.s16beProviderInstance;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedFFmpegInstance extends s16beProviderInstance {
    private final int frameSize;
    private final BlockingQueue<byte[]> cacheQueue;
    private final int NUM_RETRIES;
    private final LoaderThread loader = new LoaderThread();
    private final String inPath;
    private final FFmpegInstance instance;
    private float volume = 1f;
    private int currFrame = 0;
    public CachedFFmpegInstance(String inPath, int frameSize, int second) {
        this.inPath = inPath;
        instance = new FFmpegInstance(inPath);
        this.frameSize = frameSize;
        int cacheSize = GlobalOptions.getGlobalOptions().getCacheSize();
        NUM_RETRIES = GlobalOptions.getGlobalOptions().getNumRetries();
        if (cacheSize <= 0) cacheQueue = new LinkedBlockingQueue<>();
        else cacheQueue = new LinkedBlockingQueue<>(cacheSize);
        seek(second);
        loader.start();
    }

    @Override
    public byte[] provide(int length) throws IOException {
        if (frameSize != length) throw new IOException(String.format("%d != %d !!!", frameSize, length));
        byte[] out;
        while (!loader.isDone.get() && cacheQueue.isEmpty()) {
            synchronized (loader) {
                loader.notify();
            }
        }
        if (loader.isDone.get() || cacheQueue.size() < cacheQueue.remainingCapacity()){
            synchronized (loader) {
                loader.notify();
            }
        }
        if ((out = cacheQueue.poll()) == null || out.length == 0) return new byte[0];
        currFrame++;
        return out;
    }

    @Override
    public void seek(float second) {
        currFrame = Math.round(second*FRAMES_PER_SECOND);
        synchronized (loader) {
            cacheQueue.clear();
            loader.setCurrFrame(currFrame);
            if(loader.isDone.get()){
                loader.isDone.set(false);
            }
        }
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        seek((float) currFrame/FRAMES_PER_SECOND);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        loader.shouldExit.set(true);
    }

    private class LoaderThread extends Thread {
        private static final int WAIT_TIME_MS = 20;
        public AtomicBoolean isDone = new AtomicBoolean(false);
        public AtomicBoolean shouldExit = new AtomicBoolean(false);
        public AtomicInteger currFrame;
        private int retries;

        public LoaderThread() {
            retries = 0;
            currFrame = new AtomicInteger(0);
            setName("Loader Thread For " + inPath);
        }

        public void setCurrFrame(int currFrame) {
            this.currFrame.set(currFrame);
            instance.seek((float) currFrame/s16beProviderInstance.FRAMES_PER_SECOND);
            instance.setVolume(volume);
            retries = 0;
        }

        private void waitBeforeRetry(){
            try {
                synchronized (this) {
                    wait(WAIT_TIME_MS);
                }
            } catch (InterruptedException e) {
                //ignored
            }
        }

        @Override
        public void run() {
            super.run();
            byte[] out;
            while (!shouldExit.get()) {
                if(isDone.get()) waitBeforeRetry();
                synchronized (this) {
                    try {
                        out = instance.provide(frameSize);
                        if (out.length == 0) {
                            if (instance.isInterrupted())
                                throw new IOException("process is interrupted");
                            else isDone.set(true);
                        }
                        while (!cacheQueue.offer(out)) {
                            waitBeforeRetry();
                        }
                        currFrame.incrementAndGet();
                    } catch (IOException e) {
                        if (retries > NUM_RETRIES) {
                            isDone.set(true);
                        }
                        retries++;
                        instance.seek((float) (currFrame.get()) / FRAMES_PER_SECOND);
                    }
                }
            }
        }
    }
}
