package cs451.message;

import java.nio.ByteBuffer;
import java.util.Objects;

import cs451.host.HostInfo;

public final class BroadcastMessage extends Message {

	private static int SENDER_OFFSET = 4;
	private final int originalSenderId;

	public BroadcastMessage(byte[] payload, int sequenceNbr, int senderId, int originalSenderId) {
		super(payload, sequenceNbr, senderId);
		this.originalSenderId = originalSenderId;
	}

	public BroadcastMessage(int sequenceNbr, int senderId) {
		super(new byte[0], sequenceNbr, senderId);
		this.originalSenderId = senderId;
	}

	public P2PMessage toP2PMessage(int receiverId) {
		byte[] payload = getPayload();
		ByteBuffer payloadBuffer = ByteBuffer.allocate(SENDER_OFFSET + payload.length);
		payloadBuffer.putInt(getOriginalSenderId()).put(payload);
		return new P2PMessage(payloadBuffer.array(), getSequenceNbr(), false, HostInfo.getCurrentHostId(), receiverId);
	}

	public static BroadcastMessage fromP2PMessage(P2PMessage message) {
		byte payload[] = message.getPayload();
		ByteBuffer payloadBuffer = ByteBuffer.wrap(payload);
		int originalSenderId = payloadBuffer.getInt();
		byte originalPayload[] = new byte[payload.length - SENDER_OFFSET];
		payloadBuffer.get(originalPayload);
		return new BroadcastMessage(originalPayload, message.getSequenceNbr(), message.getSenderId(), originalSenderId);

	}

	public int getOriginalSenderId(){
		return originalSenderId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSequenceNbr(), getOriginalSenderId());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BroadcastMessage) && ((BroadcastMessage) obj).getSequenceNbr() == getSequenceNbr()
				&& ((BroadcastMessage) obj).getOriginalSenderId() == getOriginalSenderId();
	}

}
