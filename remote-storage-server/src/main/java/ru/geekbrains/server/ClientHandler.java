package ru.geekbrains.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private static final int TRANSFER_BUFFER_SIZE = 1024;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        System.out.println(str);
                        if (str.startsWith("FETCH ")) {
                            String fileName = str.split(" ")[1]; // TODO: improve regex
                            File file = new File(fileName);
                            if (!file.exists()) {
                                sendMsg("FETCH-RESP NOT-FOUND");
                            } else {
                                sendMsg("FETCH-RESP OK " + file.length());
                                sendFile(file);
                            }
                        }

                        if (str.equals("LIST")) {
                            File file = new File("/"); // TODO: получить содержимое папки
                            File[] files = file.listFiles();
                            StringBuilder sb = new StringBuilder("LIST-RESP");
                            for (File f : files) {
                                sb.append(" ").append(f.getName());
                            }
                            sendMsg(sb.toString());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
            int nRead;
            while ((nRead = fileInputStream.read(buffer, 0, TRANSFER_BUFFER_SIZE)) >= 0) {
                out.write(buffer, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
