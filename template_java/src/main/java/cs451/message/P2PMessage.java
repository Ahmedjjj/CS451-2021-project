package cs451.message;

import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import cs451.host.Host;
import cs451.host.HostInfo;
import static cs451.util.Constants.*; 

public final class P2PMessage extends Message {


	private final boolean isAck;
	private final int receiverId;
	private final int sequenceNbr;

	public P2PMessage(byte[] payload, int sequenceNbr, boolean isAck, int senderId, int receiverId) {
		super(payload, senderId);
		this.isAck = isAck;
		this.receiverId = receiverId;
		this.sequenceNbr = sequenceNbr;
	}

	public P2PMessage(int sequenceNbr, int senderId, int receiverId) {
		this(new byte[0], sequenceNbr, false, senderId, receiverId);
	}

	public P2PMessage getAck() {
		return new P2PMessage(new byte[0], getSequenceNbr(), true, getReceiverId(), getSenderId());
	}

	public static P2PMessage fromPacket(DatagramPacket packet) {
		byte[] packetData = packet.getData();
		ByteBuffer dataBuffer = ByteBuffer.wrap(packetData);
		int sequenceNbr = dataBuffer.getInt();
		int senderId = dataBuffer.getInt();
		int receiverId = dataBuffer.getInt();
		boolean isAck = dataBuffer.get() == 1;
		byte[] payload = new byte[packet.getLength() - (SEQ_NBR_OFFSET + ACK_OFFSET + SENDER_OFFSET + RECEIVER_OFFSET)];
		dataBuffer.get(payload);
		return new P2PMessage(payload, sequenceNbr, isAck, senderId, receiverId);
	}

	public DatagramPacket toPacket() throws UnknownHostException {
		byte payload[] = getPayload();
		int totalPayloadLength = SEQ_NBR_OFFSET + ACK_OFFSET + SENDER_OFFSET + RECEIVER_OFFSET + payload.length;
		ByteBuffer dataBuffer = ByteBuffer.allocate(totalPayloadLength);
		byte ackByte = isAck() ? (byte) 1 : (byte) 0;
		dataBuffer.putInt(getSequenceNbr()).putInt(getSenderId()).putInt(getReceiverId()).put(ackByte).put(payload);
		byte[] finalPayload = dataBuffer.array();
		Host destHost = HostInfo.getHost(getReceiverId());
		return new DatagramPacket(finalPayload, finalPayload.length, destHost.getInetAddress(), destHost.getPort());
	}

	public int getReceiverId() {
		return receiverId;
	}

	public boolean isAck() {
		return isAck;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getSenderId(), getReceiverId(), getSequenceNbr(), isAck());
	}

	public int getSequenceNbr() {
		return sequenceNbr;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof P2PMessage)) {
			return false;
		} else {
			P2PMessage otherMsg = (P2PMessage) obj;
			return otherMsg.getSenderId() == this.getSenderId() && otherMsg.getReceiverId() == getReceiverId()
					&& otherMsg.getSequenceNbr() == this.getSequenceNbr() && otherMsg.isAck() == isAck()
					&& Arrays.equals(getPayload(), otherMsg.getPayload());
		}
	}
}
