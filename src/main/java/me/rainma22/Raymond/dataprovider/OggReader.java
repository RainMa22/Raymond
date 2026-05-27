package me.rainma22.Raymond.dataprovider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.rainma22.Raymond.Debug.Debugger;

public class OggReader implements AutoCloseable {

    private enum State {
        LOOK_FOR_OggS,
        PARSE_DATA,
        END,
        INVALID
    }
    private static final ByteBuffer OggS_MAGIC_NUM
            = ByteBuffer.wrap(new byte[]{'O', 'g', 'g', 'S'});
    private ByteBuffer buffer;
    private State state;
    private InputStream in;

    private Map<Integer, BitStream> bitStreams;

    public OggReader(InputStream fin) {
        in = fin;
        state = State.LOOK_FOR_OggS;
        bitStreams = new HashMap<>();
        buffer = ByteBuffer.allocate(4096);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public boolean isEndOfFile() {
        return state.equals(State.END) || state.equals(State.INVALID);
    }

    public State NextState() throws IOException {
        int readAmount;
        int readOffset = 0;
        byte[] window = buffer.array();
        switch (state) {
            default:
                return State.INVALID;
            case END:
                return state;
            case LOOK_FOR_OggS:
                readAmount = in.readNBytes(window, readOffset, buffer.capacity() - readOffset);
                if (readAmount == -1) {
                    return State.END;
                } else {
//                    readOffset contains the amount of valid data in the buffer
                    buffer.limit(readOffset + readAmount);
                    while (buffer.remaining() > 3) {
                        OggS_MAGIC_NUM.rewind();
                        if (OggS_MAGIC_NUM.getInt() == buffer.getInt()) {
                            return State.PARSE_DATA;
                        }
                    }
                }
                readOffset = buffer.remaining();
                System.arraycopy(window, buffer.position(), window, 0, buffer.remaining());
                buffer.position(0);
                return State.LOOK_FOR_OggS;
            case PARSE_DATA:
                //move over array
                readOffset = buffer.remaining();
                System.arraycopy(window, buffer.position(), window, 0, readOffset);
                readAmount = in.readNBytes(window, readOffset, buffer.capacity() - readOffset);
                if (readAmount == -1) {
                    readAmount = 0;
                    // defer processing of EOF until later
                }
                buffer.limit(readOffset + readAmount);
                buffer.position(0);
                byte version = buffer.get();
                byte headerType = buffer.get();
                long granulePosition = buffer.getLong();
                int streamId = buffer.getInt();
                int checksum = buffer.getInt();
                long pageSeqNum = buffer.getInt() & 0xffffffff;
                int nSegments = buffer.get() & 0xff;
                int pageSize = 0;
                do {
                    pageSize += buffer.get() & 0xff;
                    nSegments--;
                } while (nSegments > 0);

                byte[] pageData = new byte[pageSize];

                if (pageSize > buffer.remaining()) {
                    int readFromBufferAmt = buffer.remaining();
                    int readNeeded = pageSize - readFromBufferAmt;
                    buffer.get(pageData, 0, readFromBufferAmt);
                    readAmount = in.readNBytes(pageData, readFromBufferAmt, readNeeded);
                    if (readAmount == -1) {
                        readAmount = 0;
                        // defer processing of EOF until later
                    }
                    if (readAmount < readNeeded) {
                        Debugger.getInstance().log("Missing Data of at least "
                                + String.valueOf(readNeeded - readAmount)
                                + "byte, continuing", Debugger.WARNING);
                    }
                } else {
                    buffer.get(pageData);
                }
                BitStream bs = bitStreams.computeIfAbsent(streamId,
                        (_) -> new BitStream());
                if ((headerType & 0x01) == 0) {
                    //not a continued packet
                    bs.commitPacket();
                }
                bs.appendData(pageSeqNum, pageData, (headerType & 0x02) != 0);
                if ((headerType & 0x04) != 0) {
                    //end of stream
                    bs.commitPacket();
                    bs.endOfStream = true;
                }
                readOffset = buffer.remaining();
                return State.LOOK_FOR_OggS;
        }
    }

    public void close() throws IOException {
        in.close();
    }

}

class BitStream {

    public long lastPage = -1;
    public Packet currentPacket = new Packet();
    public List<byte[]> packets = new ArrayList();
    public boolean endOfStream = false;

    public void appendData(long pageNum, byte[] data2, boolean beginningOfStream) {
        if (beginningOfStream) {
            lastPage = pageNum;
        }
        if (lastPage + 1 < pageNum) {
            Debugger.getInstance().log("new page num is larger than expected:"
                    + String.valueOf(pageNum)
                    + "With a difference of "
                    + String.valueOf(pageNum - lastPage)
                    + ", Committing 1 potentially-faulty packet",
                    Debugger.WARNING);
            commitPacket();
        }
        lastPage = pageNum;
        currentPacket.appendData(data2);
    }

    public void commitPacket() {
        packets.add(currentPacket.data());
        currentPacket = new Packet();
    }

}

class Packet {

    private ByteArrayOutputStream payload = new ByteArrayOutputStream();

    public void appendData(byte[] data2) {
        payload.writeBytes(data2);
    }

    public byte[] data() {
        return payload.toByteArray();
    }
}
