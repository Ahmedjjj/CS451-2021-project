package cs451.broadcast;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import cs451.host.HostInfo;
import cs451.message.BroadcastMessage;

public final class Fifo extends Broadcaster implements Broadcaster.Receiver{

	private final Broadcaster.Receiver receiver;
	private final int[] pendingSeqNum;
	private final UniformReliable urb;
	private final Map<Integer, PriorityQueue<BroadcastMessage>> pending;

	public Fifo(Broadcaster.Receiver receiver) throws SocketException, UnknownHostException {
		this.receiver = receiver;
		this.pendingSeqNum = new int[HostInfo.numHosts()];
		for (int i = 0; i < HostInfo.numHosts(); i++) {
			this.pendingSeqNum[i] = 1;
		}
		this.urb = new UniformReliable(this);
		this.pending = new HashMap<>();
	}

	@Override
	public void deliver(BroadcastMessage message) throws IOException {

		Comparator<BroadcastMessage> msgComparator = (msg1, msg2) -> {
			assert msg1.getOriginalSenderId() == msg2.getOriginalSenderId();
			return Integer.compare(msg1.getOriginalSequenceNbr(), msg2.getOriginalSequenceNbr());
		};

		int senderId = message.getOriginalSenderId();
		PriorityQueue<BroadcastMessage> queue = this.pending.computeIfAbsent(senderId,
				key -> new PriorityQueue<>(msgComparator));
		queue.add(message);

		boolean canDeliver = queue.peek().getOriginalSequenceNbr() == this.pendingSeqNum[senderId - 1];
		while (canDeliver) {
			this.receiver.deliver(queue.poll());
			pendingSeqNum[senderId - 1] += 1;
			canDeliver = (queue.peek() != null) && (queue.peek().getOriginalSequenceNbr() == pendingSeqNum[senderId - 1]);
		}

	}

	@Override
	public void broadcast(BroadcastMessage message) throws IOException {
		this.urb.broadcast(message);
	}

	@Override
	public void stop() {
		pending.forEach((k,v) -> {
			System.out.print(k + ": ");
			v.forEach(a -> System.out.print(a.getOriginalSequenceNbr() + " "));
			System.out.println();
		});
		for (int i = 0; i < pendingSeqNum.length; i++) {
			System.out.println(i + ": " + pendingSeqNum[i]);
		}
		this.urb.stop();
	}
}
