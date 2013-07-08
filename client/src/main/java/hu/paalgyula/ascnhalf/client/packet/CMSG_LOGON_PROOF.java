package hu.paalgyula.ascnhalf.client.packet;

import org.jboss.netty.buffer.BigEndianHeapChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Felhasználó
 * Date: 2013.07.04.
 * Time: 13:17
 * To change this template use File | Settings | File Templates.
 */
public class CMSG_LOGON_PROOF extends ClientPacket {
    private BigEndianHeapChannelBuffer buffer;
    private String accName;
    private String password;
    private Channel channel;

    public CMSG_LOGON_PROOF(BigEndianHeapChannelBuffer buffer, Channel channel, String accName, String password) {
        this.channel = channel;
        this.buffer = buffer;
        this.accName = accName;
        this.password = password;
    }

    @Override
    public void write() throws Exception {
        System.out.println(Arrays.toString(buffer.array()));

        System.err.println("gotChallenge()");

        byte command = buffer.readByte();
        byte error = buffer.readByte();

        // B(32), ?(1)==1, g(1), ?(1)==32, N(32), s(32), unk3(16), ?(1)==0
        byte[] bBytes = new byte[32];
        buffer.readBytes(bBytes);
        BigInteger B = getBigInteger(bBytes);

        // g size (=1)
        byte[] gBytes = new byte[buffer.readByte()];
        buffer.readBytes(gBytes);
        BigInteger g = getBigInteger(gBytes);

        byte[] nBytes = new byte[buffer.readByte()];
        buffer.readBytes(nBytes);

        BigInteger N = getBigInteger(nBytes);

        byte[] salt = new byte[32];
        buffer.readBytes(salt);

        BigInteger sb = getBigInteger(salt);

        byte unk3[] = new byte[16];
        buffer.readBytes(unk3);

        int unk12 = buffer.readByte();
        if (sb.signum() == 0) {
            // if s == 0 the user is likely incorrect (has no salt on server)
            String errorStr = "Account unknown by server";
        }

        System.err.println("Got challenge, B=" + B.toString(16) + " g=" + g.intValue() + " N=" + N.toString(16) + " salt=" + sb.toString(16));

        MessageDigest h = MessageDigest.getInstance("SHA-1");

        byte[] userName = accName.getBytes("UTF-8");
        h.update(userName);
        byte[] userHash = h.digest();
        h.reset();
        h.update((accName + ":" + password.toUpperCase()).getBytes("UTF-8"));
        byte[] authHash = h.digest();

        h = MessageDigest.getInstance("SHA-1");

        h.update(salt);
        h.update(authHash);
        BigInteger x = getBigInteger(h.digest());
        System.err.println("Computed x=" + x.toString(16));
        BigInteger v = g.modPow(x, N);
        System.err.println("Computed v=" + v.toString(16));
        BigInteger a = BigInteger.ONE.add(new BigInteger(128, new Random()));
        BigInteger A = g.modPow(a, N);
        System.err.println("Computed a=" + a.toString(16) + " A=" + A.toString(16));
        h.reset();
        h.update(getBytesOf(A));
        h.update(getBytesOf(B));
        BigInteger u = getBigInteger(h.digest());
        System.err.println("Computed u=" + u.toString(16));
        BigInteger k = new BigInteger("3");
        BigInteger S = B.subtract(k.multiply(v)).modPow(a.add(u.multiply(x)), N);
        System.err.println("Computed S=" + S.toString(16));
        byte[] s = new byte[32];
        copyBytes(s, S, 32, 0);
        byte[] s1 = new byte[16];
        byte[] s2 = new byte[16];
        for (int i = 0; i < 16; i++) {
            s1[i] = s[i * 2];
            s2[i] = s[i * 2 + 1];
        }
        h.reset();
        h.update(s1);
        s1 = h.digest();
        h.reset();
        h.update(s2);
        s2 = h.digest();
        s = new byte[40];
        for (int i = 0; i < 20; i++) {
            s[i * 2] = s1[i];
            s[i * 2 + 1] = s2[i];
        }

        byte[] sessKey = s;
        System.err.println("Session key=" + getBigInteger(sessKey).toString(16));

        h.reset();
        h.update(nBytes);

        byte[] ngh = h.digest();
        h.reset();
        h.update(gBytes);
        byte[] gh = h.digest();
        for (int i = 0; i < 20; i++)
            ngh[i] ^= gh[i];
        h.reset();
        h.update(ngh);
        h.update(userHash);
        h.update(salt);
        h.update(getBytesOf(A));
        h.update(getBytesOf(B));
        h.update(sessKey);
        byte[] m1 = h.digest();
        System.err.println("M1=" + getBigInteger(m1).toString(16));
        h.reset();
        h.update(getBytesOf(A));
        h.update(m1);
        h.update(sessKey);
        byte[] authM2 = h.digest();
        System.err.println("M2=" + getBigInteger(authM2).toString(16));
        byte b[] = new byte[75];
        b[0] = 0x01; // LOGON_PROOF
        // A(32), M1(20), H(20), nkeys(1), ?(1)
        copyBytes(b, A, 32, 1);
        copyBytes(b, m1, 20, 33, 0);
        // WTF is crc_hash?
        b[73] = 0;
        b[74] = (byte) unk12;

        ChannelBuffer cb = ChannelBuffers.copiedBuffer(b);
        channel.write( cb );
    }
}
