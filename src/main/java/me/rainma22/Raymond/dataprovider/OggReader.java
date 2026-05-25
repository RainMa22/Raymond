package me.rainma22.Raymond.dataprovider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OggReader {

    private enum State {
        LOOK_FOR_OggS,
        PARSE_DATA,
        END
    }
    private static final ByteBuffer OggS_MAGIC_NUM
            = ByteBuffer.wrap(new byte[]{'O', 'g', 'g', 'S'});
    private ByteBuffer buffer;
    private State state;
    private InputStream in;

    private Map<Integer, BitStream> BitStreams;

    public OggReader(InputStream fin) {
        in = fin;
        state = State.LOOK_FOR_OggS;
        BitStreams = new HashMap<>();
        buffer = ByteBuffer.allocate(4096);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public boolean isEndOfFile() {
        return state.equals(State.END);
    }

    public State NextState() throws IOException {
        int status;
        int offset = 0;
        byte[] window = buffer.array();
        switch (state) {
            case END:
                return state;
            case LOOK_FOR_OggS:
                status = in.readNBytes(window, offset, buffer.capacity() - offset);
                if (status == -1) {
                    return State.END;
                } else {
                    while (buffer.remaining() > 3) {
                        if (OggS_MAGIC_NUM.getLong() == buffer.getLong()) {
                            return State.PARSE_DATA;
                        }
                    }
                }
                System.arraycopy(window, buffer.position(), window, 0, buffer.remaining());
                buffer.position(3);
                return State.LOOK_FOR_OggS;
            case PARSE_DATA:
//                move over array
                System.arraycopy(window, buffer.position(), window, 0, buffer.remaining());
                buffer.position(buffer.remaining());
                status = in.readNBytes(window, buffer.position(), buffer.remaining());
                if (status == -1) {
                    status = 0;
                }
                byte version = buffer.get();
                byte headerType = buffer.get();
                long granulePosition = buffer.getLong();
                int streamId = buffer.getInt();
                int checksum = buffer.getInt();
                long pageSeqNum = buffer.getInt() & 0xffffffff;
                int nSegments = buffer.get() & 0xff;
                ByteBuffer segmentSizes = buffer.slice(buffer.position(), nSegments);
                int segmentSize = segmentSizes.get() & 0xff;
                do {
                    
                } while (segmentSizes.hasRemaining());
                break;
        }
    }

}

class BitStream {
    public long lastPage;
    public byte[] currentPacket;
    public List<byte[]> packets = new ArrayList();
    public boolean endOfStream = false;
}

class Packet{
    private ByteBuffer payload = ByteBuffer.allocate(0);
    public void appendData(byte[] data2){
    
    }

}
