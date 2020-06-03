package kriuchkov.maksim.client.connection;

import kriuchkov.maksim.common.CommandService;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class MainService {

    private static final MainService instance = new MainService();

    public static MainService getInstance() {
        return instance;
    }

    private NetworkHandler networkHandler = NetworkHandler.getInstance();
    private ClientCommandService commandService = ClientCommandService.getInstance();
    private ClientFileService fileService = ClientFileService.getInstance();

    public void launchNetwork(String serverAddress, int serverPort) {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                networkHandler.launch(latch, serverAddress, serverPort, new IncomingDataReader());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopNetwork() {
        networkHandler.stop();
    }

    public void fetch(String fileName) throws FileNotFoundException {
        File file = Paths.get("local", fileName).toFile();
        if (file.exists())
            file.delete();
        fileService.setDataTarget(file, 0);
        commandService.expectResponse("FETCH-RESP");
        CommandService.sendMsg("FETCH " + file.getName(), networkHandler.getChannel());
    }

    public void store(String fileName) throws FileNotFoundException {
        File file = Paths.get("local", fileName).toFile();
        if (!file.exists())
            throw new FileNotFoundException();
        fileService.setDataSource(file);
        commandService.expectResponse("STORE-RESP");
        ClientCommandService.sendMsg("STORE " + file.getName() + " " + file.length(), networkHandler.getChannel());
    }

    public void delete(String fileName) {
        commandService.expectResponse("REMOVE-RESP");
        ClientCommandService.sendMsg("REMOVE " + fileName, networkHandler.getChannel());
    }

    public void rename(String fileName) {
        // TODO
        commandService.expectResponse("RENAME-RESP");
        ClientCommandService.sendMsg("RENAME " + fileName, networkHandler.getChannel());
    }

    public void list() {
        commandService.expectResponse("LIST-RESP");
        ClientCommandService.sendMsg("LIST", networkHandler.getChannel());
    }

}
