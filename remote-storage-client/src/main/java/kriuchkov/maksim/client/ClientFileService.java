package kriuchkov.maksim.client;

import kriuchkov.maksim.common.FileService;

public class ClientFileService extends FileService {

    private static final ClientFileService instance = new ClientFileService();

    public static ClientFileService getInstance() {
        return instance;
    }

    public void doStore() throws Exception {
        sendFile(dataSource, NetworkHandler.getInstance().getChannel(), null);
    }
}
