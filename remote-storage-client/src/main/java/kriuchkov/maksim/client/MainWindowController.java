package kriuchkov.maksim.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import kriuchkov.maksim.client.connection.MainService;

import java.io.IOException;

public class MainWindowController {

    private MainService mainService = MainService.getInstance();

    public Button fetchButton;
    public Button storeButton;
    public ListView<String> remoteFolderListView;
    public ListView<String> localFolderListView;

    
    @FXML
    private void storeButtonPress() throws Exception {
//        mainService.store();
    }

    @FXML
    private void fetchButtonPress() throws IOException {
//        mainService.fetch();
    }
}
