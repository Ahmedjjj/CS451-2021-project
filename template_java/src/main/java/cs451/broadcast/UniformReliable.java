package cs451.broadcast;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 
import cs451.host.HostInfo;
import cs451.message.BroadcastMessage;

public final class UniformReliable implements Broadcaster, Broadcaster.Receiver {

	private final Broadcaster.Receiver receiver;
	private final BestEffort beb;
	private final Map<BroadcastMessage, Set<Integer>> pending;
	private final Set<BroadcastMessage> delivered;

	public UniformReliable(Broadcaster.Receiver receiver) throws SocketException, UnknownHostException {
		this.receiver = receiver;
		this.beb = new BestEffort(this);
		this.pending = new HashMap<>();
		this.delivered = new HashSet<>();
	}

	@Override
	public void broadcast(BroadcastMessage message) throws IOException {
		beb.broadcast(message);
		this.pending.put(message, new HashSet<>());
	}

	@Override
	public void deliver(BroadcastMessage message) throws IOException {
		if (!this.pending.containsKey(message)) {
			this.broadcast(message);
		} else {
			Set<Integer> pendingMessage = this.pending.get(message);
			pendingMessage.add(message.getSenderId());
			if (pendingMessage.size() >= HostInfo.getMinNumCorrectHosts() && !delivered.contains(message)) {
				delivered.add(message);
				this.receiver.deliver(message);
			}

		}

	}

}
