package kriuchkov.maksim.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import kriuchkov.maksim.common.Protocol;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

public class IncomingDataReader extends ChannelInboundHandlerAdapter {

    private Queue<String> commandQueue;
    private ByteBuf dataBuf;

    public IncomingDataReader() {
        commandQueue = new LinkedList<>();
        dataBuf = ByteBufAllocator.DEFAULT.buffer(1024 * 1024 * 8);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) {
                byte firstByte = in.readByte();
                if (firstByte == Protocol.COMMAND_SIGNAL_BYTE) {
                    int commandByteLength = in.readInt();
                    if (in.isReadable(commandByteLength)) {
                        String command = in.readCharSequence(commandByteLength, StandardCharsets.UTF_8).toString();
                        commandQueue.add(command);
                    }
                } else if (firstByte == Protocol.DATA_SIGNAL_BYTE) {
                    while (in.isReadable()) {
                        dataBuf.writeBytes(in);
                    }
                }
            }
        } finally {
            in.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void getData(byte[] arr, int length) {
        dataBuf.readBytes(arr, 0, length);
    }

    public String getMsg() {
        return commandQueue.remove();
    }
}
