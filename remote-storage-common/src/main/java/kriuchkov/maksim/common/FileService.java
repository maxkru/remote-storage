package kriuchkov.maksim.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.*;

public class FileService implements Closeable {

    protected static final int FILE_CHUNK_SIZE = Protocol.MAX_FRAME_BODY_LENGTH / 2;

    public static void sendFile(File file, Channel channel, Runnable callback) throws Exception {
        ChunkedNioFile chunkedNioFile = null;
        try {
            chunkedNioFile = new ChunkedNioFile(file, FILE_CHUNK_SIZE);

            while (!chunkedNioFile.isEndOfInput()) {
                ByteBuf next = chunkedNioFile.readChunk(ByteBufAllocator.DEFAULT);
                channel.writeAndFlush(next);
            }

            if (callback != null) {
                callback.run();
            }
        } finally {
            if (chunkedNioFile != null)
                chunkedNioFile.close();
        }

    }
    protected File dataTarget;
    protected long length;

    protected File dataSource;

    protected FileOutputStream fos;

    protected final byte[] buffer = new byte[Protocol.MAX_FRAME_BODY_LENGTH];

    public void setDataTarget(File dataTarget) throws FileNotFoundException {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataTarget = dataTarget;
        if (dataTarget != null)
            this.fos = new FileOutputStream(dataTarget);
    }

    public void setDataSource(File dataSource) throws FileNotFoundException {
        this.dataSource = dataSource;
    }

    public void receiveData(ByteBuf data) throws IOException {
        if (dataTarget == null)
            throw new RuntimeException("Unexpected data block.");

        int l = data.readableBytes();
        if (l > length)
            throw new RuntimeException("More data in block than expected.");
        data.readBytes(buffer, 0, l);
        fos.write(buffer, 0, l);
        fos.flush();
        length -= l;
        if (length == 0) {
            dataTarget = null;
            fos.close();
        }
    }

    @Override
    public void close() throws IOException {
        if (fos != null)
            fos.close();
    }

    public void setExpectedDataLength(long length) {
        this.length = length;
    }
}
