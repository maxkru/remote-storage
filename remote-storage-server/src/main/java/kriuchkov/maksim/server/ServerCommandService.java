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

    private String userName;

    public ServerCommandService(ServerFileService fileService) {
        this.fileService = fileService;
        this.userName = null;
    }

    public void parseAndExecute(String input, Channel channel) throws Exception {
        String[] split = input.split(" ");
        String command = split[0];

        switch (command) {

            case "AUTH":
                if (split.length == 3) {
                    if (DatabaseHandler.isGoodCredentials(split[1], split[2])) {
                        sendMsg("AUTH-RESP OK", channel);
                        authorize(split[1]);
                    } else
                        sendMsg("AUTH-RESP BAD-CREDENTIALS", channel);
                } else {
                    sendMsg("AUTH-RESP SYNTAX-ERROR", channel);
                }
                break;


            case "LIST":
                if (!isAuthorized()) {
                    sendMsg("LIST-RESP AUTH-REQUIRED", channel);
                    break;
                }
                Path folder = Paths.get("remote", userName);
                List<Path> files = Files.list(folder)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());
                StringBuilder sb = new StringBuilder("LIST-RESP\n");
                for (Path f : files)
                    sb.append(f.getFileName()).append("\n");
                sendMsg(sb.toString(), channel);
                break;

            case "FETCH":
                if (!isAuthorized()) {
                    sendMsg("FETCH-RESP AUTH-REQUIRED", channel);
                    break;
                }
                String filenameFetch = input.split(" ", 2)[1];
                Path pathFetch = Paths.get("remote", userName, filenameFetch);
                if (Files.notExists(pathFetch)) {
                    ServerCommandService.sendMsg("FETCH-RESP NOT-FOUND", channel);
                } else {
                    File file = pathFetch.toFile();
                    ServerCommandService.sendMsg("FETCH-RESP OK " + file.getName() + " " + file.length(), channel);
                    FileService.sendFile(file, channel, null);
                }
                break;

            case "STORE":
                if (!isAuthorized()) {
                    sendMsg("STORE-RESP AUTH-REQUIRED", channel);
                    break;
                }
                String filenameStore = split[1];
                long fileLength;
                try {
                    fileLength = Long.parseLong(split[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing command: " + input);
                    ServerCommandService.sendMsg("SYNTAX-ERROR", channel);
                    break;
                }
                fileService.setDataTarget(Paths.get("remote", userName, filenameStore).toFile());
                fileService.setExpectedDataLength(fileLength);
                ServerCommandService.sendMsg("STORE-RESP OK", channel);
                break;

            case "REMOVE":
                if (!isAuthorized()) {
                    sendMsg("REMOVE-RESP AUTH-REQUIRED", channel);
                    break;
                }
                String filenameRemove = input.split(" ", 2)[1];
                Path pathRemove = Paths.get("remote", userName, filenameRemove);
                if (Files.notExists(pathRemove)) {
                    ServerCommandService.sendMsg("REMOVE-RESP NOT-FOUND", channel);
                } else {
                    Files.delete(pathRemove);
                    ServerCommandService.sendMsg("REMOVE-RESP OK", channel);
                }
                break;
        }
    }

    private void authorize(String login) {
        userName = login;
    }

    private boolean isAuthorized() {
        return userName != null;
    }
}
