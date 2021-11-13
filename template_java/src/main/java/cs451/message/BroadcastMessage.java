package cs451.message;

import java.nio.ByteBuffer;
import java.util.Objects;

import cs451.host.HostInfo;
import static cs451.util.Constants.*;

public final class BroadcastMessage extends Message {

	private final int originalSenderId;
	private final int originalSequenceNbr;

	public BroadcastMessage(byte[] payload, int originalSequenceNbr, int senderId, int originalSenderId) {
		super(payload, senderId);
		this.originalSenderId = originalSenderId;
		this.originalSequenceNbr = originalSequenceNbr;
	}

	public BroadcastMessage(int originalSequenceNbr, int senderId) {
		super(new byte[0], senderId);
		this.originalSenderId = senderId;
		this.originalSequenceNbr = originalSequenceNbr;
	}

	public BroadcastMessage(int originalSequenceNbr) {
		this(originalSequenceNbr, HostInfo.getCurrentHostId());
	}

	public P2PMessage toP2PMessage(int receiverId, int sequenceNumber) {
		byte[] payload = getPayload();
		ByteBuffer payloadBuffer = ByteBuffer.allocate(SENDER_OFFSET + SEQ_NBR_OFFSET + payload.length);
		payloadBuffer.putInt(getOriginalSenderId()).putInt(getOriginalSequenceNbr()).put(payload);
		return new P2PMessage(payloadBuffer.array(), sequenceNumber, false, getSenderId(), receiverId);
	}

	public BroadcastMessage toRebroadcastMessage() {
		return new BroadcastMessage(getPayload(), getOriginalSequenceNbr(), HostInfo.getCurrentHostId(),
				getOriginalSenderId());
	}

	public static BroadcastMessage fromP2PMessage(P2PMessage message) {
		byte payload[] = message.getPayload();
		ByteBuffer payloadBuffer = ByteBuffer.wrap(payload);
		int originalSenderId = payloadBuffer.getInt();
		int originalSequenceNumber = payloadBuffer.getInt();
		byte originalPayload[] = new byte[payload.length - SENDER_OFFSET - SEQ_NBR_OFFSET];
		payloadBuffer.get(originalPayload);
		return new BroadcastMessage(originalPayload, originalSequenceNumber, message.getSenderId(), originalSenderId);
	}

	public int getOriginalSenderId() {
		return originalSenderId;
	}

	public int getOriginalSequenceNbr() {
		return originalSequenceNbr;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getOriginalSequenceNbr(), getOriginalSenderId());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BroadcastMessage) && ((BroadcastMessage) obj).getOriginalSequenceNbr() == getOriginalSequenceNbr()
				&& ((BroadcastMessage) obj).getOriginalSenderId() == getOriginalSenderId();
	}

}
