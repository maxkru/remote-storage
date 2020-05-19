package ru.geekbrains.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
                            Path path = Paths.get(fileName);
                            if (Files.notExists(path)) {
                                sendMsg("FETCH-RESP NOT-FOUND");
                            } else {
                                File file = path.toFile();
                                sendMsg("FETCH-RESP OK " + file.length());
                                sendFile(file);
                            }
                        } else if (str.equals("LIST")) {
                            Path folder = Paths.get(".");
                            List<Path> files = Files.list(folder)
                                    .filter(Files::isRegularFile)
                                    .collect(Collectors.toList());

                            StringBuilder sb = new StringBuilder("LIST-RESP\n");
                            for (Path f : files) {
                                sb.append(f.getFileName()).append("\n");
                            }
                            sendMsg(sb.toString());
                        } else if (str.startsWith("STORE ")) {
                            String[] strSplit = str.split(" ");
                            String fileName = strSplit[1];
                            long fileLength;
                            try {
                                fileLength = Long.parseLong(strSplit[2]);
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing command: " + str);
                                sendMsg("SYNTAX-ERROR");
                                continue;
                            }
                            receiveFile(fileLength);
                            sendMsg("STORE-RESP OK");
                        } else {
                            sendMsg("SYNTAX-ERROR");
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
            while ((nRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int BUFFER_SIZE = 1024;

    public void receiveFile(long length) {
        try {
            File file = new File("sent.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            byte[] buffer = new byte[BUFFER_SIZE];
            while (length > 0) {
                in.read(buffer);
                fileOutputStream.write(buffer, 0, (length > BUFFER_SIZE) ? BUFFER_SIZE : (int) length);
                length -= BUFFER_SIZE;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
