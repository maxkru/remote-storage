package ru.geekbrains.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import ru.geekbrains.common.Protocol;

import java.nio.charset.StandardCharsets;

public class CommandService {
    public static void sendMsg(String msg, Channel channel) {
        ByteBuf byteBuf;

        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);

        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + msgBytes.length);
        byteBuf.writeByte(Protocol.COMMAND_SIGNAL_BYTE);
        byteBuf.writeInt(msgBytes.length);
        byteBuf.writeBytes(msgBytes);
        channel.writeAndFlush(byteBuf);
    }
}
