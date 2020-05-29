package kriuchkov.maksim.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import kriuchkov.maksim.common.Protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler extends ChannelInboundHandlerAdapter {



    private String username = "guest";

    private enum State {
        AWAITING_COMMAND, AWAITING_DATA
    }

    private State state = State.AWAITING_COMMAND;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("OK");
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) {
                byte firstByte = in.readByte();
                if (firstByte == Protocol.COMMAND_SIGNAL_BYTE && in.isReadable(4) && state == State.AWAITING_COMMAND) {
                    int commandByteLength = in.readInt();
                    if (in.isReadable(commandByteLength)) {
                        String input = in.readCharSequence(commandByteLength, StandardCharsets.UTF_8).toString();
                        String[] split = input.split(" ");
                        String command = split[0];

                        switch (command) {
                            case "AUTH":
                                if (split.length == 2) {
                                    username = split[1];
                                    CommandService.sendMsg("AUTH-RESP OK", ctx.channel());
                                } else {
                                    CommandService.sendMsg("SYNTAX-ERROR", ctx.channel());
                                }
                                break;
                            case "LIST":
                                Path folder = Paths.get(".");
                                List<Path> files = Files.list(folder)
                                        .filter(Files::isRegularFile)
                                        .collect(Collectors.toList());
                                StringBuilder sb = new StringBuilder("LIST-RESP\n");
                                for (Path f : files)
                                    sb.append(f.getFileName()).append("\n");
                                CommandService.sendMsg(sb.toString(), ctx.channel());
                                break;

                            case "FETCH":
                                String filenameFetch = input.split(" ", 2)[1];
                                Path pathFetch = Paths.get("remote", filenameFetch);
                                if (Files.notExists(pathFetch)) {
                                    CommandService.sendMsg("FETCH-RESP NOT-FOUND", ctx.channel());
                                } else {
                                    File file = pathFetch.toFile();
                                    CommandService.sendMsg("FETCH-RESP OK " + file.length(), ctx.channel());
                                    FileService.sendFile(file, ctx.channel(), null);
                                }
                                break;

                            case "STORE":
                                String filenameStore = split[1];
                                long fileLength;
                                try {
                                    fileLength = Long.parseLong(split[2]);
                                } catch (NumberFormatException e) {
                                    System.err.println("Error parsing command: " + input);
                                    CommandService.sendMsg("SYNTAX-ERROR", ctx.channel());
                                    break;
                                }
                                FileService.receiveFile(filenameStore, fileLength);
                                CommandService.sendMsg("STORE-RESP OK", ctx.channel());
                                state = State.AWAITING_DATA;
                                break;

                            case "REMOVE":
                                String filenameRemove = input.split(" ", 2)[1];
                                Path pathRemove = Paths.get(filenameRemove);
                                if (Files.notExists(pathRemove)) {
                                    CommandService.sendMsg("REMOVE-RESP NOT-FOUND", ctx.channel());
                                } else {
                                    Files.delete(pathRemove);
                                    CommandService.sendMsg("REMOVE-RESP OK", ctx.channel());
                                }
                                break;
                        }
                    }
                } else if (firstByte == Protocol.DATA_SIGNAL_BYTE && state == State.AWAITING_DATA) {
                    while (in.isReadable()) {
                        
                    }
                } else {
                    // ??
                }
            }
        } finally {
            in.release();
        }
    }

}
