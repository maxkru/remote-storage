package kriuchkov.maksim.client.connection;

import kriuchkov.maksim.common.FileService;

class ClientFileService extends FileService {

    private static final ClientFileService instance = new ClientFileService();

    public static ClientFileService getInstance() {
        return instance;
    }

    public void doStore(Runnable callback) throws Exception {
        sendFile(dataSource, NetworkHandler.getInstance().getChannel(), callback);
    }

}
