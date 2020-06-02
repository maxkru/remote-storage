package kriuchkov.maksim.server;

import io.netty.channel.Channel;
import kriuchkov.maksim.common.CommandService;
import kriuchkov.maksim.common.FileService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ServerCommandService extends CommandService {

    private ServerFileService fileService;

    public ServerCommandService(ServerFileService fileService) {
        this.fileService = fileService;
    }

    public void parseAndExecute(String input, Channel channel) throws Exception {
        String[] split = input.split(" ");
        String command = split[0];

        switch (command) {
            case "AUTH":
                if (split.length == 2) {
//                    username = split[1];
                    ServerCommandService.sendMsg("AUTH-RESP OK", channel);
                } else {
                    ServerCommandService.sendMsg("SYNTAX-ERROR", channel);
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
                ServerCommandService.sendMsg(sb.toString(), channel);
                break;

            case "FETCH":
                String filenameFetch = input.split(" ", 2)[1];
                Path pathFetch = Paths.get("remote", filenameFetch);
                if (Files.notExists(pathFetch)) {
                    ServerCommandService.sendMsg("FETCH-RESP NOT-FOUND", channel);
                } else {
                    File file = pathFetch.toFile();
                    ServerCommandService.sendMsg("FETCH-RESP OK " + file.getName() + " " + file.length(), channel);
                    FileService.sendFile(file, channel, null);
                }
                break;

            case "STORE":
                String filenameStore = split[1];
                long fileLength;
                try {
                    fileLength = Long.parseLong(split[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing command: " + input);
                    ServerCommandService.sendMsg("SYNTAX-ERROR", channel);
                    break;
                }
                fileService.setDataTarget(Paths.get("remote", filenameStore).toFile(), fileLength);
                ServerCommandService.sendMsg("STORE-RESP OK", channel);
//                state = ClientHandler.State.AWAITING_DATA;
                break;

            case "REMOVE":
                String filenameRemove = input.split(" ", 2)[1];
                Path pathRemove = Paths.get(filenameRemove);
                if (Files.notExists(pathRemove)) {
                    ServerCommandService.sendMsg("REMOVE-RESP NOT-FOUND", channel);
                } else {
                    Files.delete(pathRemove);
                    ServerCommandService.sendMsg("REMOVE-RESP OK", channel);
                }
                break;
        }
    }
}
