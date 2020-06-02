package kriuchkov.maksim.client;

import io.netty.channel.Channel;
import kriuchkov.maksim.common.CommandService;

public class ClientCommandService extends CommandService {

    private static final ClientCommandService instance = new ClientCommandService();

    public static ClientCommandService getInstance() {
        return instance;
    }

    private NetworkHandler networkHandler = NetworkHandler.getInstance();
    private ClientFileService fileService = ClientFileService.getInstance();

    private String expectedResponse = null;

    public void parseAndExecute(String input, Channel channel) throws Exception {
        String[] split = input.split(" ", 2);
        String command = split[0];

        assertResponseExpected(command);

        switch (command) {
            case "AUTH-RESP":
                // TODO
                break;

            case "LIST-RESP":
                String[] fileNames = split[1].split("\n");
                break;

            case "FETCH-RESP":
                if (!split[1].split(" ")[0].equals("OK")) {
                    fileService.setDataTarget(null, 0);
                } else {
                    fileService.setExpectedDataLength(Long.parseLong(split[1].split(" ")[1]));
                }
                break;

            case "STORE-RESP":
                if (!split[1].split(" ")[0].equals("OK")) {
                    fileService.setDataSource(null);
                } else {
                    fileService.doStore();
                }
                break;

            case "REMOVE-RESP":
                // TODO
                break;
        }
    }

    public void expectResponse(String s) {
        expectedResponse = s;
    }

    public void assertResponseExpected(String response) {
        if (!response.equals(expectedResponse))
            throw new RuntimeException("Unexpected response from server: " + response);
    }
}
