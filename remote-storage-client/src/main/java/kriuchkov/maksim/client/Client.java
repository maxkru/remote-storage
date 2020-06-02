package kriuchkov.maksim.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Client extends Application {

    static final String SERVER_IP_ADDRESS = "localhost";
    static final int SERVER_PORT = 8189;

    private static Scene scene;

    private static final NetworkHandler networkHandler = NetworkHandler.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("mainWindow.fxml"));
        scene = new Scene(fxmlLoader.load());
        MainWindowController controller = fxmlLoader.getController();
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                networkHandler.launch(latch, SERVER_IP_ADDRESS, SERVER_PORT, new IncomingDataReader());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }).start();
        latch.await();
        launch();
    }
}
