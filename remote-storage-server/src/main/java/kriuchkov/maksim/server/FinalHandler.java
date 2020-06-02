package kriuchkov.maksim.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import kriuchkov.maksim.common.Protocol;

public class FinalHandler extends ChannelInboundHandlerAdapter {

    private String username = "guest";
    private ServerFileService fileService;
    private ServerCommandService commandService;

    private final byte[] buffer = new byte[Protocol.MAX_FRAME_BODY_LENGTH];

    public FinalHandler() {
        fileService = new ServerFileService();
        commandService = new ServerCommandService(fileService);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        fileService.close();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof String) {
            String command = (String) msg;
            commandService.parseAndExecute(command, ctx.channel());
        } else {
            ByteBuf data = (ByteBuf) msg;
            fileService.receiveData(data);
        }
    }

}
