package kriuchkov.maksim.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageTypeDecoder extends ByteToMessageDecoder {

    public MessageTypeDecoder() {
//        setSingleDecode(true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.getByte(0) == Protocol.COMMAND_SIGNAL_BYTE) {
            in.skipBytes(1 + 4); // discards frame header
            in.discardReadBytes();
            out.add(in.toString(StandardCharsets.UTF_8));
            in.skipBytes(in.readableBytes());
        } else if (in.getByte(0) == Protocol.DATA_SIGNAL_BYTE) {
            in.skipBytes(1 + 4); // discards frame header
            in.discardReadBytes();
            out.add(in);
        } else {
            throw new CorruptedFrameException("Expected a signal byte in front of frame, but it was not there.");
        }
    }
}
