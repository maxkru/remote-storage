package kriuchkov.maksim.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import kriuchkov.maksim.client.connection.MainService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MainWindowController {

    private MainService mainService = MainService.getInstance();

    public Button fetchButton;
    public Button storeButton;
    public ListView<String> remoteFolderListView;
    public ListView<String> localFolderListView;

    private final Path localFolder = Paths.get("..", "local");


    private final Runnable storeSuccess = () -> {

    };

    private final Runnable storeFailure = () -> {

    };

    private final Runnable fetchSuccess = () -> {

    };

    private final Runnable fetchFailure = () -> {

    };

    private final Runnable updateSuccess = () -> {

    };

    private final Runnable updateFailure = () -> {

    };
    
    @FXML
    private void storeButtonPress() throws Exception {
        String fileName = localFolderListView.getSelectionModel().getSelectedItem();
        mainService.store(fileName, storeSuccess, storeFailure);
    }

    @FXML
    private void fetchButtonPress() throws IOException {
        String fileName = remoteFolderListView.getSelectionModel().getSelectedItem();
        mainService.fetch(fileName, fetchSuccess, fetchFailure);
    }

    void init() throws IOException {
        // получить списки файлов

        // на сервере
        // Runnable updateList = ...???
        mainService.list();

        // на клиенте
        List<String> list = Files.list(localFolder)
                .filter(Files::isRegularFile)
                .map(path -> path.getName(path.getNameCount() - 1).toString())
                .collect(Collectors.toList());
        Platform.runLater( () ->
                localFolderListView.setItems(FXCollections.observableList(list)) );
    }
}
