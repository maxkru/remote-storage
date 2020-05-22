package ru.geekbrains.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import ru.geekbrains.common.Protocol;

import java.io.File;

public class FileService {
    public static void sendFile(File file, Channel channel, ChannelFutureListener finishListener) {
        FileRegion region = new DefaultFileRegion(file, 0, file.length());

        ByteBuf byteBuf;

        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(1 + 8);
        byteBuf.writeByte(Protocol.DATA_SIGNAL_BYTE);

        byteBuf.writeLong(file.length());
        channel.write(byteBuf);

        channel.flush();

        ChannelFuture future = channel.writeAndFlush(region);
        if (finishListener != null) {
            future.addListener(finishListener);
        }
    }

    public static void receiveFile(String fileName, long fileLength) {

    }
}
