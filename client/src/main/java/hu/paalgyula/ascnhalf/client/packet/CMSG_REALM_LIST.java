package hu.paalgyula.ascnhalf.client.packet;

import hu.paalgyula.ascnhalf.client.WoWAuthPacket;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

/**
 * Created with IntelliJ IDEA.
 * User: Felhasználó
 * Date: 2013.07.04.
 * Time: 13:31
 * To change this template use File | Settings | File Templates.
 */
public class CMSG_REALM_LIST extends ClientPacket {
    private Channel channel;

    public CMSG_REALM_LIST(Channel channel) {
        this.channel = channel;
    }

    public void write() throws Exception {
        byte b[] = new byte[5];
        b[0] = (byte) WoWAuthPacket.REALM_LIST.getCode();
        channel.write(ChannelBuffers.copiedBuffer(b));
    }
}
