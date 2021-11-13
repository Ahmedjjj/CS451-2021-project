package cs451.broadcast;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cs451.host.HostInfo;
import cs451.message.BroadcastMessage;

public final class UniformReliable extends Broadcaster implements Broadcaster.Receiver {

	private final Broadcaster.Receiver receiver;
	private final BestEffort beb;
	private final Map<BroadcastMessage, Set<Integer>> pending;
	private final Set<BroadcastMessage> delivered;

	public UniformReliable(Broadcaster.Receiver receiver) throws SocketException, UnknownHostException {
		this.receiver = receiver;
		this.beb = new BestEffort(this);
		this.pending = new ConcurrentHashMap<>();
		this.delivered = new HashSet<>();
	}

	@Override
	public void broadcast(BroadcastMessage message) throws IOException {
		this.pending.put(message, new HashSet<>());
		this.beb.broadcast(message);
	}

	@Override
	public void deliver(BroadcastMessage message) throws IOException {

		if (!this.pending.containsKey(message)) {
			HashSet<Integer> pendingMsg = new HashSet<>();
			pendingMsg.add(message.getSenderId());
			this.pending.put(message, pendingMsg);
			this.beb.broadcast(message.toRebroadcastMessage());

		} else {
			Set<Integer> pendingMessage = this.pending.get(message);
			pendingMessage.add(message.getSenderId());
			if (pendingMessage.size() >= HostInfo.getMinNumCorrectHosts() && !delivered.contains(message)) {
				delivered.add(message);
				this.receiver.deliver(message);
			}

		}

	}

	@Override
	public void stop() {
		this.beb.stop();
	}

}
