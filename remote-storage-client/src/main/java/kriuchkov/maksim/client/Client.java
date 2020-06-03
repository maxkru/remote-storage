package kriuchkov.maksim.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kriuchkov.maksim.client.connection.MainService;

import java.io.IOException;

public class Client extends Application {

    static final String SERVER_IP_ADDRESS = "localhost";
    static final int SERVER_PORT = 8189;

    private static Scene scene;

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
        MainService.getInstance().launchNetwork(SERVER_IP_ADDRESS, SERVER_PORT);
        launch();
    }
}
