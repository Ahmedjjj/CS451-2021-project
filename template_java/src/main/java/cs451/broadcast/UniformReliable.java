package cs451.broadcast;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cs451.host.HostInfo;
import cs451.message.BroadcastMessage;
import cs451.util.Stoppable;

public final class UniformReliable extends Broadcaster implements Broadcaster.Receiver{

	private final Broadcaster.Receiver receiver;
	private final BestEffort beb;
	private final Map<BroadcastMessage, Set<Integer>> pending;
	private final Set<BroadcastMessage> delivered;
	private final List<BroadcastMessage> recorded;

	public UniformReliable(Broadcaster.Receiver receiver) throws SocketException, UnknownHostException {
		this.receiver = receiver;
		this.beb = new BestEffort(this);
		this.pending = new ConcurrentHashMap<>();
		this.delivered = new HashSet<>();
		this.recorded = new LinkedList<>();
	}

	@Override
	public void broadcast(BroadcastMessage message) throws IOException {
//		System.out.println("Broadcasting message from originalSender " + message.getOriginalSenderId() + " seq num "
//				+ message.getSequenceNbr() + " sender " + message.getSenderId());
//		System.out.println(String.format("ub %s %s %s", message.getOriginalSenderId(), message.getSequenceNbr(), message.getSenderId()));
		this.pending.put(message, new HashSet<>());
		this.beb.broadcast(message);
//		System.out.println(pending);
	}

	@Override
	public void deliver(BroadcastMessage message) throws IOException {
//		System.out.println("Received message from originalSender " + message.getOriginalSenderId() + " seq num "
//				+ message.getSequenceNbr() + " sender " + message.getSenderId());
//		System.out.println(String.format("r %s %s %s", message.getOriginalSenderId(), message.getSequenceNbr(), message.getSenderId()));
		
		this.recorded.add(message);
		if (!this.pending.containsKey(message)) {
//			System.out.println(String.format("urb %s %s %s", message.getOriginalSenderId(), message.getSequenceNbr(), message.getSenderId()));
			HashSet<Integer> pendingMsg = new HashSet<>();
			pendingMsg.add(message.getSenderId());
			this.pending.put(message, pendingMsg);
			this.beb.broadcast(message.toRebroadcastMessage());
			
		} else {
			Set<Integer> pendingMessage = this.pending.get(message);
			pendingMessage.add(message.getSenderId());
//			System.out.println("size " + pendingMessage.size());
			if (pendingMessage.size() >= HostInfo.getMinNumCorrectHosts() && !delivered.contains(message)) {
				delivered.add(message);
				this.receiver.deliver(message);
			}

		}

	}

	@Override
	public void stop() {
		System.out.println(recorded);
		System.out.println(recorded.size());
		this.pending.forEach((s,m) ->{
			System.out.println(s.getOriginalSenderId() + " " + s.getSequenceNbr() + " : " + m);
		});
		this.beb.stop();
	}

}
