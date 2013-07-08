package hu.paalgyula.ascnhalf.client.packet;

import java.math.BigInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Felhasználó
 * Date: 2013.07.04.
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */
public abstract class ClientPacket {
    abstract void write() throws Exception;

    protected BigInteger getBigInteger(byte[] b) {
        byte[] r = new byte[b.length];
        // reverse little endian -> big endian, then create the big integer
        for (int i = 0; i < b.length; i++)
            r[i] = b[b.length - i - 1];
        return new BigInteger(1, r);
    }

    protected byte[] getBytesOf(BigInteger bi) {
        byte[] b = bi.toByteArray();
        byte[] r = new byte[b.length];
        // reverse big endian -> little endian
        for (int i = 0; i < b.length; i++)
            r[i] = b[b.length - i - 1];
        return r;
    }

    protected void copyBytes(byte[] dest, byte[] src, int len, int offs1, int offs2) {
        while (len-- > 0)
            dest[offs1++] = src[offs2++];
    }

    protected void copyBytes(byte[] dest, BigInteger src, int len, int offs) {
        byte[] ba = src.toByteArray();
        if (len == 0)
            len = ba.length;
        byte s = (src.signum() < 0) ? (byte) 255 : 0;
        // BigInteger uses big endian while we need little endian
        for (int i = 0; i < len; i++) {
            int idx = ba.length - i - 1;
            dest[offs++] = (idx >= 0) ? ba[idx] : s;
        }
    }
}
