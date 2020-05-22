package ru.geekbrains.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import ru.geekbrains.common.Protocol;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class ClientMain {

    static final String SERVER_IP_ADDRESS = "localhost";
    static final int SERVER_PORT = 8189;

    static final int BUFFER_SIZE = 1024;

    private static NetworkHandler networkHandler = NetworkHandler.getInstance();

    public static void main(String[] args) throws Throwable {
        IncomingDataReader reader = new IncomingDataReader();
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                networkHandler.launch(latch, SERVER_IP_ADDRESS, SERVER_PORT, reader);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }).start();
        latch.await();


        Scanner scanner = new Scanner(System.in);
        String line;
        while (true) {
            System.out.print("> ");
            line = scanner.nextLine();

            File storedFile = null;
            if (line.isEmpty()) {
                System.out.println("Bye");
                break;
            } else if (line.startsWith("STORE ")) {
                storedFile = new File("local/" + line.split(" ")[1]);
                if (!storedFile.exists()) {
                    System.out.println("No such file - " + storedFile.getName());
                    continue;
                }
                sendMsg(line + " " + storedFile.length());
                System.out.print("Sending file " + storedFile.getName() + "...");
                sendFile(storedFile);
                System.out.println("Complete.");
                continue;
            }
            sendMsg(line);

            Thread.sleep(1000);

            String resp = reader.getMsg();
            if (resp.startsWith("FETCH-RESP ")) {
                String[] respSplit = resp.split(" ");
                if (respSplit[1].equals("NOT-FOUND")) {
                    System.out.println("File not found.");
                    continue;
                }
                long fileSize = Long.parseLong(respSplit[3]);
                System.out.print("Receiving file, " + fileSize + " bytes... ");
                File file = new File("local/" + respSplit[2]);
                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                byte[] buffer = new byte[BUFFER_SIZE];
                while (fileSize > 0) {
                    reader.getData(buffer, (fileSize > BUFFER_SIZE) ? BUFFER_SIZE : (int) fileSize);
                    fileOutputStream.write(buffer, 0, (fileSize > BUFFER_SIZE) ? BUFFER_SIZE : (int) fileSize);
                    fileSize -= BUFFER_SIZE;
                }
                System.out.println("Complete.");

            } else if (resp.startsWith("LIST-RESP ")) {
                String[] respSplit = resp.split("\n");
                System.out.print("File list:");
                for (int i = 1; i < respSplit.length; i++) {
                    System.out.print(" " + respSplit[i]);
                }
                System.out.println();
            } else if (resp.startsWith("STORE-RESP ")) {
                if (storedFile == null) {
                    System.out.println("Received uncalled STORE-RESP from server.");
                    continue;
                }
            } else {
                System.out.println(resp);
            }
        }
    }

    public static void sendFile(File file) {
        FileRegion region = new DefaultFileRegion(file, 0, file.length());
        Channel channel = networkHandler.getChannel();

        ByteBuf byteBuf;

        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(1 + 8);
        byteBuf.writeByte(Protocol.DATA_SIGNAL_BYTE);

        byteBuf.writeLong(file.length());
        channel.write(byteBuf);

        channel.flush();

        channel.writeAndFlush(region);
    }

    public static void sendMsg(String msg) {
        Channel channel = networkHandler.getChannel();
        ByteBuf byteBuf;

        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);

        byteBuf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + msgBytes.length);
        byteBuf.writeByte(Protocol.COMMAND_SIGNAL_BYTE);
        byteBuf.writeInt(msgBytes.length);
        byteBuf.writeBytes(msgBytes);
        channel.writeAndFlush(byteBuf);
    }

}
