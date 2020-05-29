package kriuchkov.maksim.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CountDownLatch;

public class NetworkHandler {
    private Channel channel;

    private static NetworkHandler instance = new NetworkHandler();

    private NetworkHandler() {

    }

    public static NetworkHandler getInstance() {
        return instance;
    }

    public void launch(CountDownLatch countDownLatch, String address, int port, IncomingDataReader incomingDataReader) throws Throwable {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(address, port)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            channel = socketChannel;
                            socketChannel.pipeline().addLast(incomingDataReader);
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();
            countDownLatch.countDown();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public Channel getChannel() {
        return channel;
    }
}
