package cs451.message;

import static org.junit.jupiter.api.Assertions.*;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import cs451.host.Host;
import cs451.host.HostInfo;

class TestBroadcastMessage {

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
	@DisplayName("Creation should be properly done")
	void testCreate() {
		BroadcastMessage message = new BroadcastMessage(new byte[0], 1, 3, 1);
		assertEquals(message.getOriginalSequenceNbr(), 1);
		assertEquals(message.getOriginalSenderId(), 1);
		assertEquals(message.getSenderId(), 3);
		assertEquals(message.getPayload().length, 0);
	}

	@Test
	@DisplayName("Reboroadcast should only change sender id")
	void testRebroadcast() {
		BroadcastMessage message = new BroadcastMessage(new byte[0], 1, 3, 1);
		BroadcastMessage rebroadcast = message.toRebroadcastMessage();
		assertEquals(message.getOriginalSequenceNbr(), rebroadcast.getOriginalSequenceNbr());
		assertEquals(message.getOriginalSenderId(), rebroadcast.getOriginalSenderId());
		assertEquals(rebroadcast.getSenderId(), HostInfo.getCurrentHostId());
		assertEquals(rebroadcast.getPayload().length, 0);
	}

	@Test
	@DisplayName("Equals should only take seqnum and original sender into account")
	void testEquals() {
		assertEquals(new BroadcastMessage(0), new BroadcastMessage(0));
	}

	@Test
	@DisplayName("P2P message should have correct params")
	void testP2P() {
		BroadcastMessage message = new BroadcastMessage(new byte[0], 1, 3, 1);
		P2PMessage p2pMsg = message.toP2PMessage(2, 1);
		assertEquals(p2pMsg.isAck(), false);
		assertEquals(p2pMsg.getSequenceNbr(), 1);
		assertEquals(p2pMsg.getReceiverId(), 2);
		assertEquals(p2pMsg.getSenderId(), 3);
		assertTrue(p2pMsg.getPayload().length > 0, "payload is empty");
	}

	@Test
	@DisplayName("Should recover correct message")
	void testP2PAndBack() {
		BroadcastMessage message = new BroadcastMessage(new byte[0], 1, 3, 1);
		P2PMessage p2pMsg = message.toP2PMessage(2, 1);
		BroadcastMessage backMsg = BroadcastMessage.fromP2PMessage(p2pMsg);
		assertEquals(message, backMsg);
		assertArrayEquals(backMsg.getPayload(), message.getPayload());
		assertEquals(backMsg.getSenderId(), message.getSenderId());
		assertEquals(p2pMsg.getReceiverId(), 2);
	}

	@Test
	void testRebroadcastAndBack() {
		BroadcastMessage message = new BroadcastMessage(new byte[0], 1, 3, 1);
		P2PMessage p2pMsg = message.toRebroadcastMessage().toP2PMessage(2, 1);
		BroadcastMessage backMsg = BroadcastMessage.fromP2PMessage(p2pMsg);
		assertEquals(message, backMsg);
		assertArrayEquals(backMsg.getPayload(), message.getPayload());
		assertEquals(backMsg.getSenderId(), HostInfo.getCurrentHostId());
	}

	@Test
	void testRebroadcastAndBackNotEquals() {
		BroadcastMessage message = new BroadcastMessage(new byte[0], 1, 3, 1);
		P2PMessage p2pMsg = message.toRebroadcastMessage().toP2PMessage(2,1);
		assertNotEquals(message.toP2PMessage(2, 1), p2pMsg);
	}

	@Test
	void testToP2PToPacketAndBackEquals() throws UnknownHostException {
		BroadcastMessage message = new BroadcastMessage(new byte[0], 1, 3, 1);
		BroadcastMessage result = BroadcastMessage.fromP2PMessage(P2PMessage.fromPacket(message.toP2PMessage(2, 1).toPacket()));
		assertEquals(message, result);
		assertArrayEquals(result.getPayload(), message.getPayload());
		assertEquals(result.getSenderId(), message.getSenderId());
	}

}
