package kriuchkov.maksim.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class IncomingDataReader extends ChannelInboundHandlerAdapter {

    public IncomingDataReader() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");

        if (msg instanceof String) {
            String command = (String) msg;
            ClientCommandService.getInstance().parseAndExecute(command, ctx.channel());
        } else {
            ByteBuf data = (ByteBuf) msg;
            ClientFileService.getInstance().receiveData(data);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
