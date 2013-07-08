package hu.paalgyula.ascnhalf.client;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: paalgyula
 * Date: 2013.07.08.
 * Time: 23:34
 */
@XmlRootElement(name = "packets")
public class PacketData {
    List<Packet> packets;

    public List<Packet> getPackets() {
        return packets;
    }

    public void setPackets(List<Packet> packets) {
        this.packets = packets;
    }

    public static class Packet {
        private String opcode;
        private String name;

        public String getOpcode() {
            return opcode;
        }

        public void setOpcode(String opcode) {
            this.opcode = opcode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
