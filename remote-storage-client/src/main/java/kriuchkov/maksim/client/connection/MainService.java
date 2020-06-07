package kriuchkov.maksim.client.connection;

import kriuchkov.maksim.common.CommandService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class MainService {

    private static final MainService instance = new MainService();

    public static MainService getInstance() {
        return instance;
    }

    private static final Logger logger = LogManager.getLogger(MainService.class);

    private NetworkHandler networkHandler = NetworkHandler.getInstance();
    private ClientCommandService commandService = ClientCommandService.getInstance();
    private ClientFileService fileService = ClientFileService.getInstance();

    private boolean connected = false;

    public void connect(String serverAddress, int serverPort) { ;
        CountDownLatch latch = new CountDownLatch(1);
        logger.debug("Invoking networkHandler.launch()");
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
        connected = true;
    }

    public void disconnect() {
        networkHandler.stop();
        connected = false;
    }

    public void fetch(String fileName, Runnable successCallback, Runnable failureCallback) throws FileNotFoundException {
        checkConnection();
        File file = Paths.get("local", fileName).toFile();
        if (file.exists())
            file.delete();
//        fileService.setDataTarget(file);
        commandService.expectResponse("FETCH-RESP");
        CommandService.sendMsg("FETCH " + file.getName(), networkHandler.getChannel());
    }

    private void checkConnection() {
        if (!connected)
            throw new IllegalStateException("Not connected");
    }

    public void store(String fileName, Runnable successCallback, Runnable failureCallback) throws FileNotFoundException {
        checkConnection();
        File file = Paths.get("local", fileName).toFile();
        if (!file.exists())
            throw new FileNotFoundException("Attempted to store a non-existing file: " + file.toString());
        fileService.setDataSource(file);
        commandService.expectResponse("STORE-RESP");
        ClientCommandService.sendMsg("STORE " + file.getName() + " " + file.length(), networkHandler.getChannel());
    }

    public void delete(String fileName) {
        checkConnection();
        commandService.expectResponse("REMOVE-RESP");
        ClientCommandService.sendMsg("REMOVE " + fileName, networkHandler.getChannel());
    }

    public void rename(String fileName) {
        checkConnection();
        // TODO
        commandService.expectResponse("RENAME-RESP");
        ClientCommandService.sendMsg("RENAME " + fileName, networkHandler.getChannel());
    }

    public void list() {
        checkConnection();
        commandService.expectResponse("LIST-RESP");
        ClientCommandService.sendMsg("LIST", networkHandler.getChannel());
    }

}
