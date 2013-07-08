package hu.paalgyula.ascnhalf.client;

/**
 * Created with IntelliJ IDEA.
 * User: Felhasználó
 * Date: 2013.07.04.
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
public enum WoWAuthPacket {
    NO_COMMAND(0xFF),
    REALM_LIST(0x10),
    LOGON_CHALLANGE(0x00),
    LOGON_PROOF(0x01);

    private int opcode;

    WoWAuthPacket(int opcode) {
        this.opcode = opcode;
    }

    public int getCode() {
        return this.opcode;
    }

    public static WoWAuthPacket getByValue( byte val ) {
        for ( WoWAuthPacket packet : values() ) {
            if ( packet.getCode() == val )
                return packet;
        }

        return null;
    }
}
