package cs451.message;

public abstract class Message {
	private final byte[] payload;
	private final int sequenceNbr;
	private final int senderId;

	public Message(byte[] payload, int sequenceNbr, int senderId) {
		this.payload = new byte[payload.length];
		System.arraycopy(payload, 0, this.payload, 0, payload.length);
		this.sequenceNbr = sequenceNbr;
		this.senderId = senderId;
	}

	public int getSenderId() {
		return senderId;
	}

	public int getSequenceNbr() {
		return sequenceNbr;
	}

	public byte[] getPayload() {
		return payload.clone();
	}
}