package kriuchkov.maksim.client.connection;

import kriuchkov.maksim.common.FileService;

class ClientFileService extends FileService {

    private static final ClientFileService instance = new ClientFileService();

    public static ClientFileService getInstance() {
        return instance;
    }

    private Runnable storeSuccess;
    private Runnable storeFailure;
    private Runnable fetchSuccess;
    private Runnable fetchFailure;

    public void doStore(Runnable callback) throws Exception {
        sendFile(dataSource, NetworkHandler.getInstance().getChannel(), null);
    }

    public void setStoreSuccess(Runnable storeSuccess) {
        this.storeSuccess = storeSuccess;
    }

    public void setStoreFailure(Runnable storeFailure) {
        this.storeFailure = storeFailure;
    }

    public void setFetchSuccess(Runnable fetchSuccess) {
        this.fetchSuccess = fetchSuccess;
    }

    public void setFetchFailure(Runnable fetchFailure) {
        this.fetchFailure = fetchFailure;
    }
}
