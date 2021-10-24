package cs451;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public final class Message {
	private final static int SEQ_NBR_OFFSET = 4;
	private final static int ACK_OFFSET = 1;

	private final byte[] payload;
	private final int sequenceNbr;
	private final boolean isAck;

	public Message(byte[] payload, int sequenceNbr, boolean isAck) {
		this.payload = new byte[payload.length];
		System.arraycopy(payload, 0, this.payload, 0, payload.length);
		this.sequenceNbr = sequenceNbr;
		this.isAck = isAck;
	}

	public static Message fromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		ByteBuffer dataBuffer = ByteBuffer.wrap(packetData);
		int sequenceNbr = dataBuffer.getInt();
		boolean isAck = dataBuffer.get() == 1;
		byte[] payload = new byte[packet.getLength() - (SEQ_NBR_OFFSET + ACK_OFFSET)];
		dataBuffer.get(payload);
		return new Message(payload, sequenceNbr, isAck);
	}

	public Message toAckMessage() {
		return new Message(new byte[0], getSequenceNbr(), true);
	}

	public DatagramPacket toPacket() {
		int totalPayloadLength = SEQ_NBR_OFFSET + ACK_OFFSET + this.payload.length;
		ByteBuffer dataBuffer = ByteBuffer.allocate(totalPayloadLength);
		byte ackByte = isAck() ? (byte) 1 : (byte) 0;
		dataBuffer.putInt(getSequenceNbr()).put(ackByte).put(this.payload);
		byte[] finalPayload = dataBuffer.array();
		return new DatagramPacket(finalPayload, finalPayload.length);
	}

	public int getSequenceNbr() {
		return sequenceNbr;
	}

	public boolean isAck() {
		return isAck;
	}

}