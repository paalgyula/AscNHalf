package hu.paalgyula.ascnhalf.client;

import hu.paalgyula.ascnhalf.client.packet.CMSG_LOGON_CHALLENGE;
import hu.paalgyula.ascnhalf.client.packet.CMSG_LOGON_PROOF;
import hu.paalgyula.ascnhalf.client.packet.CMSG_REALM_LIST;
import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: paalgyula
 * Date: 2013.07.04.
 * Time: 11:04
 */
public class AuthClientHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(AuthClientHandler.class.getName());

    private String accName;
    private String password;

    public AuthClientHandler(String accName, String password) {
        this.accName = accName;
        this.password = password;
    }

    private byte[] sessionKey = new byte[20];

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        logger.log(Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        BigEndianHeapChannelBuffer buffer = (BigEndianHeapChannelBuffer) e.getMessage();

        WoWAuthPacket packet = WoWAuthPacket.getByValue( buffer.readByte() );
        System.out.println( "Packet received: " + packet );
        System.out.println(Arrays.toString(buffer.array()));

        switch ( packet ) {
            case LOGON_CHALLANGE:
                new CMSG_LOGON_PROOF(buffer, ctx.getChannel(), accName, password).write();
                break;
            case LOGON_PROOF:
                if ( buffer.array().length > 20 ) {
                    buffer.readBytes( sessionKey );
                    logger.log( Level.INFO, "Logged in! SessionKey: " + new BigInteger( sessionKey ).toString( 16 ) + " Listing realms." );
                    WoWClient.connectRealm( "87.229.98.127", 18585, sessionKey );
                    ctx.getChannel().close();
                    //new CMSG_REALM_LIST(ctx.getChannel()).write();
                } else {
                    ctx.getChannel().close();
                }
                break;
            case REALM_LIST:
                /* short textSize = */
                buffer.readShort();
                // ?0
                buffer.readShort();
                buffer.readByte();

                short realmCount = buffer.readShort();

                logger.log( Level.INFO, "Got [" + realmCount + "] realm" );

                for (int i=0;i<realmCount;i++) {
                    byte icon = buffer.readByte();
                    byte securityLevel = buffer.readByte();
                    byte realmFlags = buffer.readByte();

                    String name = readStr( buffer );
                    String address = readStr( buffer );

                    float population = buffer.readFloat();

                    byte charCount = buffer.readByte();
                    byte timezone = buffer.readByte();

                    // WTF?
                    buffer.readByte();//writeC(0x2C);

                    System.err.println("Realm '"+name+"' at '"+address+"' icon="+icon+", locked="+securityLevel+", color="+population+", chars="+charCount+", tz="+timezone);

                    logger.log( Level.INFO, String.format( "Realm %d: %s [%s] (%.5f) Characters: %d", i, name, address, population, charCount ) );
                }

                break;
        }

        // Connect to realm

    }

    private String readStr(ChannelBuffer cb) {
        String s = new String();
        int c;
        while ((c = cb.readByte()) != 0)
            s += String.valueOf((char)c);
        return s;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Channel channel = ctx.getChannel();
        new CMSG_LOGON_CHALLENGE(channel, accName).write();
    }
}
