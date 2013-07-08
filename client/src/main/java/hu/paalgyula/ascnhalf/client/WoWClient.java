package hu.paalgyula.ascnhalf.client;

import bsh.Interpreter;
import bsh.NameSpace;
import bsh.util.JConsole;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Felhasználó
 * Date: 2013.07.04.
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
public class WoWClient extends Thread {
    private final String host;
    private final int port;
    private ChannelFuture future = null;

    private static WoWClient client;

    public WoWClient(String host, int port) {
        this.host = host;
        this.port = port;
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
                return Channels.pipeline(new AuthClientHandler( "GMGOOFY", "susegoofyY0" ));
            }
        });

        // Start the connection attempt.
        this.future = bootstrap.connect(new InetSocketAddress(host, port));

        // Wait until the connection is closed or the connection attempt fails.
        this.future.getChannel().getCloseFuture().awaitUninterruptibly();

        // Shut down thread pools to exit.
        bootstrap.releaseExternalResources();
    }

    public static void main(String[] args) throws Exception {
        // Print usage if no argument is specified.
        /*if (args.length < 2 || args.length > 3) {
            System.err.println(
                    "Usage: " + EchoClient.class.getSimpleName() +
                            " <host> <port> [<first message size>]");
            return;
        }

        // Parse options.
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        final int firstMessageSize;
        if (args.length == 3) {
            firstMessageSize = Integer.parseInt(args[2]);
        } else {
            firstMessageSize = 256;
        }*/
        final String host = "hu.logon.tauri.hu";
        final int port = 3724;

        JConsole console = new JConsole();
        console.print( "Connecting to: ", Color.BLUE );
        console.print( host, new Color( 0, 106, 8) );
        console.print("[:" + port + "]", Color.RED);
        console.println();

        Interpreter interpreter = new Interpreter( console );
        interpreter.set( "bsh.prompt", "[AscNHalf Client]: " );
        new Thread( interpreter ).start();

        System.setOut( console.getOut() );
        System.setErr( console.getErr() );

        client = new WoWClient(host, port);
        client.start();

        JFrame frame = new JFrame( "Title" );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add( console );
        console.setPreferredSize( new Dimension( 200,200 ) );
        frame.pack();
        frame.setVisible( true );
    }

    public static void connectRealm( String host, int port, byte[] key ) throws Exception {
        RealmClient realmClient = new RealmClient(host, port, key);
        new Thread(realmClient).start();
    }
}
