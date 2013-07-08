package hu.paalgyula.ascnhalf.client;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: paalgyula
 * Date: 2013.07.05.
 * Time: 13:59
 */
public class RealmClient extends SimpleChannelUpstreamHandler implements Runnable {
    private ChannelFuture future;
    private String host;
    private int port;
    private byte[] key;
    private PacketData data;

    public RealmClient(String host, int port, byte[] key) throws JAXBException {
        this.host = host;
        this.port = port;
        this.key = key;

        try {
            JAXBContext jc = JAXBContext.newInstance(PacketData.class);
            Unmarshaller un = jc.createUnmarshaller();

            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            data = PacketData.class.cast(un.unmarshal(RealmClient.this.getClass().getResourceAsStream("/client_packets.xml")));
        } catch (JAXBException e) {
            System.err.println("Error while loading xml data for class: "
                    + PacketData.class.getCanonicalName());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void run() {
        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(RealmClient.this);
            }
        });

        // Start the connection attempt.
        this.future = bootstrap.connect(new InetSocketAddress(host, port));

        // Wait until the connection is closed or the connection attempt fails.
        this.future.getChannel().getCloseFuture().awaitUninterruptibly();

        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        BigEndianHeapChannelBuffer buffer = (BigEndianHeapChannelBuffer) e.getMessage();
        System.out.println( ChannelBuffers.hexDump( buffer ).replaceAll("[a-zA-Z0-9]{2}", "$0 ") );

        System.out.println(Arrays.toString(buffer.array()));

        int len = buffer.readByte();
        if ((len & 0x80) != 0)
            len = ((len & 0x7f) << 16) | (buffer.readByte() << 8) | buffer.readByte();
        else
            len = (len << 8) | buffer.readByte();

        // for now support only packets up to 64KBytes
        if (len < 2 || len > 0x10002) {
            throw new Exception( "Invalid packet length 0x"+Integer.toHexString(len)+" ("+len+")" );
        }

        len -= 2;
        // opcodes are little endian
        byte[] cmdBytes = new byte[2];
        buffer.readBytes( cmdBytes );

        ByteBuffer bb = ByteBuffer.wrap( cmdBytes );
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int cmd = bb.asShortBuffer().get();

        byte[] buf = (len > 0) ? new byte[len] : null;
        buffer.readBytes(buf);

        if ( 0x1ec == cmd )
            System.out.println( "Jujj!" );

        System.out.println( String.format( "Packet received: (%x)", cmd ) );
        System.out.println( "Readable bytes: " + buffer.readableBytes() );

        super.messageReceived(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
