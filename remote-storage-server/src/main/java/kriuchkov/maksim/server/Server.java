package kriuchkov.maksim.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import kriuchkov.maksim.common.MessageTypeDecoder;
import kriuchkov.maksim.common.OutboundMessageSplitter;
import kriuchkov.maksim.common.Protocol;

public class Server {

    public Server() {

    }

    public void launch(int port) throws Throwable {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // out
                            socketChannel.pipeline().addLast(new OutboundMessageSplitter());

                            // in
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Protocol.MAX_FRAME_BODY_LENGTH, 1, 4));
                            socketChannel.pipeline().addLast(new MessageTypeDecoder());
                            socketChannel.pipeline().addLast(new FinalHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
