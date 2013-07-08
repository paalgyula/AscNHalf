package hu.paalgyula.ascnhalf.client.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

/**
 * Created with IntelliJ IDEA.
 * User: paalgyula
 * Date: 2013.07.04.
 * Time: 12:56
 */
public class CMSG_LOGON_CHALLENGE extends ClientPacket {

    private Channel channel;
    private String accName;

    public CMSG_LOGON_CHALLENGE(Channel channel, String accName) {
        this.channel = channel;
        this.accName = accName;
    }

    public void write() {
        int len = 30 + accName.length();

        byte b[] = new byte[len + 4];

        b[0] = 0x00; // LOGON_CHALLENGE
        b[1] = 8;
        b[2] = (byte) len;
        b[3] = (byte) (len >> 8);

        // Application name
        b[4] = 'W';
        b[5] = 'o';
        b[6] = 'W';
        b[7] = 0;

        // Version and build
        b[8] = 3;
        b[9] = 3;
        b[10] = 5;
        b[11] = (byte)12340;
        b[12] = (byte)(12340 >> 8);

        // Platform
        b[13] = 'a';
        b[14] = 'v';
        b[15] = 'a';
        b[16] = 'J';

        // Operating system
        b[17] = 'r';
        b[18] = 'd';
        b[19] = 'n';
        b[20] = 'A';

        // Locale
        b[21] = 'B';
        b[22] = 'G';
        b[23] = 'n';
        b[24] = 'e';

        // Timezone bias
        int tzo = 120;
        b[25] = (byte) tzo;
        b[26] = (byte) (tzo >> 8);
        b[27] = (byte) (tzo >> 16);
        b[28] = (byte) (tzo >> 24);

        // Interface IP address
        b[29] = (byte) 127;
        b[30] = (byte) 0;
        b[31] = (byte) 0;
        b[32] = (byte) 1;

        // Name length and name (must be uppercase)
        byte[] accBytes = accName.toUpperCase().getBytes();

        b[33] = (byte)accBytes.length;
        for (len = 0; len < accBytes.length; len++)
            b[34+len] = accBytes[len];

        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(b);

        channel.write(buffer);
    }
}
