package cs451.message;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class Message{
	private final static int SEQ_NBR_OFFSET = 4;
	private final static int ACK_OFFSET = 1;
	private final static int SENDER_OFFSET = 4;

	private final byte[] payload;
	private final int sequenceNbr;
	private final boolean isAck;
	private final int senderId;
	private final int receiverId;

	public Message(byte[] payload, int sequenceNbr, boolean isAck, int senderId, int receiverId) {
		this.payload = new byte[payload.length];
		System.arraycopy(payload, 0, this.payload, 0, payload.length);
		this.sequenceNbr = sequenceNbr;
		this.isAck = isAck;
		this.senderId = senderId;
		this.receiverId = receiverId;
	}

	public Message(int sequenceNbr, int senderId, int receiverId) {
		this(new byte[0], sequenceNbr, false, senderId, receiverId);
	}	

	public static Message fromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		ByteBuffer dataBuffer = ByteBuffer.wrap(packetData);
		int sequenceNbr = dataBuffer.getInt();
		int senderId = dataBuffer.getInt();
		int receiverId = dataBuffer.getInt();
		boolean isAck = dataBuffer.get() == 1;
		byte[] payload = new byte[packet.getLength() - (SEQ_NBR_OFFSET + ACK_OFFSET + SENDER_OFFSET + SENDER_OFFSET)];
		dataBuffer.get(payload);
		return new Message(payload, sequenceNbr, isAck, senderId, receiverId);
	}

	public Message getAck() {
		return new Message(new byte[0], this.sequenceNbr, true, this.receiverId, this.senderId);
	}

	public DatagramPacket toPacket() {
		int totalPayloadLength = SEQ_NBR_OFFSET + ACK_OFFSET + SENDER_OFFSET + SENDER_OFFSET + this.payload.length;
		ByteBuffer dataBuffer = ByteBuffer.allocate(totalPayloadLength);
		byte ackByte = isAck() ? (byte) 1 : (byte) 0;
		dataBuffer.putInt(this.sequenceNbr).putInt(this.senderId).putInt(this.receiverId).put(ackByte)
				.put(this.payload);
		byte[] finalPayload = dataBuffer.array();
		return new DatagramPacket(finalPayload, finalPayload.length);
	}

	public int getSenderId() {
		return senderId;
	}

	public int getReceiverId() {
		return receiverId;
	}

	public int getSequenceNbr() {
		return sequenceNbr;
	}

	public boolean isAck() {
		return isAck;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.senderId, this.receiverId, this.sequenceNbr, this.isAck);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Message)) {
			return false;
		} else {
			Message otherMsg = (Message) obj;
			return otherMsg.senderId == this.senderId && otherMsg.receiverId == this.receiverId
					&& otherMsg.sequenceNbr == this.sequenceNbr && otherMsg.isAck == this.isAck;
		}
	}

}