package ru.geekbrains.client;

import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    static final String SERVER_IP_ADDRESS = "localhost";
    static final int SERVER_PORT = 8189;

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        Scanner scanner = new Scanner(System.in);
        String line;
        while (true) {
            System.out.print("> ");
            line = scanner.nextLine();
            if (line.isEmpty()) {
                System.out.println("Bye");
                break;
            }
            out.writeUTF(line);
            String resp = in.readUTF();
            if (resp.startsWith("FETCH-RESP ")) {
                String[] respSplit = resp.split(" ");
                if (respSplit[1].equals("NOT-FOUND")) {
                    System.out.println("File not found.");
                    continue;
                }
                long fileSize = Long.parseLong(respSplit[2]);
                System.out.print("Receiving file, " + fileSize + " bytes... ");
                File file = new File("received.txt");
                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                byte[] buffer = new byte[BUFFER_SIZE];
                while (fileSize > 0) {
                    in.read(buffer);
                    fileOutputStream.write(buffer, 0, (fileSize > BUFFER_SIZE) ? BUFFER_SIZE : (int) fileSize);
                    fileSize -= BUFFER_SIZE;
                }
                System.out.println("Complete.");

            } else if (resp.startsWith("LIST-RESP ")) {
                String[] respSplit = resp.split(" "); // TODO: подобрать regex для выделения имён файлов из выражения вида 'LIST-RESP "file 1.txt" "file 2.txt"'
                System.out.print("File list:");
                for(int i = 1; i < respSplit.length; i++) {
                    System.out.print(" " + respSplit[i]);
                }
                System.out.println();
            }
        }
    }

}
