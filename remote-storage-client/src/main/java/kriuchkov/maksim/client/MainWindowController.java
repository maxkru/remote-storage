package kriuchkov.maksim.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import kriuchkov.maksim.client.connection.MainService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainWindowController {

    private MainService mainService = MainService.getInstance();

    public Button fetchButton;
    public Button storeButton;
    public ListView<String> remoteFolderListView;
    public ListView<String> localFolderListView;

    private final Path localFolder = Paths.get("local");


    private final Runnable storeSuccess = () -> Platform.runLater( () ->
    {
        try {
            updateLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    private final Consumer<String> storeFailure = (msg) -> {
        Platform.runLater( () ->
                showDialog(msg));
        Platform.runLater( () ->
        {
            try {
                updateLists();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

    private final Runnable fetchSuccess = () -> Platform.runLater( () ->
    {
        try {
            updateLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    private final Consumer<String> fetchFailure = (msg) -> {
        Platform.runLater( () ->
                showDialog(msg));
        Platform.runLater( () ->
        {
            try {
                updateLists();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    };

//    private final Runnable updateSuccess = () -> {
//
//    };
//
//    private final Consumer<String> updateFailure = () -> {
//
//    };
    
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
        updateLists();
    }

    void updateLists() throws IOException {
        // получить списки файлов

        // на сервере
        mainService.list( (list) ->
                Platform.runLater( () ->
                        remoteFolderListView.setItems(FXCollections.observableList(list))));

        // на клиенте
        List<String> list = Files.list(localFolder)
                .filter(Files::isRegularFile)
                .map(path -> path.getName(path.getNameCount() - 1).toString())
                .collect(Collectors.toList());
        Platform.runLater( () ->
                localFolderListView.setItems(FXCollections.observableList(list)) );
    }

    private void showDialog(String msg) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setContentText(msg);
        dialog.show();
    }
}
