package cs451.message;

import static org.junit.jupiter.api.Assertions.*;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cs451.host.Host;
import cs451.host.HostInfo;

class TestP2PMessage {
	
	@BeforeEach
	void setUp() {
		List<Host> hosts = new LinkedList<>();
		for (int i = 1; i <= 3; i++) {
			Host host = new Host();
			host.populate(Integer.toString(i), "localhost", Integer.toString(i));
			hosts.add(host);
		}
		HostInfo.configureFromHostList(hosts);
		HostInfo.setCurrentHostId(1);
	}

	@Test
	void testToDatagramAndBack() throws UnknownHostException {
		P2PMessage msg = new P2PMessage(new byte[0], 1, false, 1, 1);
		assertEquals(msg, P2PMessage.fromPacket(msg.toPacket()));
	}

}
