package kriuchkov.maksim.common;

public class Protocol {
    public static final byte COMMAND_SIGNAL_BYTE = 1;
    public static final byte DATA_SIGNAL_BYTE = 2;

    public static final String[] POSSIBLE_CLIENT_COMMANDS = {
            "HELLO",
            "LIST",
            "FETCH",
            "STORE",
            "DELETE",
            "RENAME",
            "AUTH",
            "REGISTER-USER",
    };

    public static final String[] POSSIBLE_SERVER_RESPONSES = {
            "HELLO-RESP",
            "LIST-RESP",
            "FETCH-RESP",
            "STORE-RESP",
            "DELETE-RESP",
            "RENAME-RESP",
            "AUTH-RESP",
            "REGISTER-USER-RESP",
    };
}
