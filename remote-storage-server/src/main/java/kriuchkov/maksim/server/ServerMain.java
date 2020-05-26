package kriuchkov.maksim.server;

public class ServerMain {

    static final int SERVER_PORT = 8189;

    public static void main(String[] args) throws Throwable {
        new Server().launch(SERVER_PORT);
    }

}
