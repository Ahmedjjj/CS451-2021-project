package cs451.broadcast;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import cs451.host.HostInfo;
import cs451.link.PerfectLink;
import cs451.message.BroadcastMessage;
import cs451.message.P2PMessage;

public final class BestEffort extends Broadcaster implements PerfectLink.Receiver {
	private final Broadcaster.Receiver receiver;
	private final PerfectLink link;
	private int p2pSeqNum;

	public BestEffort(Broadcaster.Receiver receiver) throws SocketException, UnknownHostException {
		this.receiver = receiver;
		this.link = new PerfectLink(this);
		this.p2pSeqNum = 1;
	}

	@Override
	public void deliver(P2PMessage message) throws IOException {
		BroadcastMessage originalMessage = BroadcastMessage.fromP2PMessage(message);
		receiver.deliver(originalMessage);
	}

	@Override
	public void broadcast(BroadcastMessage message) throws IOException {
		assert message.getSenderId() == HostInfo.getCurrentHostId();

		for (int i = 1; i <= HostInfo.numHosts(); i++) {
			P2PMessage destMessage = message.toP2PMessage(i, p2pSeqNum++);
			link.send(destMessage);
		}
	}

	@Override
	public void stop() {
		this.link.stop();
	}

}
