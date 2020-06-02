package kriuchkov.maksim.common;

import io.netty.channel.Channel;

public class CommandService {

    public static void sendMsg(String msg, Channel channel) {
        channel.writeAndFlush(msg);
    }

}
